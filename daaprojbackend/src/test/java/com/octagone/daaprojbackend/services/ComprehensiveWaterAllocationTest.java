package com.octagone.daaprojbackend.services;

import com.octagone.daaprojbackend.models.*;
import com.octagone.daaprojbackend.metrics.AllocationMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
public class ComprehensiveWaterAllocationTest {

    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveWaterAllocationTest.class);

    @Autowired
    private WaterAllocationServiceImpl waterAllocationService;

    private static final String RESULTS_DIR = "test-results/";
    private final Random random = new Random(42); // Fixed seed for reproducibility

    @Test
    void runComprehensiveTests() {
        createResultsDirectory();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // Create both detailed and CSV reports
        String detailedReport = RESULTS_DIR + "detailed_results_" + timestamp + ".txt";
        String csvReport = RESULTS_DIR + "metrics_" + timestamp + ".csv";

        try (PrintWriter detailedWriter = new PrintWriter(new FileWriter(detailedReport));
                PrintWriter csvWriter = new PrintWriter(new FileWriter(csvReport))) {

            // Write CSV header
            writeCSVHeader(csvWriter);

            // Run realistic test scenarios
            runScenario("Small Town Network", 2, 4, 5, detailedWriter, csvWriter);
            runScenario("Suburban District", 4, 8, 12, detailedWriter, csvWriter);
            runScenario("City Network", 6, 15, 25, detailedWriter, csvWriter);
            runScenario("Metropolitan Area", 8, 20, 35, detailedWriter, csvWriter);
            runScenario("Regional Network", 10, 30, 50, detailedWriter, csvWriter);

            System.out.println("Test results written to:");
            System.out.println("Detailed report: " + detailedReport);
            System.out.println("CSV metrics: " + csvReport);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runScenario(String scenarioName, int numSources, int numZones,
            int numConnections, PrintWriter detailedWriter, PrintWriter csvWriter) {

        detailedWriter.println("\n=== " + scenarioName + " ===");
        detailedWriter.println("Configuration:");
        detailedWriter.printf("- Sources: %d\n- Zones: %d\n- Connections: %d\n\n",
                numSources, numZones, numConnections);

        // Generate test data
        List<Source> sources = generateSources(numSources);
        List<Zone> zones = generateZones(numZones);
        List<Connection> connections = generateConnections(sources, zones, numConnections);

        // Store original capacities before allocation
        double totalOriginalCapacity = sources.stream()
                .mapToDouble(Source::getCapacity)
                .sum();

        // Run allocation and measure time
        long startTime = System.nanoTime();
        AllocationResponse response = waterAllocationService.allocateWater(sources, zones, connections);
        long executionTime = System.nanoTime() - startTime;

        // Calculate metrics using original capacity
        AllocationMetrics metrics = new AllocationMetrics(scenarioName);
        metrics.setTotalSourceCapacity(totalOriginalCapacity);
        metrics.calculateMetrics(sources, zones, connections, response, executionTime);

        // Write results
        writeDetailedResults(detailedWriter, metrics, response, sources, zones, connections);
        writeCSVMetrics(csvWriter, metrics);
    }

    private List<Source> generateSources(int count) {
        List<Source> sources = new ArrayList<>();

        // Base capacity per source (ensure enough total capacity)
        double baseCapacity = switch (count) {
            case 2 -> 50.0; // Small town: ~100 MLD total
            case 4 -> 60.0; // Suburban: ~240 MLD total
            case 6 -> 70.0; // City: ~420 MLD total
            case 8 -> 100.0; // Metro: ~800 MLD total
            default -> 120.0; // Regional: ~1200 MLD total
        };

        for (int i = 1; i <= count; i++) {
            // Add 10% variation to capacity
            double capacity = baseCapacity + (random.nextDouble() * baseCapacity * 0.1);
            sources.add(new Source((long) i, capacity));
            logger.debug("Generated Source {}: Capacity={}", i, capacity);
        }
        return sources;
    }

    private List<Zone> generateZones(int count) {
        List<Zone> zones = new ArrayList<>();

        // Set minimum demands based on network size
        double minDemand = switch (count) {
            case 4 -> 20.0; // Small town: 20-30 MLD
            case 8 -> 25.0; // Suburban: 25-35 MLD
            case 15 -> 30.0; // City: 30-40 MLD
            case 20 -> 35.0; // Metro: 35-45 MLD
            default -> 40.0; // Regional: 40-50 MLD
        };

        for (int i = 1; i <= count; i++) {
            // Generate demand with 20% variation but never below minDemand
            double demand = minDemand + (random.nextDouble() * minDemand * 0.2);

            // Assign priorities with realistic distribution
            int priority;
            if (i <= count * 0.6) { // 60% residential
                priority = 1;
                demand *= 1.0; // Base demand
            } else if (i <= count * 0.85) { // 25% commercial
                priority = 2;
                demand *= 1.2; // 20% higher than residential
            } else { // 15% industrial
                priority = 3;
                demand *= 1.5; // 50% higher than residential
            }

            zones.add(new Zone((long) i, demand, priority));
            logger.debug("Generated Zone {}: Demand={}, Priority={}", i, demand, priority);
        }
        return zones;
    }

    private List<Connection> generateConnections(List<Source> sources, List<Zone> zones, int count) {
        List<Connection> connections = new ArrayList<>();
        long connectionId = 1;

        // Ensure each zone has at least one connection to nearest source
        for (Zone zone : zones) {
            Source nearestSource = sources.get(random.nextInt(sources.size()));
            // Base cost on distance (simulated by random but controlled variation)
            double baseCost = 1.0 + random.nextDouble(); // Base cost between 1-2
            connections.add(new Connection(connectionId++, nearestSource, zone, baseCost));
        }

        // Add additional connections with higher costs (representing longer distances)
        while (connections.size() < count) {
            Source source = sources.get(random.nextInt(sources.size()));
            Zone zone = zones.get(random.nextInt(zones.size()));
            // Additional connections have higher costs
            double cost = 2.0 + random.nextDouble() * 2.0; // Cost between 2-4
            connections.add(new Connection(connectionId++, source, zone, cost));
        }

        return connections;
    }

    private void writeDetailedResults(PrintWriter writer, AllocationMetrics metrics,
            AllocationResponse response, List<Source> sources, List<Zone> zones, List<Connection> connections) {

        writer.println("Performance Metrics:");
        writer.printf("- Execution Time: %.2f ms\n", metrics.getExecutionTimeMs());
        writer.printf("- Overall Satisfaction Rate: %.2f%%\n", metrics.getSatisfactionRate());
        writer.printf("- Resource Utilization: %.2f%%\n", metrics.getResourceUtilization());
        writer.printf("- Cost Efficiency: %.4f\n", metrics.getCostEfficiency());

        writer.println("\nNetwork Configuration:");
        writer.println("Available Connections:");
        connections.stream()
                .sorted(Comparator.comparing(Connection::getId))
                .forEach(conn -> writer.printf("  Connection %d: Source %d → Zone %d (Priority: %d, Cost: %.2f)\n",
                        conn.getId(),
                        conn.getSource().getId(),
                        conn.getZone().getId(),
                        conn.getZone().getPriority(),
                        conn.getCostPerUnit()));

        writer.println("\nPriority-based Analysis:");
        metrics.getPriorityMetrics().forEach((priority, prioMetrics) -> {
            writer.printf("Priority %d:\n", priority);
            writer.printf("  Zones: %d\n", prioMetrics.getNumZones());
            writer.printf("  Demand: %.2f\n", prioMetrics.getDemand());
            writer.printf("  Allocated: %.2f\n", prioMetrics.getAllocated());
            writer.printf("  Satisfaction Rate: %.2f%%\n", prioMetrics.getSatisfactionRate());
        });

        writer.println("\nDetailed Allocations:");
        response.getAllocations().forEach(allocation -> {
            writer.printf("Zone %d:\n", allocation.getZoneId());
            writer.printf("  Total Allocated: %.2f units\n", allocation.getAmount());
            for (ConnectionAllocation connAlloc : allocation.getConnectionAllocations()) {
                writer.printf("  - Connection %d: %.2f units\n",
                        connAlloc.getConnectionId(), connAlloc.getAmount());
            }
        });

        writer.println("\n---------------------------------------------------\n");
    }

    private void writeCSVHeader(PrintWriter writer) {
        writer.println("Scenario,Sources,Zones,Connections,ExecutionTime(ms)," +
                "SatisfactionRate(%),ResourceUtilization(%),CostEfficiency");
    }

    private void writeCSVMetrics(PrintWriter writer, AllocationMetrics metrics) {
        writer.printf("%s,%d,%d,%d,%.2f,%.2f,%.2f,%.4f\n",
                metrics.getScenarioName(),
                metrics.getNumSources(),
                metrics.getNumZones(),
                metrics.getNumConnections(),
                metrics.getExecutionTimeMs(),
                metrics.getSatisfactionRate(),
                metrics.getResourceUtilization(),
                metrics.getCostEfficiency());
    }

    private void createResultsDirectory() {
        File directory = new File(RESULTS_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}