package logistics.strategy;

import logistics.model.Location;
import logistics.model.ShipmentPackage;

import java.util.List;

/**
 * Strategy Tasarim Kalibi: Farkli teslimat rotasi optimizasyon algoritmalari için ortak sözlesme.
 */
public interface RouteStrategy {

    List<Location> optimizeRoute(Location depot, List<ShipmentPackage> packages);

    String getStrategyName();

    default double calculateTotalDistance(List<Location> routeStops) {
        if (routeStops == null || routeStops.size() < 2) {
            return 0.0;
        }
        double totalDistance = 0.0;
        for (int i = 0; i < routeStops.size() - 1; i++) {
            totalDistance += routeStops.get(i).distanceTo(routeStops.get(i + 1));
        }
        return totalDistance;
    }
}
