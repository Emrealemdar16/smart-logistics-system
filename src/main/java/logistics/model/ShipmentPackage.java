package logistics.model;

import java.util.Locale;

/**
 * Tasinacak kargo paketlerinin kimlik, agirlik, hedef konum ve teslimat özelliklerini tutar.
 */
public class ShipmentPackage {
    private final String packageId;
    private final String recipientName;
    private final Location destination;
    private final double weightKg;
    private final boolean urgent;
    private final boolean coldChainRequired;
    private boolean delivered;

    // Ana yapici: tüm dogrulamalar tek noktada yapilir
    public ShipmentPackage(String packageId, String recipientName, Location destination,
                           double weightKg, boolean urgent, boolean coldChainRequired, boolean delivered) {
        if (packageId == null || packageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Paket kodu bos olamaz.");
        }
        if (packageId.contains(";")) {
            throw new IllegalArgumentException("Paket kodu ';' karakteri içeremez.");
        }
        if (recipientName == null || recipientName.trim().isEmpty()) {
            throw new IllegalArgumentException("Alici adi bos olamaz.");
        }
        if (recipientName.contains(";")) {
            throw new IllegalArgumentException("Alici adi ';' karakteri içeremez.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Teslimat konumu bos olamaz.");
        }
        if (weightKg <= 0) {
            throw new IllegalArgumentException("Paket agirligi 0'dan büyük olmalidir: " + weightKg);
        }

        this.packageId = packageId.trim().toUpperCase(Locale.ROOT);
        this.recipientName = recipientName.trim();
        this.destination = new Location(destination); // Savunmaci kopya
        this.weightKg = weightKg;
        this.urgent = urgent;
        this.coldChainRequired = coldChainRequired;
        this.delivered = delivered;
    }

    // Yapici zincirleme: yeni olusturulan paket varsayilan olarak teslim edilmemistir
    public ShipmentPackage(String packageId, String recipientName, Location destination,
                           double weightKg, boolean urgent, boolean coldChainRequired) {
        this(packageId, recipientName, destination, weightKg, urgent, coldChainRequired, false);
    }

    // Yapici zincirleme: standart (acil olmayan ve soguk zincir gerektirmeyen) paket
    public ShipmentPackage(String packageId, String recipientName, Location destination, double weightKg) {
        this(packageId, recipientName, destination, weightKg, false, false, false);
    }

    public void markAsDelivered() {
        this.delivered = true;
    }

    public String getPackageId() {
        return packageId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public Location getDestination() {
        return new Location(destination);
    }

    public double getWeightKg() {
        return weightKg;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public boolean isColdChainRequired() {
        return coldChainRequired;
    }

    public boolean isDelivered() {
        return delivered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ShipmentPackage other = (ShipmentPackage) o;
        return packageId.equals(other.packageId);
    }

    @Override
    public int hashCode() {
        return packageId.hashCode();
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Paket[%s | Alici: %s | Hedef: %s | %.1f kg | %s | %s | Durum: %s]",
                packageId,
                recipientName,
                destination,
                weightKg,
                urgent ? "ACİL" : "Normal",
                coldChainRequired ? "Soguk Zincir" : "Standart",
                delivered ? "Teslim Edildi" : "Bekliyor");
    }
}
