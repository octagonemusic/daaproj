package com.octagone.daaprojbackend.metrics;

import com.octagone.daaprojbackend.models.*;
import java.util.*;
import java.util.stream.Collectors;

public class AllocationMetrics {
    private String scenarioName;
    private int numSources;
    private int numZones;
    private int numConnections;
    private double totalDemand;
    private double totalAllocated;
    private double totalSourceCapacity;
    private double executionTimeMs;
    private Map<Integer, PriorityMetrics> priorityMetrics;
    private double costEfficiency;
    private double resourceUtilization;
    private double satisfactionRate;

    public AllocationMetrics(String scenarioName) {
        this.scenarioName = scenarioName;
        this.priorityMetrics = new HashMap<>();
    }

    public void calculateMetrics(List<Source> sources, List<Zone> zones,
            List<Connection> connections, AllocationResponse response, long executionTime) {

        this.numSources = sources.size();
        this.numZones = zones.size();
        this.numConnections = connections.size();
        this.executionTimeMs = executionTime / 1_000_000.0;

        // Calculate total original demand (demand + filledCapacity gives us original
        // demand)
        this.totalDemand = zones.stream()
                .mapToDouble(z -> z.getDemand() + z.getFilledCapacity())
                .sum();

        // Calculate total allocated from filled capacities
        this.totalAllocated = zones.stream()
                .mapToDouble(Zone::getFilledCapacity)
                .sum();

        // Calculate resource utilization using original capacity
        this.resourceUtilization = (totalAllocated / totalSourceCapacity) * 100;

        // Calculate satisfaction rate correctly
        this.satisfactionRate = totalDemand > 0 ? Math.min((totalAllocated / totalDemand) * 100, 100) : 0;

        calculatePriorityMetrics(zones, response);
        calculateCostEfficiency(connections, response);
    }

    private void calculatePriorityMetrics(List<Zone> zones, AllocationResponse response) {
        Map<Integer, List<Zone>> priorityGroups = zones.stream()
                .collect(Collectors.groupingBy(Zone::getPriority));

        priorityGroups.forEach((priority, priorityZones) -> {
            // Calculate original demand for this priority group
            double priorityDemand = priorityZones.stream()
                    .mapToDouble(z -> z.getDemand() + z.getFilledCapacity())
                    .sum();

            // Calculate what was actually allocated
            double priorityAllocated = priorityZones.stream()
                    .mapToDouble(Zone::getFilledCapacity)
                    .sum();

            // Cap satisfaction rate at 100%
            double satisfactionRate = priorityDemand > 0 ? Math.min((priorityAllocated / priorityDemand) * 100, 100)
                    : 0;

            priorityMetrics.put(priority, new PriorityMetrics(
                    priorityDemand,
                    priorityAllocated,
                    priorityZones.size(),
                    satisfactionRate));
        });
    }

    private void calculateCostEfficiency(List<Connection> connections, AllocationResponse response) {
        double totalCost = 0.0;

        for (ZoneAllocation allocation : response.getAllocations()) {
            for (ConnectionAllocation connAlloc : allocation.getConnectionAllocations()) {
                Optional<Connection> connection = connections.stream()
                        .filter(c -> c.getId().equals(connAlloc.getConnectionId()))
                        .findFirst();

                if (connection.isPresent()) {
                    totalCost += connection.get().getCostPerUnit() * connAlloc.getAmount();
                }
            }
        }

        // Cost efficiency is total allocation per unit cost
        this.costEfficiency = totalCost > 0 ? totalAllocated / totalCost : 0.0;
    }

    // Getters
    public String getScenarioName() {
        return scenarioName;
    }

    public int getNumSources() {
        return numSources;
    }

    public int getNumZones() {
        return numZones;
    }

    public int getNumConnections() {
        return numConnections;
    }

    public double getTotalDemand() {
        return totalDemand;
    }

    public double getTotalAllocated() {
        return totalAllocated;
    }

    public double getTotalSourceCapacity() {
        return totalSourceCapacity;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public Map<Integer, PriorityMetrics> getPriorityMetrics() {
        return priorityMetrics;
    }

    public double getSatisfactionRate() {
        return satisfactionRate;
    }

    public double getResourceUtilization() {
        return resourceUtilization;
    }

    public double getCostEfficiency() {
        return costEfficiency;
    }

    // Inner class for priority-specific metrics
    public static class PriorityMetrics {
        private double demand;
        private double allocated;
        private int numZones;
        private double satisfactionRate;

        public PriorityMetrics(double demand, double allocated, int numZones, double satisfactionRate) {
            this.demand = demand;
            this.allocated = allocated;
            this.numZones = numZones;
            this.satisfactionRate = satisfactionRate;
        }

        public double getDemand() {
            return demand;
        }

        public double getAllocated() {
            return allocated;
        }

        public int getNumZones() {
            return numZones;
        }

        public double getSatisfactionRate() {
            return satisfactionRate;
        }
    }

    // Add this setter
    public void setTotalSourceCapacity(double capacity) {
        this.totalSourceCapacity = capacity;
    }
}