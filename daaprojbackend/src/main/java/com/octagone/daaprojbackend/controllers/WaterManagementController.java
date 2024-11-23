package com.octagone.daaprojbackend.controllers;

import com.octagone.daaprojbackend.models.AllocationResponse;
import com.octagone.daaprojbackend.services.WaterAllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class WaterManagementController {

    @Autowired
    private WaterAllocationService waterAllocationService;

    @GetMapping("/allocate-water")
    public ResponseEntity<AllocationResponse> allocateWater() {
        try {
            AllocationResponse allocationResponse = waterAllocationService.allocateWater();
            return new ResponseEntity<>(allocationResponse, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Handle specific error like missing or invalid data
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            // General error catch-all
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to allocate water", e);
        }
    }

    // Global exception handler for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>("Unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
