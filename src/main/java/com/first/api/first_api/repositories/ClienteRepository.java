package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    // Trae solo los clientes del productor logueado
    @EntityGraph(attributePaths = {"localidad", "productor"})
    Page<Cliente> findByProductorEmail(String email, Pageable pageable);

    // Busca un cliente por DNI asegurando que pertenezca a la cartera del productor logueado
    Optional<Cliente> findByDniAndProductorEmail(String dni, String email);

    @Query("SELECT new com.first.api.first_api.dtoresponse.ClienteLightResponse(c.id, CONCAT(c.nombre, ' ', c.apellido), c.dni) " +
           "FROM Cliente c WHERE c.productor.email = :email AND c.activo = true " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :query, '%')) OR c.dni LIKE CONCAT('%', :query, '%'))")
    java.util.List<com.first.api.first_api.dtoresponse.ClienteLightResponse> searchClientesLight(@Param("email") String email, @Param("query") String query, Pageable pageable);

    // Busca un cliente específico garantizando que le pertenezca al productor
    Optional<Cliente> findByIdAndProductorEmail(Long id, String email);

    // Evita DNI duplicados pero acotado a la cartera de ese productor individual
    boolean existsByDniAndProductorEmail(String dni, String email);

    // Trae los clientes activos de ESE productor
    @EntityGraph(attributePaths = {"localidad", "productor"})
    Page<Cliente> findByActivoTrueAndProductorEmail(String email, Pageable pageable);

    // Trae los clientes activos de ESE productor, filtrando por una coincidencia en el nombre
    @EntityGraph(attributePaths = {"localidad", "productor"})
    Page<Cliente> findByNombreContainingIgnoreCaseAndActivoTrueAndProductorEmail(String nombre, String email, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.productor.email = :email AND c.activo = true")
    Long countActiveClientesByProductorEmail(@Param("email") String email);
}