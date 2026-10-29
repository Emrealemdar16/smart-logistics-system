package logistics.factory;

import logistics.model.RefrigeratedTruck;
import logistics.model.Truck;
import logistics.model.Van;
import logistics.model.Vehicle;

import java.util.Locale;

/**
 * Factory Tasarim Kalibi: Araç türü koduna göre uygun Vehicle alt sinifi nesnesini üretir.
 */
public class VehicleFactory {

    private VehicleFactory() {
        // Yardimci fabrika sinifindan nesne üretilmesini engeller
    }

    public static Vehicle createVehicle(String type, String plateNumber, double maxWeightKg) {
        return createVehicle(type, plateNumber, maxWeightKg, RefrigeratedTruck.DEFAULT_TARGET_TEMP);
    }

    public static Vehicle createVehicle(String type, String plateNumber,
                                        double maxWeightKg, double targetTempCelsius) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Araç tipi bos olamaz.");
        }
        String normalizedType = type.trim().toUpperCase(Locale.ROOT);
        switch (normalizedType) {
            case "VAN":
                return new Van(plateNumber, maxWeightKg);
            case "TRUCK":
                return new Truck(plateNumber, maxWeightKg);
            case "REFRIGERATED":
                return new RefrigeratedTruck(plateNumber, maxWeightKg, targetTempCelsius);
            default:
                throw new IllegalArgumentException("Geçersiz araç tipi: " + type);
        }
    }
}
