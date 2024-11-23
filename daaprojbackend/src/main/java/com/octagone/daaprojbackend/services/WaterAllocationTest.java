package com.octagone.daaprojbackend.services;

import com.octagone.daaprojbackend.models.Connection;
import com.octagone.daaprojbackend.models.Source;
import com.octagone.daaprojbackend.models.Zone;
import com.octagone.daaprojbackend.models.ZoneAllocation;
import com.octagone.daaprojbackend.models.AllocationResponse;

import java.util.ArrayList;
import java.util.List;

public class WaterAllocationTest {

    public static void main(String[] args) {
        WaterAllocationServiceImpl service = new WaterAllocationServiceImpl();

        // Sample Sources
        List<Source> sources = new ArrayList<>();
        sources.add(new Source(1L, 100)); // Source 1 with 100 units capacity
        sources.add(new Source(2L, 150)); // Source 2 with 150 units capacity
        sources.add(new Source(3L, 200)); // Source 3 with 200 units capacity

        // Sample Zones
        List<Zone> zones = new ArrayList<>();
        zones.add(new Zone(1L, 80, 1)); // Zone 1 with 80 units demand and priority 1
        zones.add(new Zone(2L, 120, 2)); // Zone 2 with 120 units demand and priority 2
        zones.add(new Zone(3L, 150, 3)); // Zone 3 with 150 units demand and priority 3
        zones.add(new Zone(4L, 90, 1)); // Zone 4 with 90 units demand and priority 1

        // Sample Connections
        List<Connection> connections = new ArrayList<>();
        connections.add(new Connection(1L, sources.get(0), zones.get(0), 2)); // Source 1 to Zone 1
        connections.add(new Connection(2L, sources.get(1), zones.get(1), 3)); // Source 2 to Zone 2
        connections.add(new Connection(3L, sources.get(0), zones.get(1), 1)); // Source 1 to Zone 2
        connections.add(new Connection(4L, sources.get(2), zones.get(2), 2)); // Source 3 to Zone 3
        connections.add(new Connection(5L, sources.get(1), zones.get(3), 1)); // Source 2 to Zone 4
        connections.add(new Connection(6L, sources.get(2), zones.get(0), 3)); // Source 3 to Zone 1
        connections.add(new Connection(7L, sources.get(2), zones.get(1), 2)); // Source 3 to Zone 2

        // Execute water allocation
        AllocationResponse response = service.allocateWater(sources, zones, connections);

        // Print results
        System.out.println("Water Allocation Results:");
        for (ZoneAllocation allocation : response.getAllocations()) {
            System.out.println("Zone ID: " + allocation.getZoneId() + ", Allocated Amount: " + allocation.getAmount());
        }

        // Print remaining capacities and demands
        System.out.println("\nRemaining Source Capacities:");
        for (Source source : sources) {
            System.out.println("Source ID: " + source.getId() + ", Remaining Capacity: " + source.getCapacity());
        }

        System.out.println("\nRemaining Zone Demands:");
        for (Zone zone : zones) {
            System.out.println("Zone ID: " + zone.getId() + ", Remaining Demand: " + zone.getDemand());
        }
    }
}
