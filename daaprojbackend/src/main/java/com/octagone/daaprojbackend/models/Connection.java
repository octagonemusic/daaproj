package com.octagone.daaprojbackend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "connections", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "source_id", "zone_id" })
})
public class Connection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @ManyToOne
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "cost_per_unit", nullable = false)
    private double costPerUnit;

    @Transient
    private double weightedCost; // Not persisted in DB

    public Connection() {
    }

    public Connection(Long id, Source source, Zone zone, double costPerUnit) {
        this.id = id;
        this.source = source;
        this.zone = zone;
        this.costPerUnit = costPerUnit;
    }

    public Connection(Source source, Zone zone, double costPerUnit) {
        this.source = source;
        this.zone = zone;
        this.costPerUnit = costPerUnit;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public double getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(double costPerUnit) {
        this.costPerUnit = costPerUnit;
    }

    public double getWeightedCost() {
        return weightedCost;
    }

    public void setWeightedCost(double weightedCost) {
        this.weightedCost = weightedCost;
    }
}
