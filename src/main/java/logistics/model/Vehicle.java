package logistics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Filodaki tüm teslimat araçlarinin ortak özellik ve davranislarini tanimlayan soyut üst sinif.
 */
public abstract class Vehicle {
    public static final double DEFAULT_FUEL_PRICE = 42.50;

    private final String plateNumber;
    private final double maxWeightKg;
    private double currentLoadKg;
    private final List<ShipmentPackage> loadedPackages;

    protected Vehicle(String plateNumber, double maxWeightKg) {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Plaka bilgisi bos olamaz.");
        }
        if (plateNumber.contains(";")) {
            throw new IllegalArgumentException("Plaka ';' karakteri içeremez.");
        }
        if (maxWeightKg <= 0) {
            throw new IllegalArgumentException("Araç kapasitesi 0'dan büyük olmalidir: " + maxWeightKg);
        }
        this.plateNumber = plateNumber.trim().toUpperCase(Locale.ROOT);
        this.maxWeightKg = maxWeightKg;
        this.currentLoadKg = 0.0;
        this.loadedPackages = new ArrayList<>();
    }

    /**
     * Standart araçlar soguk zincir gerektirmeyen ve kalan kapasiteyi asmayan paketleri tasiyabilir.
     * Özel donanimli alt siniflar bu kurali ezebilir.
     */
    public boolean canCarry(ShipmentPackage pkg) {
        if (pkg == null || pkg.isDelivered()) {
            return false;
        }
        if (pkg.isColdChainRequired()) {
            return false;
        }
        return currentLoadKg + pkg.getWeightKg() <= maxWeightKg;
    }

    public void loadPackage(ShipmentPackage pkg) {
        if (!canCarry(pkg)) {
            throw new IllegalArgumentException(
                    "Paket bu araca yüklenemez (kapasite veya donanim uyumsuz): " + plateNumber);
        }
        loadedPackages.add(pkg);
        currentLoadKg += pkg.getWeightKg();
    }

    public void clearLoad() {
        loadedPackages.clear();
        currentLoadKg = 0.0;
    }

    public double getRemainingCapacityKg() {
        return maxWeightKg - currentLoadKg;
    }

    // Metot asiri yükleme (Overloading): yakit birim fiyati verilmezse varsayilan fiyati kullanir
    public double calculateDeliveryCost(double distanceKm) {
        return calculateDeliveryCost(distanceKm, DEFAULT_FUEL_PRICE);
    }

    public abstract double calculateDeliveryCost(double distanceKm, double fuelPricePerLiter);

    public abstract double getFuelConsumptionPerKm();

    public abstract String getVehicleType();

    public String getPlateNumber() {
        return plateNumber;
    }

    public double getMaxWeightKg() {
        return maxWeightKg;
    }

    public double getCurrentLoadKg() {
        return currentLoadKg;
    }

    public List<ShipmentPackage> getLoadedPackages() {
        return Collections.unmodifiableList(loadedPackages);
    }

    protected void validateCostParameters(double distanceKm, double fuelPricePerLiter) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Mesafe negatif olamaz.");
        }
        if (fuelPricePerLiter <= 0) {
            throw new IllegalArgumentException("Yakit fiyati 0'dan büyük olmalidir.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vehicle other = (Vehicle) o;
        return plateNumber.equals(other.plateNumber);
    }

    @Override
    public int hashCode() {
        return plateNumber.hashCode();
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s [Plaka: %s | Yük: %.1f / %.1f kg | Tüketim: %.2f L/km]",
                getVehicleType(), plateNumber, currentLoadKg, maxWeightKg, getFuelConsumptionPerKm());
    }
}
