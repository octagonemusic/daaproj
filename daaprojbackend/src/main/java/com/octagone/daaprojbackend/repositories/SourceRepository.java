package com.octagone.daaprojbackend.repositories;

import com.octagone.daaprojbackend.models.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface SourceRepository extends JpaRepository<Source, Long> {
    // Additional query methods (if needed) can be defined here
}
