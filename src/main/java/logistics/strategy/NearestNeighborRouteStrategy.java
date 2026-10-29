package logistics.strategy;

import logistics.model.Location;
import logistics.model.ShipmentPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * En Yakin Komsu (Nearest Neighbor) algoritmasi ile toplam yol mesafesini azaltacak sekilde rota olusturur.
 */
public class NearestNeighborRouteStrategy implements RouteStrategy {

    @Override
    public List<Location> optimizeRoute(Location depot, List<ShipmentPackage> packages) {
        if (depot == null) {
            throw new IllegalArgumentException("Depo konumu bos olamaz.");
        }
        if (packages == null || packages.isEmpty()) {
            return Collections.emptyList();
        }

        List<Location> unvisited = new ArrayList<>();
        for (ShipmentPackage pkg : packages) {
            Location dest = pkg.getDestination();
            if (!unvisited.contains(dest)) {
                unvisited.add(dest);
            }
        }

        List<Location> route = new ArrayList<>();
        route.add(depot);
        Location current = depot;

        while (!unvisited.isEmpty()) {
            Location nearest = findNearest(current, unvisited);
            route.add(nearest);
            unvisited.remove(nearest);
            current = nearest;
        }

        route.add(depot);
        return route;
    }

    private Location findNearest(Location current, List<Location> candidates) {
        Location nearest = candidates.get(0);
        double minDistance = current.distanceTo(nearest);

        for (int i = 1; i < candidates.size(); i++) {
            Location candidate = candidates.get(i);
            double distance = current.distanceTo(candidate);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    @Override
    public String getStrategyName() {
        return "En Yakin Komsu (Nearest Neighbor)";
    }
}
