package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Poliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PolizaRepository extends JpaRepository<Poliza, Long> {
    
    // Para el dashboard: Traer solo las pólizas que no están dadas de baja
    List<Poliza> findByActivaTrue();

    // Para el dashboard: Traer pólizas de un usuario (productor) específico
    List<Poliza> findByUsuarioId(Long usuarioId);
}