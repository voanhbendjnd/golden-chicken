package vn.edu.fpt.golden_chicken.services;

import org.springframework.stereotype.Component;
import vn.edu.fpt.golden_chicken.domain.response.LocationMessage;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ShipperLocationStore {

    private final ConcurrentMap<Long, LocationMessage> latestLocations = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, PreviousShipperSnapshot> previousShipperSnapshots = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Set<Long>> reassignedHistoryByShipperId = new ConcurrentHashMap<>();

    public static class PreviousShipperSnapshot {
        private final String shipperName;
        private final Double lat;
        private final Double lng;
        private final String issueReason;

        public PreviousShipperSnapshot(String shipperName, Double lat, Double lng, String issueReason) {
            this.shipperName = shipperName;
            this.lat = lat;
            this.lng = lng;
            this.issueReason = issueReason;
        }

        public String getShipperName() {
            return shipperName;
        }

        public Double getLat() {
            return lat;
        }

        public Double getLng() {
            return lng;
        }

        public String getIssueReason() {
            return issueReason;
        }
    }

    public void save(LocationMessage message) {
        if (message == null || message.getOrderId() == null) {
            return;
        }
        latestLocations.put(message.getOrderId(), message);
    }

    public Optional<LocationMessage> getByOrderId(Long orderId) {
        return Optional.ofNullable(latestLocations.get(orderId));
    }

    public void savePreviousShipperSnapshot(Long orderId, String shipperName, Double lat, Double lng,
                                            String issueReason) {
        if (orderId == null) {
            return;
        }
        previousShipperSnapshots.put(orderId, new PreviousShipperSnapshot(shipperName, lat, lng, issueReason));
    }

    public Optional<PreviousShipperSnapshot> getPreviousShipperSnapshot(Long orderId) {
        return Optional.ofNullable(previousShipperSnapshots.get(orderId));
    }

    public void markReassignedOrderForShipper(Long shipperId, Long orderId) {
        if (shipperId == null || orderId == null) {
            return;
        }
        reassignedHistoryByShipperId
                .computeIfAbsent(shipperId, id -> ConcurrentHashMap.newKeySet())
                .add(orderId);
    }

    public Set<Long> getReassignedOrderIdsForShipper(Long shipperId) {
        if (shipperId == null) {
            return Collections.emptySet();
        }
        return reassignedHistoryByShipperId.getOrDefault(shipperId, Collections.emptySet());
    }
}