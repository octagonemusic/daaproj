package com.octagone.daaprojbackend.services;

import com.octagone.daaprojbackend.models.AllocationResponse;
import com.octagone.daaprojbackend.models.Connection;
import com.octagone.daaprojbackend.models.Source;
import com.octagone.daaprojbackend.models.Zone;
import com.octagone.daaprojbackend.repositories.ConnectionRepository;
import com.octagone.daaprojbackend.repositories.SourceRepository;
import com.octagone.daaprojbackend.repositories.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WaterAllocationService {

    @Autowired
    private WaterAllocationServiceImpl allocationServiceImpl;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    public AllocationResponse allocateWater() {
        // Retrieve data from repositories
        List<Source> sources = sourceRepository.findAll();
        List<Zone> zones = zoneRepository.findAll();
        List<Connection> connections = connectionRepository.findAll();

        // Call the allocation algorithm
        return allocationServiceImpl.allocateWater(sources, zones, connections);
    }
}
