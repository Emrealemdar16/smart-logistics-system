package logistics.repository;

import logistics.factory.VehicleFactory;
import logistics.model.TemperatureControlled;
import logistics.model.Vehicle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Araç kayitlarini metin dosyasinda (UTF-8) kalici olarak saklayan ve okuyan sinif.
 */
public class FileVehicleRepository implements DataRepository<Vehicle> {
    private final Path filePath;

    public FileVehicleRepository(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Dosya yolu bos olamaz.");
        }
        this.filePath = Paths.get(filePath);
    }

    @Override
    public List<Vehicle> findAll() {
        List<Vehicle> vehicles = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return vehicles;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split(";");
                if (parts.length >= 3) {
                    String type = parts[0].trim();
                    String plate = parts[1].trim();
                    double maxWeight = Double.parseDouble(parts[2].trim());
                    double targetTemp = (parts.length >= 4) ? Double.parseDouble(parts[3].trim()) : -4.0;
                    vehicles.add(VehicleFactory.createVehicle(type, plate, maxWeight, targetTemp));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Araç dosyasi okunamadi: " + e.getMessage(), e);
        }
        return vehicles;
    }

    @Override
    public void saveAll(List<Vehicle> items) {
        if (items == null) {
            throw new IllegalArgumentException("Kaydedilecek araç listesi bos olamaz.");
        }
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                for (Vehicle vehicle : items) {
                    double targetTemp = 0.0;
                    if (vehicle instanceof TemperatureControlled) {
                        targetTemp = ((TemperatureControlled) vehicle).getTargetTemperatureCelsius();
                    }
                    String record = String.format(Locale.US, "%s;%s;%.2f;%.1f",
                            vehicle.getVehicleType(),
                            vehicle.getPlateNumber(),
                            vehicle.getMaxWeightKg(),
                            targetTemp);
                    writer.write(record);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Araç dosyasi kaydedilemedi: " + e.getMessage(), e);
        }
    }
}
