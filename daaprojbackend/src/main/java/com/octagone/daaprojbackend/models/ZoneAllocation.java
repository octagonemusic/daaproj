package com.octagone.daaprojbackend.models;

import java.util.ArrayList;
import java.util.List;

public class ZoneAllocation {
    private Long zoneId;
    private double amount;
    private List<ConnectionAllocation> connectionAllocations;

    // Constructor
    public ZoneAllocation(Long zoneId) {
        this.zoneId = zoneId;
        this.amount = 0;
        this.connectionAllocations = new ArrayList<>();
    }

    // Getters
    public Long getZoneId() {
        return zoneId;
    }

    public double getAmount() {
        return amount;
    }

    public List<ConnectionAllocation> getConnectionAllocations() {
        return connectionAllocations;
    }

    // Setters
    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void addConnectionAllocation(Long connectionId, double amount) {
        this.connectionAllocations.add(new ConnectionAllocation(connectionId, amount));
        this.amount += amount;
    }
}
