package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Localidad;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {

    @EntityGraph(attributePaths = {"provincia"})
    java.util.List<Localidad> findAllBy();

    Optional<Localidad> findByNombreIgnoreCaseAndProvinciaId(String nombre, Long provinciaId);
}