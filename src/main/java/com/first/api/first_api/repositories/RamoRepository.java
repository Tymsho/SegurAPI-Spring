package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Ramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RamoRepository extends JpaRepository<Ramo, Long> {
}