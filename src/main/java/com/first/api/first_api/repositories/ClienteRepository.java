package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    // Trae solo los clientes del productor logueado
    Page<Cliente> findByProductorEmail(String email, Pageable pageable);

    // Busca un cliente específico garantizando que le pertenezca al productor
    Optional<Cliente> findByIdAndProductorEmail(Long id, String email);

    // Evita DNI duplicados pero acotado a la cartera de ese productor individual
    boolean existsByDniAndProductorEmail(String dni, String email);

    // Trae los clientes activos de ESE productor
    Page<Cliente> findByActivoTrueAndProductorEmail(String email, Pageable pageable);

    // Trae los clientes activos de ESE productor, filtrando por una coincidencia en el nombre
    Page<Cliente> findByNombreContainingIgnoreCaseAndActivoTrueAndProductorEmail(String nombre, String email, Pageable pageable);
}