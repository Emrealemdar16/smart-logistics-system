package logistics.strategy;

import logistics.model.Location;
import logistics.model.ShipmentPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Önce acil (urgent) paketlerin duraklarini, ardindan standart paketlerin duraklarini yakinlik sirasina göre ziyaret eder.
 */
public class PriorityFirstRouteStrategy implements RouteStrategy {

    @Override
    public List<Location> optimizeRoute(Location depot, List<ShipmentPackage> packages) {
        if (depot == null) {
            throw new IllegalArgumentException("Depo konumu bos olamaz.");
        }
        if (packages == null || packages.isEmpty()) {
            return Collections.emptyList();
        }

        List<Location> urgentStops = new ArrayList<>();
        for (ShipmentPackage pkg : packages) {
            if (pkg.isUrgent()) {
                Location dest = pkg.getDestination();
                if (!urgentStops.contains(dest)) {
                    urgentStops.add(dest);
                }
            }
        }

        List<Location> normalStops = new ArrayList<>();
        for (ShipmentPackage pkg : packages) {
            if (!pkg.isUrgent()) {
                Location dest = pkg.getDestination();
                if (!urgentStops.contains(dest) && !normalStops.contains(dest)) {
                    normalStops.add(dest);
                }
            }
        }

        List<Location> route = new ArrayList<>();
        route.add(depot);
        Location current = depot;

        current = appendNearestStops(current, urgentStops, route);
        appendNearestStops(current, normalStops, route);

        route.add(depot);
        return route;
    }

    private Location appendNearestStops(Location start, List<Location> stops, List<Location> route) {
        Location current = start;
        while (!stops.isEmpty()) {
            Location nearest = stops.get(0);
            double minDistance = current.distanceTo(nearest);
            for (int i = 1; i < stops.size(); i++) {
                Location candidate = stops.get(i);
                double dist = current.distanceTo(candidate);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = candidate;
                }
            }
            route.add(nearest);
            stops.remove(nearest);
            current = nearest;
        }
        return current;
    }

    @Override
    public String getStrategyName() {
        return "Öncelik Odakli Rota (Önce Acil Paketler)";
    }
}
