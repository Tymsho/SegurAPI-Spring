package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Poliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolizaRepository extends JpaRepository<Poliza, Long> {
    
    // Trae solo las pólizas emitidas por el productor logueado
    List<Poliza> findByProductorEmail(String email);

    // Busca una póliza por ID asegurando la propiedad de la misma
    Optional<Poliza> findByIdAndProductorEmail(Long id, String email);
}