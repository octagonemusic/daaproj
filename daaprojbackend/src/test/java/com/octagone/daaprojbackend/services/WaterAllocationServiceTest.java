package com.octagone.daaprojbackend.services;

import com.octagone.daaprojbackend.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class WaterAllocationServiceTest {

    @Autowired
    private WaterAllocationServiceImpl waterAllocationService;

    private List<Source> sources;
    private List<Zone> zones;
    private List<Connection> connections;

    @BeforeEach
    void setUp() {
        sources = new ArrayList<>();
        zones = new ArrayList<>();
        connections = new ArrayList<>();

        // Create sources with sufficient capacity
        sources.add(new Source(1L, 200.0)); // Increased capacity
        sources.add(new Source(2L, 300.0)); // Increased capacity

        // Create zones with different priorities
        zones.add(new Zone(1L, 80.0, 1)); // Low priority
        zones.add(new Zone(2L, 120.0, 2)); // Medium priority
        zones.add(new Zone(3L, 100.0, 3)); // High priority

        // Create connections ensuring all zones are connected
        connections.add(new Connection(1L, sources.get(0), zones.get(0), 2.0));
        connections.add(new Connection(2L, sources.get(1), zones.get(1), 3.0));
        connections.add(new Connection(3L, sources.get(1), zones.get(2), 2.0)); // Connection for high priority zone
        connections.add(new Connection(4L, sources.get(0), zones.get(1), 1.0));
    }

    @Test
    void testBasicAllocation() {
        AllocationResponse response = waterAllocationService.allocateWater(sources, zones, connections);
        assertNotNull(response);
        assertNotNull(response.getAllocations());
        assertFalse(response.getAllocations().isEmpty());
    }

    @Test
    void testPriorityBasedAllocation() {
        AllocationResponse response = waterAllocationService.allocateWater(sources, zones, connections);

        System.out.println("\n=== Water Allocation Test Results ===");
        for (ZoneAllocation allocation : response.getAllocations()) {
            System.out.printf("Zone %d: Allocated %.2f units\n",
                    allocation.getZoneId(), allocation.getAmount());

            // Print connection allocations
            for (ConnectionAllocation connAlloc : allocation.getConnectionAllocations()) {
                System.out.printf("  - Connection %d: %.2f units\n",
                        connAlloc.getConnectionId(), connAlloc.getAmount());
            }
        }
        System.out.println("===================================\n");

        // Find allocations for different priority zones
        ZoneAllocation highPriorityAllocation = findAllocationForZone(response.getAllocations(), 3L);
        ZoneAllocation lowPriorityAllocation = findAllocationForZone(response.getAllocations(), 1L);

        assertNotNull(highPriorityAllocation, "High priority zone allocation should not be null");
        assertNotNull(lowPriorityAllocation, "Low priority zone allocation should not be null");

        // Calculate satisfaction rates
        double highPriorityFulfillment = highPriorityAllocation.getAmount() / 100.0;
        double lowPriorityFulfillment = lowPriorityAllocation.getAmount() / 80.0;

        assertTrue(highPriorityFulfillment >= lowPriorityFulfillment,
                "High priority zone should have better or equal demand fulfillment");
    }

    private ZoneAllocation findAllocationForZone(List<ZoneAllocation> allocations, Long zoneId) {
        return allocations.stream()
                .filter(a -> a.getZoneId().equals(zoneId))
                .findFirst()
                .orElse(null);
    }
}