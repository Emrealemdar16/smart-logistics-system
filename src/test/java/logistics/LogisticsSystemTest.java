package logistics;

import logistics.factory.VehicleFactory;
import logistics.model.DeliveryPlan;
import logistics.model.Location;
import logistics.model.RefrigeratedTruck;
import logistics.model.ShipmentPackage;
import logistics.model.Truck;
import logistics.model.Van;
import logistics.model.Vehicle;
import logistics.repository.DataRepository;
import logistics.repository.FilePackageRepository;
import logistics.repository.FileVehicleRepository;
import logistics.service.LogisticsService;
import logistics.strategy.NearestNeighborRouteStrategy;
import logistics.strategy.PriorityFirstRouteStrategy;
import logistics.strategy.RouteStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Akilli Lojistik Sistemi birim testleri (JUnit 5).
 */
public class LogisticsSystemTest {

    @TempDir
    Path tempDir;

    // 1. Test: Factory tasarim kalibinin dogru alt sinif nesnelerini üretmesi
    @Test
    void testVehicleFactoryCreatesCorrectSubclasses() {
        Vehicle van = VehicleFactory.createVehicle("VAN", "39AA101", 900.0);
        Vehicle truck = VehicleFactory.createVehicle("TRUCK", "39BB202", 2500.0);
        Vehicle fridge = VehicleFactory.createVehicle("REFRIGERATED", "39CC303", 1400.0, -5.0);

        assertTrue(van instanceof Van);
        assertTrue(truck instanceof Truck);
        assertTrue(fridge instanceof RefrigeratedTruck);
        assertThrows(IllegalArgumentException.class,
                () -> VehicleFactory.createVehicle("PLANE", "39DD404", 1000.0));
    }

