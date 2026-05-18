package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Cliente findByDniCuit(String dniCuit);

    @Query("SELECT c FROM Cliente c WHERE c.nombre LIKE %:nombre% AND c.activo = true")
    List<Cliente> buscarPorNombreYActivo(@Param("nombre") String nombre);
}

