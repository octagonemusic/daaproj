package com.octagone.daaprojbackend.models;

import java.util.List;

public class AllocationResponse {
    private List<ZoneAllocation> allocations;

    public AllocationResponse(List<ZoneAllocation> allocations) {
        this.allocations = allocations;
    }

    // Getters
    public List<ZoneAllocation> getAllocations() {
        return allocations;
    }
}
