package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Poliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface PolizaRepository extends JpaRepository<Poliza, Long> {
    
    @Query("SELECT p FROM Poliza p WHERE p.productor.email = :email " +
           "AND (:clienteId IS NULL OR p.tomador.id = :clienteId) " +
           "AND (:companiaId IS NULL OR p.compania.id = :companiaId) " +
           "AND (:ramoId IS NULL OR p.ramo.id = :ramoId)")
    Page<Poliza> findMisPolizasFiltradas(
        @Param("email") String email,
        @Param("clienteId") Long clienteId,
        @Param("companiaId") Long companiaId,
        @Param("ramoId") Long ramoId,
        Pageable pageable
    );

    // Trae solo las pólizas emitidas por el productor logueado
    Page<Poliza> findByProductorEmail(String email, Pageable pageable);

    // Busca una póliza por ID asegurando la propiedad de la misma
    Optional<Poliza> findByIdAndProductorEmail(Long id, String email);
}