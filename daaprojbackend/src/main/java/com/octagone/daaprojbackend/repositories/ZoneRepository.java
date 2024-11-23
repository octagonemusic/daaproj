package com.octagone.daaprojbackend.repositories;

import com.octagone.daaprojbackend.models.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    // Additional query methods (if needed) can be defined here
}
