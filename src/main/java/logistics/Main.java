package logistics;

import logistics.factory.VehicleFactory;
import logistics.model.DeliveryPlan;
import logistics.model.Location;
import logistics.model.ShipmentPackage;
import logistics.model.Vehicle;
import logistics.repository.DataRepository;
import logistics.repository.FilePackageRepository;
import logistics.repository.FileVehicleRepository;
import logistics.service.LogisticsService;
import logistics.strategy.NearestNeighborRouteStrategy;
import logistics.strategy.PriorityFirstRouteStrategy;
import logistics.strategy.RouteStrategy;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Konsol tabanli kullanici menüsünü yöneten ana uygulama sinifi.
 * Tek Sorumluluk İlkesi (SRP) geregi ekran çiktilari ve kullanici girdileri yalnizca bu sinifta toplanmistir.
 */
public class Main {
    private static final String VEHICLES_FILE = "data/vehicles.txt";
    private static final String PACKAGES_FILE = "data/packages.txt";

    public static void main(String[] args) {
        DataRepository<Vehicle> vehicleRepo = new FileVehicleRepository(VEHICLES_FILE);
        DataRepository<ShipmentPackage> packageRepo = new FilePackageRepository(PACKAGES_FILE);
        RouteStrategy initialStrategy = new NearestNeighborRouteStrategy();

        LogisticsService service = new LogisticsService(vehicleRepo, packageRepo, initialStrategy);
        seedSampleDataIfEmpty(service);

        Scanner scanner = new Scanner(System.in);
        List<DeliveryPlan> currentPlans = null;
        boolean running = true;

        System.out.println("==================================================");
        System.out.println("             AKILLI LOJİSTİK SİSTEMİ              ");
        System.out.println("==================================================");

        while (running) {
            printMenu(service.getRouteStrategy().getStrategyName());
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        listVehicles(service);
                        break;
                    case "2":
                        addNewVehicle(scanner, service);
                        break;
                    case "3":
                        listPackages(service);
                        break;
                    case "4":
                        addNewPackage(scanner, service);
                        break;
                    case "5":
                        changeRouteStrategy(scanner, service);
                        break;
                    case "6":
                        currentPlans = generateAndPrintPlans(service);
                        break;
                    case "7":
                        completeDeliveriesFlow(service, currentPlans);
                        currentPlans = null;
                        break;
                    case "0":
                        running = false;
                        System.out.println("Veriler kaydedildi. Program sonlandiriliyor...");
                        break;
                    default:
                        System.out.println("Geçersiz seçim. Lütfen menüdeki numaralardan birini girin.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Hata: Sayisal bir deger girilmesi gereken alana geçersiz giris yapildi.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Hata: " + e.getMessage());
            }
        }
    }

    private static void printMenu(String activeStrategy) {
        System.out.println("\n--------------------------------------------------");
        System.out.println("Aktif Rota Stratejisi: " + activeStrategy);
        System.out.println("--------------------------------------------------");
        System.out.println("1 - Filodaki Araçlari Listele");
        System.out.println("2 - Filoya Yeni Araç Ekle");
        System.out.println("3 - Paketleri Listele");
        System.out.println("4 - Yeni Paket Kaydi Olustur");
        System.out.println("5 - Rota Optimizasyon Stratejisini Degistir");
        System.out.println("6 - Kapasiteye Göre Paket Dagitimi ve Rota Planla");
        System.out.println("7 - Planlanan Teslimatlari Tamamla");
        System.out.println("0 - Çikis");
        System.out.print("Seçiminiz: ");
    }

    private static void listVehicles(LogisticsService service) {
        List<Vehicle> vehicles = service.getVehicles();
        System.out.println("\n--- Kayitli Araç Filosu (" + vehicles.size() + " Araç) ---");
        if (vehicles.isEmpty()) {
            System.out.println("Kayitli araç bulunmuyor.");
            return;
        }
        for (Vehicle vehicle : vehicles) {
            System.out.println(" - " + vehicle);
        }
    }

    private static void addNewVehicle(Scanner scanner, LogisticsService service) {
        System.out.println("\n--- Yeni Araç Ekle ---");
        System.out.println("Araç Tipleri: 1- Panelvan (VAN), 2- Kamyon (TRUCK), 3- Sogutuculu Kamyon (REFRIGERATED)");
        System.out.print("Araç tipi seçiniz (1/2/3): ");
        String typeChoice = scanner.nextLine().trim();

        String type;
        if ("1".equals(typeChoice)) {
            type = "VAN";
        } else if ("2".equals(typeChoice)) {
            type = "TRUCK";
        } else if ("3".equals(typeChoice)) {
            type = "REFRIGERATED";
        } else {
            throw new IllegalArgumentException("Geçersiz araç tipi seçimi.");
        }

        System.out.print("Plaka (örn. 39LOG404): ");
        String plate = scanner.nextLine().trim();

        System.out.print("Maksimum yük kapasitesi (kg): ");
        double maxWeight = Double.parseDouble(scanner.nextLine().trim());

        Vehicle vehicle;
        if ("REFRIGERATED".equals(type)) {
            System.out.print("Hedef sogutucu sicakligi (°C, örn. -4): ");
            double targetTemp = Double.parseDouble(scanner.nextLine().trim());
            vehicle = VehicleFactory.createVehicle(type, plate, maxWeight, targetTemp);
        } else {
            vehicle = VehicleFactory.createVehicle(type, plate, maxWeight);
        }

        service.addVehicle(vehicle);
        System.out.println("Araç basariyla eklendi ve kaydedildi: " + vehicle);
    }

    private static void listPackages(LogisticsService service) {
        List<ShipmentPackage> packages = service.getPackages();
        System.out.println("\n--- Kayitli Paketler (" + packages.size() + " Paket) ---");
        if (packages.isEmpty()) {
            System.out.println("Kayitli paket bulunmuyor.");
            return;
        }
        for (ShipmentPackage pkg : packages) {
            System.out.println(" - " + pkg);
        }
    }

    private static void addNewPackage(Scanner scanner, LogisticsService service) {
        System.out.println("\n--- Yeni Paket Kaydi ---");
        System.out.print("Paket Kodu (örn. PKT-107): ");
        String id = scanner.nextLine().trim();

        System.out.print("Alici Adi / Kurum: ");
        String recipient = scanner.nextLine().trim();

        System.out.print("Teslimat Bölgesi / İlçe Adi: ");
        String locationName = scanner.nextLine().trim();

        System.out.print("Hedef X Koordinati (km): ");
        double x = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Hedef Y Koordinati (km): ");
        double y = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Paket Agirligi (kg): ");
        double weight = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Acil teslimat mi? (E/H): ");
        boolean urgent = scanner.nextLine().trim().equalsIgnoreCase("E");

        System.out.print("Soguk zincir gerekiyor mu? (E/H): ");
        boolean coldChain = scanner.nextLine().trim().equalsIgnoreCase("E");

        Location destination = new Location(locationName, x, y);
        ShipmentPackage pkg = new ShipmentPackage(id, recipient, destination, weight, urgent, coldChain);
        service.addPackage(pkg);
        System.out.println("Paket kaydedildi: " + pkg);
    }

    private static void changeRouteStrategy(Scanner scanner, LogisticsService service) {
        System.out.println("\n--- Rota Optimizasyon Stratejisi Seçimi ---");
        System.out.println("1 - En Yakin Komsu (Nearest Neighbor - Toplam mesafeyi azaltir)");
        System.out.println("2 - Öncelik Odakli Rota (Önce acil paketlerin duraklarina ugrar)");
        System.out.print("Seçiminiz (1/2): ");
        String selection = scanner.nextLine().trim();

        if ("1".equals(selection)) {
            service.setRouteStrategy(new NearestNeighborRouteStrategy());
        } else if ("2".equals(selection)) {
            service.setRouteStrategy(new PriorityFirstRouteStrategy());
        } else {
            throw new IllegalArgumentException("Geçersiz strateji seçimi.");
        }
        System.out.println("Yeni strateji etkinlestirildi: " + service.getRouteStrategy().getStrategyName());
    }

    private static List<DeliveryPlan> generateAndPrintPlans(LogisticsService service) {
        List<DeliveryPlan> plans = service.planDeliveries();
        System.out.println("\n================ DAgITIM VE ROTA PLANI ================");
        System.out.println("Kullanilan Strateji: " + service.getRouteStrategy().getStrategyName());

        if (plans.isEmpty()) {
            System.out.println("Dagitilacak bekleyen paket veya uygun kapasiteli araç bulunamadi.");
        } else {
            double grandTotalDistance = 0.0;
            double grandTotalCost = 0.0;

            for (DeliveryPlan plan : plans) {
                Vehicle v = plan.getVehicle();
                System.out.println("\nAraç: " + v);
                System.out.println("Yüklenen Paketler (" + plan.getAssignedPackages().size() + " adet):");
                for (ShipmentPackage pkg : plan.getAssignedPackages()) {
                    System.out.println("   * " + pkg);
                }

                System.out.print("Optimize Edilmis Rota: ");
                List<Location> stops = plan.getRouteStops();
                for (int i = 0; i < stops.size(); i++) {
                    System.out.print(stops.get(i).getName());
                    if (i < stops.size() - 1) {
                        System.out.print(" -> ");
                    }
                }
                System.out.println();
                System.out.printf(Locale.US, "Toplam Mesafe: %.2f km | Tahmini Maliyet: %.2f TL%n",
                        plan.getTotalDistanceKm(), plan.getEstimatedCostTl());

                grandTotalDistance += plan.getTotalDistanceKm();
                grandTotalCost += plan.getEstimatedCostTl();
            }

            System.out.println("-------------------------------------------------------");
            System.out.printf(Locale.US, "GENEL TOPLAM -> Mesafe: %.2f km | Tahmini Maliyet: %.2f TL%n",
                    grandTotalDistance, grandTotalCost);
        }

        List<ShipmentPackage> unassigned = service.getLastUnassignedPackages();
        if (!unassigned.isEmpty()) {
            System.out.println("\nUYARI - Kapasite veya donanim kisiti nedeniyle atanamayan paketler:");
            for (ShipmentPackage pkg : unassigned) {
                System.out.println("   ! " + pkg);
            }
        }
        return plans;
    }

    private static void completeDeliveriesFlow(LogisticsService service, List<DeliveryPlan> currentPlans) {
        List<DeliveryPlan> plansToComplete = (currentPlans != null) ? currentPlans : service.planDeliveries();
        if (plansToComplete.isEmpty()) {
            System.out.println("Tamamlanacak aktif bir teslimat plani veya bekleyen paket yok.");
            return;
        }
        int count = service.completeDeliveries(plansToComplete);
        System.out.println(count + " adet paketin teslimati tamamlandi ve dosya kayitlari güncellendi.");
    }

    /**
     * İlk çalistirmada veri dosyalari bossa sistemi test edebilmek için örnek araç ve paket verileri olusturur.
     */
    private static void seedSampleDataIfEmpty(LogisticsService service) {
        if (!service.getVehicles().isEmpty() || !service.getPackages().isEmpty()) {
            return;
        }

        service.addVehicle(VehicleFactory.createVehicle("VAN", "39LOG101", 900.0));
        service.addVehicle(VehicleFactory.createVehicle("TRUCK", "39LOG202", 2500.0));
        service.addVehicle(VehicleFactory.createVehicle("REFRIGERATED", "39LOG303", 1500.0, -4.0));

        service.addPackage(new ShipmentPackage("PKT-101", "Devlet Hastanesi",
                new Location("Lüleburgaz", 35.0, 15.0), 300.0, true, true));
        service.addPackage(new ShipmentPackage("PKT-102", "Merkez Market",
                new Location("Babaeski", 20.0, 10.0), 600.0, false, true));
        service.addPackage(new ShipmentPackage("PKT-103", "Organize Sanayi",
                new Location("Vize", 45.0, 30.0), 1400.0, true, false));
        service.addPackage(new ShipmentPackage("PKT-104", "Teknoloji Magazasi",
                new Location("Üsküp", 18.0, 4.0), 350.0, true, false));
        service.addPackage(new ShipmentPackage("PKT-105", "Kampüs Kirtasiye",
                new Location("Kayali Kampüsü", 6.0, 2.0), 200.0, false, false));
        service.addPackage(new ShipmentPackage("PKT-106", "Yapi Market",
                new Location("Pinarhisar", 28.0, 22.0), 900.0, false, false));
        service.addPackage(new ShipmentPackage("PKT-107", "Tarim Kooperatifi",
                new Location("Kofçaz", 8.0, 20.0), 250.0, false, false));
    }
}
