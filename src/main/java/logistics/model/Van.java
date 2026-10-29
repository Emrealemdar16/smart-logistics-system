package logistics.model;

/**
 * sehir içi hafif paket dagitiminda kullanilan düsük yakit tüketimli panelvan sinifi.
 */
public class Van extends Vehicle {
    public static final double DEFAULT_CAPACITY_KG = 1000.0;
    public static final double BASE_FUEL_PER_KM = 0.09;

    public Van(String plateNumber, double maxWeightKg) {
        super(plateNumber, maxWeightKg);
    }

    public Van(String plateNumber) {
        this(plateNumber, DEFAULT_CAPACITY_KG);
    }

    @Override
    public double calculateDeliveryCost(double distanceKm, double fuelPricePerLiter) {
        validateCostParameters(distanceKm, fuelPricePerLiter);
        double loadRatio = getCurrentLoadKg() / getMaxWeightKg();
        double consumedLiters = distanceKm * BASE_FUEL_PER_KM * (1.0 + 0.15 * loadRatio);
        return consumedLiters * fuelPricePerLiter;
    }

    @Override
    public double getFuelConsumptionPerKm() {
        return BASE_FUEL_PER_KM;
    }

    @Override
    public String getVehicleType() {
        return "VAN";
    }
}
