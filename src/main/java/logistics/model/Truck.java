package logistics.model;

/**
 * Yüksek agirlikli yüklerin tasinmasinda kullanilan kamyon sinifi.
 */
public class Truck extends Vehicle {
    public static final double DEFAULT_CAPACITY_KG = 3000.0;
    public static final double BASE_FUEL_PER_KM = 0.24;

    public Truck(String plateNumber, double maxWeightKg) {
        super(plateNumber, maxWeightKg);
    }

    public Truck(String plateNumber) {
        this(plateNumber, DEFAULT_CAPACITY_KG);
    }

    @Override
    public double calculateDeliveryCost(double distanceKm, double fuelPricePerLiter) {
        validateCostParameters(distanceKm, fuelPricePerLiter);
        double loadRatio = getCurrentLoadKg() / getMaxWeightKg();
        double consumedLiters = distanceKm * BASE_FUEL_PER_KM * (1.0 + 0.35 * loadRatio);
        return consumedLiters * fuelPricePerLiter;
    }

    @Override
    public double getFuelConsumptionPerKm() {
        return BASE_FUEL_PER_KM;
    }

    @Override
    public String getVehicleType() {
        return "TRUCK";
    }
}
