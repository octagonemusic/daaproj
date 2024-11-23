package com.octagone.daaprojbackend.services;

import com.octagone.daaprojbackend.models.Connection;
import com.octagone.daaprojbackend.models.Source;
import com.octagone.daaprojbackend.models.Zone;
import com.octagone.daaprojbackend.models.ZoneAllocation;
import com.octagone.daaprojbackend.models.AllocationResponse;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WaterAllocationServiceImpl {

    private static final double EPSILON = 1e-9;
    private static final Logger logger = LoggerFactory.getLogger(WaterAllocationServiceImpl.class);

    public AllocationResponse allocateWater(List<Source> sources, List<Zone> zones, List<Connection> connections) {
        logger.info("Starting water allocation process");
        // Null check for inputs
        if (sources == null || zones == null || connections == null) {
            throw new IllegalArgumentException("Sources, Zones, and Connections cannot be null.");
        }

        validateInputs(sources, zones, connections);

        // Create maps for faster access
        Map<Long, Source> sourceMap = sources.stream().collect(Collectors.toMap(Source::getId, s -> s));
        Map<Long, Zone> zoneMap = zones.stream().collect(Collectors.toMap(Zone::getId, z -> z));
        Map<Long, List<Connection>> connectionsByZone = connections.stream()
                .collect(Collectors.groupingBy(c -> c.getZone().getId()));

        // Stage 1: Weighted Greedy Allocation
        PriorityQueue<Connection> connectionQueue = new PriorityQueue<>(
                Comparator.comparingDouble(Connection::getWeightedCost));
        connections.forEach(conn -> {
            conn.setWeightedCost(conn.getCostPerUnit() / conn.getZone().getPriority());
            connectionQueue.offer(conn);
        });

        Map<Long, ZoneAllocation> allocationMap = new HashMap<>();
        Set<Long> availableSources = new HashSet<>(sourceMap.keySet());
        Set<Long> unmetDemands = new HashSet<>(zoneMap.keySet());

        while (!connectionQueue.isEmpty() && !unmetDemands.isEmpty() && !availableSources.isEmpty()) {
            Connection conn = connectionQueue.poll();
            Source source = sourceMap.get(conn.getSource().getId());
            Zone zone = zoneMap.get(conn.getZone().getId());

            if (availableSources.contains(source.getId()) && zone.getDemand() > EPSILON) {
                double allocation = Math.min(zone.getDemand(), source.getCapacity());
                updateAllocation(allocationMap, zone.getId(), allocation, conn.getId());
                source.setCapacity(source.getCapacity() - allocation);
                zone.setDemand(zone.getDemand() - allocation);
                zone.addFilledCapacity(allocation);
                logger.debug("Allocated {} units from source {} to zone {} through connection {}",
                        allocation, source.getId(), zone.getId(), conn.getId());

                // Remove source if depleted
                if (source.getCapacity() <= EPSILON) {
                    availableSources.remove(source.getId());
                }

                // Remove zone if demand is met
                if (zone.getDemand() <= EPSILON) {
                    unmetDemands.remove(zone.getId());
                }
            }
        }

        // Stage 2: Backtracking Adjustment
        adjustAllocationsForUnmetDemand(allocationMap, zones, connectionsByZone, sourceMap);

        logger.info("Water allocation process completed");
        return new AllocationResponse(new ArrayList<>(allocationMap.values()));
    }

    private void validateInputs(List<Source> sources, List<Zone> zones, List<Connection> connections) {
        boolean invalidSource = sources.parallelStream().anyMatch(s -> s.getCapacity() < 0);
        boolean invalidZone = zones.parallelStream().anyMatch(z -> z.getDemand() < 0 || z.getPriority() <= 0);
        boolean invalidConnection = connections.parallelStream().anyMatch(c -> c.getCostPerUnit() < 0);

        if (invalidSource || invalidZone || invalidConnection) {
            throw new IllegalArgumentException("Invalid input found");
        }
    }

    private void adjustAllocationsForUnmetDemand(Map<Long, ZoneAllocation> allocationMap, List<Zone> zones,
            Map<Long, List<Connection>> connectionsByZone, Map<Long, Source> sourceMap) {
        for (Zone zone : zones) {
            double unmetDemand = zone.getDemand();
            if (unmetDemand > EPSILON) {
                List<Connection> zoneConnections = connectionsByZone.get(zone.getId());
                for (Connection conn : zoneConnections) {
                    Source source = sourceMap.get(conn.getSource().getId());
                    double availableCapacity = source.getCapacity();

                    // If the source has no available capacity, skip to the next connection
                    if (availableCapacity <= EPSILON) {
                        continue;
                    }

                    // Calculate the additional allocation
                    double additionalAllocation = Math.min(unmetDemand, availableCapacity);

                    // Update the allocation map only if there is an actual allocation
                    if (additionalAllocation > EPSILON) {
                        updateAllocation(allocationMap, zone.getId(), additionalAllocation, conn.getId());

                        // Update the source capacity and zone demand
                        source.setCapacity(availableCapacity - additionalAllocation);
                        zone.addFilledCapacity(additionalAllocation);
                        unmetDemand -= additionalAllocation;

                        logger.debug("Adjusted allocation: {} units from source {} to zone {} through connection {}",
                                additionalAllocation, source.getId(), zone.getId(), conn.getId());

                        // Early exit if the demand is met
                        if (unmetDemand <= EPSILON) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void updateAllocation(Map<Long, ZoneAllocation> allocationMap, Long zoneId, double amount,
            Long connectionId) {
        ZoneAllocation allocation = allocationMap.computeIfAbsent(zoneId, ZoneAllocation::new);
        allocation.addConnectionAllocation(connectionId, amount);
    }
}
