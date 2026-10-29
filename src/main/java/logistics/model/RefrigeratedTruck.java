package logistics.model;

import java.util.Locale;

/**
 * Soguk zincir gerektiren paketleri de tasiyabilen sogutuculu kamyon sinifi.
 */
public class RefrigeratedTruck extends Vehicle implements TemperatureControlled {
    public static final double DEFAULT_TARGET_TEMP = -4.0;
    public static final double BASE_FUEL_PER_KM = 0.28;
    public static final double COOLING_COST_MULTIPLIER = 1.20;

    private final double targetTemperatureCelsius;

    public RefrigeratedTruck(String plateNumber, double maxWeightKg, double targetTemperatureCelsius) {
        super(plateNumber, maxWeightKg);
        if (targetTemperatureCelsius > 10.0 || targetTemperatureCelsius < -30.0) {
            throw new IllegalArgumentException(
                    "Sogutucu sicakligi -30°C ile +10°C arasinda olmalidir: " + targetTemperatureCelsius);
        }
        this.targetTemperatureCelsius = targetTemperatureCelsius;
    }

    public RefrigeratedTruck(String plateNumber, double maxWeightKg) {
        this(plateNumber, maxWeightKg, DEFAULT_TARGET_TEMP);
    }

    @Override
    public boolean canCarry(ShipmentPackage pkg) {
        if (pkg == null || pkg.isDelivered()) {
            return false;
        }
        return getCurrentLoadKg() + pkg.getWeightKg() <= getMaxWeightKg();
    }

    @Override
    public double calculateDeliveryCost(double distanceKm, double fuelPricePerLiter) {
        validateCostParameters(distanceKm, fuelPricePerLiter);
        double loadRatio = getCurrentLoadKg() / getMaxWeightKg();
        double consumedLiters = distanceKm * BASE_FUEL_PER_KM * (1.0 + 0.30 * loadRatio);
        return consumedLiters * fuelPricePerLiter * COOLING_COST_MULTIPLIER;
    }

    @Override
    public double getFuelConsumptionPerKm() {
        return BASE_FUEL_PER_KM;
    }

    @Override
    public String getVehicleType() {
        return "REFRIGERATED";
    }

    @Override
    public double getTargetTemperatureCelsius() {
        return targetTemperatureCelsius;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(Locale.US, " (Sogutucu: %.1f°C)", targetTemperatureCelsius);
    }
}
