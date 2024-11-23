package com.octagone.daaprojbackend.models;

public class ConnectionAllocation {
    private Long connectionId;
    private double amount;

    public ConnectionAllocation(Long connectionId, double amount) {
        this.connectionId = connectionId;
        this.amount = amount;
    }

    // Getters and setters
    public Long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}