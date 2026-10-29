package logistics.service;

import logistics.model.DeliveryPlan;
import logistics.model.Location;
import logistics.model.ShipmentPackage;
import logistics.model.Vehicle;
import logistics.repository.DataRepository;
import logistics.strategy.RouteStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Filo yönetimi, kapasiteye göre paket dagitim algoritmasi ve rota planlama süreçlerini yönetir.
 */
public class LogisticsService {
    private final DataRepository<Vehicle> vehicleRepository;
    private final DataRepository<ShipmentPackage> packageRepository;
    private RouteStrategy routeStrategy;
    private final Location depot;

    private final List<Vehicle> vehicles;
    private final List<ShipmentPackage> packages;
    private final List<ShipmentPackage> lastUnassignedPackages;

    public LogisticsService(DataRepository<Vehicle> vehicleRepository,
                            DataRepository<ShipmentPackage> packageRepository,
                            RouteStrategy routeStrategy,
                            Location depot) {
        if (vehicleRepository == null || packageRepository == null) {
            throw new IllegalArgumentException("Veri kayit bagimliliklari bos olamaz.");
        }
        if (routeStrategy == null) {
            throw new IllegalArgumentException("Rota stratejisi bos olamaz.");
        }
        if (depot == null) {
            throw new IllegalArgumentException("Merkez depo konumu bos olamaz.");
        }

        this.vehicleRepository = vehicleRepository;
        this.packageRepository = packageRepository;
        this.routeStrategy = routeStrategy;
        this.depot = new Location(depot);

        this.vehicles = new ArrayList<>(vehicleRepository.findAll());
        this.packages = new ArrayList<>(packageRepository.findAll());
        this.lastUnassignedPackages = new ArrayList<>();
    }

    public LogisticsService(DataRepository<Vehicle> vehicleRepository,
                            DataRepository<ShipmentPackage> packageRepository,
                            RouteStrategy routeStrategy) {
        this(vehicleRepository, packageRepository, routeStrategy, new Location("Merkez Depo", 0.0, 0.0));
    }

    public void setRouteStrategy(RouteStrategy routeStrategy) {
        if (routeStrategy == null) {
            throw new IllegalArgumentException("Rota stratejisi bos olamaz.");
        }
        this.routeStrategy = routeStrategy;
    }

    public RouteStrategy getRouteStrategy() {
        return routeStrategy;
    }

    public Location getDepot() {
        return new Location(depot);
    }

    public void addVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Eklenecek araç bos olamaz.");
        }
        for (Vehicle existing : vehicles) {
            if (existing.getPlateNumber().equalsIgnoreCase(vehicle.getPlateNumber())) {
                throw new IllegalArgumentException("Bu plakaya sahip bir araç zaten kayitli: " + vehicle.getPlateNumber());
            }
        }
        vehicles.add(vehicle);
        vehicleRepository.saveAll(vehicles);
    }

    public void addPackage(ShipmentPackage shipmentPackage) {
        if (shipmentPackage == null) {
            throw new IllegalArgumentException("Eklenecek paket bos olamaz.");
        }
        for (ShipmentPackage existing : packages) {
            if (existing.getPackageId().equalsIgnoreCase(shipmentPackage.getPackageId())) {
                throw new IllegalArgumentException("Bu koda sahip bir paket zaten kayitli: " + shipmentPackage.getPackageId());
            }
        }
        packages.add(shipmentPackage);
        packageRepository.saveAll(packages);
    }

    // Metot asiri yükleme (Overloading): standart paket eklemeyi kolaylastirir
    public ShipmentPackage addPackage(String packageId, String recipientName, Location destination, double weightKg) {
        ShipmentPackage pkg = new ShipmentPackage(packageId, recipientName, destination, weightKg);
        addPackage(pkg);
        return pkg;
    }

    public List<Vehicle> getVehicles() {
        return Collections.unmodifiableList(vehicles);
    }

    public List<ShipmentPackage> getPackages() {
        return Collections.unmodifiableList(packages);
    }

    public List<ShipmentPackage> getPendingPackages() {
        List<ShipmentPackage> pending = new ArrayList<>();
        for (ShipmentPackage pkg : packages) {
            if (!pkg.isDelivered()) {
                pending.add(pkg);
            }
        }
        return pending;
    }

    public List<ShipmentPackage> getLastUnassignedPackages() {
        return Collections.unmodifiableList(lastUnassignedPackages);
    }

    /**
     * Öncelikli Azalan İlk Uygun (First-Fit Decreasing) yaklasimiyla paketleri araç kapasitelerine göre dagitir
     * ve seçili stratejiye göre her aracin teslimat rotasini optimize eder.
     */
    public List<DeliveryPlan> planDeliveries() {
        for (Vehicle vehicle : vehicles) {
            vehicle.clearLoad();
        }
        lastUnassignedPackages.clear();

        List<ShipmentPackage> pendingPackages = getPendingPackages();
        pendingPackages.sort(new Comparator<ShipmentPackage>() {
            @Override
            public int compare(ShipmentPackage a, ShipmentPackage b) {
                int coldCompare = Boolean.compare(b.isColdChainRequired(), a.isColdChainRequired());
                if (coldCompare != 0) {
                    return coldCompare;
                }
                int urgentCompare = Boolean.compare(b.isUrgent(), a.isUrgent());
                if (urgentCompare != 0) {
                    return urgentCompare;
                }
                return Double.compare(b.getWeightKg(), a.getWeightKg());
            }
        });

        List<Vehicle> candidateVehicles = new ArrayList<>(vehicles);
        candidateVehicles.sort(new Comparator<Vehicle>() {
            @Override
            public int compare(Vehicle a, Vehicle b) {
                return Double.compare(a.getFuelConsumptionPerKm(), b.getFuelConsumptionPerKm());
            }
        });

        for (ShipmentPackage pkg : pendingPackages) {
            boolean assigned = false;
            for (Vehicle vehicle : candidateVehicles) {
                if (vehicle.canCarry(pkg)) {
                    vehicle.loadPackage(pkg);
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                lastUnassignedPackages.add(pkg);
            }
        }

        List<DeliveryPlan> plans = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (!vehicle.getLoadedPackages().isEmpty()) {
                List<Location> optimizedStops = routeStrategy.optimizeRoute(depot, vehicle.getLoadedPackages());
                double totalDistance = routeStrategy.calculateTotalDistance(optimizedStops);
                double estimatedCost = vehicle.calculateDeliveryCost(totalDistance);

                plans.add(new DeliveryPlan(
                        vehicle,
                        vehicle.getLoadedPackages(),
                        optimizedStops,
                        totalDistance,
                        estimatedCost,
                        routeStrategy.getStrategyName()
                ));
            }
        }
        return plans;
    }

    public int completeDeliveries(List<DeliveryPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return 0;
        }
        int deliveredCount = 0;
        for (DeliveryPlan plan : plans) {
            for (ShipmentPackage assigned : plan.getAssignedPackages()) {
                for (ShipmentPackage stored : packages) {
                    if (stored.equals(assigned) && !stored.isDelivered()) {
                        stored.markAsDelivered();
                        deliveredCount++;
                    }
                }
            }
        }
        for (Vehicle vehicle : vehicles) {
            vehicle.clearLoad();
        }
        lastUnassignedPackages.clear();
        packageRepository.saveAll(packages);
        return deliveredCount;
    }
}
