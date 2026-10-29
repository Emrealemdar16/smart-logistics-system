package logistics.model;

import java.util.Locale;
import java.util.Objects;

/**
 * Teslimat noktalarinin ve merkez deponun düzlem üzerindeki (x, y) km koordinatlarini tutar.
 */
public class Location {
    private final String name;
    private final double x;
    private final double y;

    public Location(String name, double x, double y) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Konum adi bos olamaz.");
        }
        if (name.contains(";")) {
            throw new IllegalArgumentException("Konum adi ';' karakteri içeremez.");
        }
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y)) {
            throw new IllegalArgumentException("Geçersiz koordinat degeri.");
        }
        this.name = name.trim();
        this.x = x;
        this.y = y;
    }

    // Kopya yapici: savunmaci kopya olusturmak için kullanilir
    public Location(Location other) {
        this(requireLocation(other).name, other.x, other.y);
    }

    private static Location requireLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Konum nesnesi null olamaz.");
        }
        return location;
    }

    public double distanceTo(Location other) {
        if (other == null) {
            throw new IllegalArgumentException("Hedef konum null olamaz.");
        }
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Location other = (Location) o;
        return Double.compare(other.x, x) == 0
                && Double.compare(other.y, y) == 0
                && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, x, y);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s (%.1f, %.1f)", name, x, y);
    }
}
