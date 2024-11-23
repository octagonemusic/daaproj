package com.octagone.daaprojbackend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "sources")
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capacity", nullable = false)
    private double capacity;

    public Source() {
    }

    public Source(Long id, double capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public Source(double capacity) {
        this.capacity = capacity;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }
}
