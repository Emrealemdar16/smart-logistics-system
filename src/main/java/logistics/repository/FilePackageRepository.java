package logistics.repository;

import logistics.model.Location;
import logistics.model.ShipmentPackage;

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
 * Paket kayitlarini metin dosyasinda (UTF-8) kalici olarak saklayan ve okuyan sinif.
 */
public class FilePackageRepository implements DataRepository<ShipmentPackage> {
    private final Path filePath;

    public FilePackageRepository(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Dosya yolu bos olamaz.");
        }
        this.filePath = Paths.get(filePath);
    }

    @Override
    public List<ShipmentPackage> findAll() {
        List<ShipmentPackage> packages = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return packages;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split(";");
                if (parts.length >= 9) {
                    String id = parts[0].trim();
                    String recipient = parts[1].trim();
                    String locName = parts[2].trim();
                    double x = Double.parseDouble(parts[3].trim());
                    double y = Double.parseDouble(parts[4].trim());
                    double weight = Double.parseDouble(parts[5].trim());
                    boolean urgent = Boolean.parseBoolean(parts[6].trim());
                    boolean coldChain = Boolean.parseBoolean(parts[7].trim());
                    boolean delivered = Boolean.parseBoolean(parts[8].trim());

                    Location destination = new Location(locName, x, y);
                    packages.add(new ShipmentPackage(id, recipient, destination, weight, urgent, coldChain, delivered));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Paket dosyasi okunamadi: " + e.getMessage(), e);
        }
        return packages;
    }

    @Override
    public void saveAll(List<ShipmentPackage> items) {
        if (items == null) {
            throw new IllegalArgumentException("Kaydedilecek paket listesi bos olamaz.");
        }
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                for (ShipmentPackage pkg : items) {
                    Location dest = pkg.getDestination();
                    String record = String.format(Locale.US, "%s;%s;%s;%.2f;%.2f;%.2f;%b;%b;%b",
                            pkg.getPackageId(),
                            pkg.getRecipientName(),
                            dest.getName(),
                            dest.getX(),
                            dest.getY(),
                            pkg.getWeightKg(),
                            pkg.isUrgent(),
                            pkg.isColdChainRequired(),
                            pkg.isDelivered());
                    writer.write(record);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Paket dosyasi kaydedilemedi: " + e.getMessage(), e);
        }
    }
}
