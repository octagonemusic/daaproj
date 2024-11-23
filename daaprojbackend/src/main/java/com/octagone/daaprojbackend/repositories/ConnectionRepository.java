package com.octagone.daaprojbackend.repositories;

import com.octagone.daaprojbackend.models.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    // Additional query methods (if needed) can be defined here
}
