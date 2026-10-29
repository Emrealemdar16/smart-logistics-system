package logistics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Bir araç için olusturulan paket atama listesini, optimize edilmis rotayi ve maliyet bilgisini tutar.
 */
public class DeliveryPlan {
    private final Vehicle vehicle;
    private final List<ShipmentPackage> assignedPackages;
    private final List<Location> routeStops;
    private final double totalDistanceKm;
    private final double estimatedCostTl;
    private final String strategyName;

    public DeliveryPlan(Vehicle vehicle, List<ShipmentPackage> assignedPackages,
                        List<Location> routeStops, double totalDistanceKm,
                        double estimatedCostTl, String strategyName) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Araç bos olamaz.");
        }
        if (assignedPackages == null || routeStops == null) {
            throw new IllegalArgumentException("Paket ve durak listeleri bos olamaz.");
        }
        if (totalDistanceKm < 0 || estimatedCostTl < 0) {
            throw new IllegalArgumentException("Mesafe ve maliyet negatif olamaz.");
        }
        this.vehicle = vehicle;
        this.assignedPackages = Collections.unmodifiableList(new ArrayList<>(assignedPackages));
        this.routeStops = Collections.unmodifiableList(new ArrayList<>(routeStops));
        this.totalDistanceKm = totalDistanceKm;
        this.estimatedCostTl = estimatedCostTl;
        this.strategyName = (strategyName == null) ? "" : strategyName;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public List<ShipmentPackage> getAssignedPackages() {
        return assignedPackages;
    }

    public List<Location> getRouteStops() {
        return routeStops;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public double getEstimatedCostTl() {
        return estimatedCostTl;
    }

    public String getStrategyName() {
        return strategyName;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Plan[Araç: %s | Paket Sayisi: %d | Mesafe: %.2f km | Tahmini Maliyet: %.2f TL | Strateji: %s]",
                vehicle.getPlateNumber(), assignedPackages.size(), totalDistanceKm, estimatedCostTl, strategyName);
    }
}
