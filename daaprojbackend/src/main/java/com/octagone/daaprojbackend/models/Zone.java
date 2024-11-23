package com.octagone.daaprojbackend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "zones")
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double demand;

    private int priority;

    @Transient
    private double filledCapacity = 0.0; // Track how much has been filled for each zone

    // Constructors, getters, and setters
    public Zone() {
    }

    public Zone(Long id, double demand, int priority) {
        this.id = id;
        this.demand = demand;
        this.priority = priority;
    }

    public Zone(double demand, int priority) {
        this.demand = demand;
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public double getDemand() {
        return demand;
    }

    public void setDemand(double demand) {
        this.demand = demand;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getFilledCapacity() {
        return filledCapacity;
    }

    public void addFilledCapacity(double allocation) {
        this.filledCapacity += allocation;
    }
}