    // 2. Test: Kapsülleme dogrulamasi - geçersiz agirlik ile paket olusturulamamasi
    @Test
    void testInvalidPackageWeightThrowsException() {
        Location dest = new Location("Babaeski", 20.0, 10.0);
        assertThrows(IllegalArgumentException.class,
                () -> new ShipmentPackage("PKT-01", "Market", dest, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new ShipmentPackage("PKT-02", "Market", dest, -15.0));
    }

    // 3. Test: Soguk zincir gerektiren paketin yalnizca sogutuculu araca yüklenebilmesi (LSP ve Polimorfizm)
    @Test
    void testColdChainPackageAcceptedOnlyByRefrigeratedTruck() {
        Location dest = new Location("Lüleburgaz", 35.0, 15.0);
        ShipmentPackage coldPkg = new ShipmentPackage("PKT-COLD", "Hastane", dest, 200.0, true, true);

        Vehicle van = new Van("39VAN01", 1000.0);
        Vehicle truck = new Truck("39TRK01", 3000.0);
        Vehicle refrigerated = new RefrigeratedTruck("39REF01", 1500.0, -4.0);

        assertFalse(van.canCarry(coldPkg));
        assertFalse(truck.canCarry(coldPkg));
        assertTrue(refrigerated.canCarry(coldPkg));
    }

    // 4. Test: Araç maksimum yük kapasitesi sinirinin korunmasi
    @Test
    void testVehicleCapacityLimitEnforcement() {
        Vehicle van = new Van("39VAN02", 500.0);
        Location dest = new Location("Merkez", 10.0, 10.0);
        ShipmentPackage pkg1 = new ShipmentPackage("PKT-1", "Alici 1", dest, 350.0);
        ShipmentPackage pkg2 = new ShipmentPackage("PKT-2", "Alici 2", dest, 200.0);

        van.loadPackage(pkg1);
        assertEquals(150.0, van.getRemainingCapacityKg(), 0.001);
        assertFalse(van.canCarry(pkg2));
        assertThrows(IllegalArgumentException.class, () -> van.loadPackage(pkg2));
    }

    // 5. Test: Çok biçimlilik ile araç türüne göre farkli teslimat maliyeti hesaplanmasi
    @Test
    void testPolymorphicDeliveryCostCalculation() {
        Vehicle van = new Van("39VAN03", 1000.0);
        Vehicle truck = new Truck("39TRK03", 3000.0);
        Vehicle refrigerated = new RefrigeratedTruck("39REF03", 1500.0, -4.0);

        double vanCost = van.calculateDeliveryCost(100.0, 40.0);
        double truckCost = truck.calculateDeliveryCost(100.0, 40.0);
        double fridgeCost = refrigerated.calculateDeliveryCost(100.0, 40.0);

        assertTrue(vanCost < truckCost);
        assertTrue(truckCost < fridgeCost);
    }

    // 6. Test: NearestNeighborRouteStrategy (En Yakin Komsu) rota siralamasi ve toplam mesafe hesabi
    @Test
    void testNearestNeighborRouteOptimizationOrder() {
        Location depot = new Location("Merkez Depo", 0.0, 0.0);
        Location farLoc = new Location("Uzak Nokta", 30.0, 40.0);   // Depoya uzaklik: 50 km
        Location nearLoc = new Location("Yakin Nokta", 3.0, 4.0);   // Depoya uzaklik: 5 km

        ShipmentPackage pkgFar = new ShipmentPackage("PKT-FAR", "Alici A", farLoc, 100.0, true, false);
        ShipmentPackage pkgNear = new ShipmentPackage("PKT-NEAR", "Alici B", nearLoc, 100.0, false, false);

        RouteStrategy strategy = new NearestNeighborRouteStrategy();
        List<Location> route = strategy.optimizeRoute(depot, Arrays.asList(pkgFar, pkgNear));

        assertEquals(4, route.size());
        assertEquals(depot, route.get(0));
        assertEquals(nearLoc, route.get(1));
        assertEquals(farLoc, route.get(2));
        assertEquals(depot, route.get(3));
        assertTrue(strategy.calculateTotalDistance(route) > 55.0);
    }

    // 7. Test: PriorityFirstRouteStrategy acil paketin duragina daha uzakta olsa bile önce gitmesi
    @Test
    void testPriorityFirstRouteOptimizationOrder() {
        Location depot = new Location("Merkez Depo", 0.0, 0.0);
        Location farUrgentLoc = new Location("Acil Uzak", 30.0, 40.0);
        Location nearNormalLoc = new Location("Normal Yakin", 3.0, 4.0);

        ShipmentPackage pkgNormal = new ShipmentPackage("PKT-N", "Alici N", nearNormalLoc, 50.0, false, false);
        ShipmentPackage pkgUrgent = new ShipmentPackage("PKT-U", "Alici U", farUrgentLoc, 50.0, true, false);

        RouteStrategy strategy = new PriorityFirstRouteStrategy();
        List<Location> route = strategy.optimizeRoute(depot, Arrays.asList(pkgNormal, pkgUrgent));

        assertEquals(4, route.size());
        assertEquals(farUrgentLoc, route.get(1));
        assertEquals(nearNormalLoc, route.get(2));
    }

    // 8. Test: Kapasiteye göre paket dagitim algoritmasi ve kapasite asan paketin açikta kalmasi
    @Test
    void testCapacityBasedPackageDistributionAlgorithm() {
        DataRepository<Vehicle> vRepo = new FileVehicleRepository(tempDir.resolve("v1.txt").toString());
        DataRepository<ShipmentPackage> pRepo = new FilePackageRepository(tempDir.resolve("p1.txt").toString());
        LogisticsService service = new LogisticsService(vRepo, pRepo, new NearestNeighborRouteStrategy());

        service.addVehicle(new Van("39VAN10", 500.0));
        service.addVehicle(new RefrigeratedTruck("39REF10", 600.0, -4.0));

        Location loc = new Location("Vize", 45.0, 30.0);
        service.addPackage(new ShipmentPackage("PKT-1", "Eczane", loc, 400.0, true, true));
        service.addPackage(new ShipmentPackage("PKT-2", "Kirtasiye", loc, 450.0, false, false));
        service.addPackage(new ShipmentPackage("PKT-3", "Fabrika", loc, 2000.0, false, false)); // Kapasite üstü

        List<DeliveryPlan> plans = service.planDeliveries();

        assertEquals(2, plans.size());
        assertEquals(1, service.getLastUnassignedPackages().size());
        assertEquals("PKT-3", service.getLastUnassignedPackages().get(0).getPackageId());
    }

    // 9. Test: Teslimatlarin tamamlanmasiyla paket durumlarinin güncellenmesi
    @Test
    void testCompleteDeliveriesUpdatesPackageStatus() {
        DataRepository<Vehicle> vRepo = new FileVehicleRepository(tempDir.resolve("v2.txt").toString());
        DataRepository<ShipmentPackage> pRepo = new FilePackageRepository(tempDir.resolve("p2.txt").toString());
        LogisticsService service = new LogisticsService(vRepo, pRepo, new NearestNeighborRouteStrategy());

        service.addVehicle(new Van("39VAN20", 800.0));
        service.addPackage("PKT-100", "Magaza", new Location("Merkez", 5.0, 5.0), 300.0);

        List<DeliveryPlan> plans = service.planDeliveries();
        int completed = service.completeDeliveries(plans);

        assertEquals(1, completed);
        assertTrue(service.getPackages().get(0).isDelivered());
        assertTrue(service.getPendingPackages().isEmpty());
    }

    // 10. Test: Araç ve paket verilerinin dosyaya kalici olarak yazilip tekrar okunmasi
    @Test
    void testFileRepositoryPersistence() {
        String vFile = tempDir.resolve("vehicles_persist.txt").toString();
        String pFile = tempDir.resolve("packages_persist.txt").toString();

        FileVehicleRepository vRepo = new FileVehicleRepository(vFile);
        FilePackageRepository pRepo = new FilePackageRepository(pFile);

        Vehicle savedTruck = new RefrigeratedTruck("39PRS99", 1800.0, -6.0);
        ShipmentPackage savedPkg = new ShipmentPackage("PKT-999", "Gida As",
                new Location("Pinarhisar", 28.0, 22.0), 550.0, true, true);

        vRepo.saveAll(Arrays.asList(savedTruck));
        pRepo.saveAll(Arrays.asList(savedPkg));

        List<Vehicle> loadedVehicles = vRepo.findAll();
        List<ShipmentPackage> loadedPackages = pRepo.findAll();

        assertEquals(1, loadedVehicles.size());
        assertEquals(savedTruck, loadedVehicles.get(0));
        assertEquals(-6.0, ((RefrigeratedTruck) loadedVehicles.get(0)).getTargetTemperatureCelsius(), 0.001);

        assertEquals(1, loadedPackages.size());
        assertEquals(savedPkg, loadedPackages.get(0));
        assertTrue(loadedPackages.get(0).isColdChainRequired());
    }
}
