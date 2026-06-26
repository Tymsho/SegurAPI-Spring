package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Poliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface PolizaRepository extends JpaRepository<Poliza, Long> {
    
    @EntityGraph(attributePaths = {"tomador", "compania", "ramo", "productor"})
    @Query("SELECT p FROM Poliza p WHERE p.productor.email = :email AND p.activo = true " +
           "AND (:nroPza IS NULL OR LOWER(p.nroPza) LIKE LOWER(CONCAT('%', :nroPza, '%'))) " +
           "AND (:clienteId IS NULL OR p.tomador.id = :clienteId) " +
           "AND (:companiaId IS NULL OR p.compania.id = :companiaId) " +
           "AND (:ramoId IS NULL OR p.ramo.id = :ramoId)")
    Page<Poliza> findMisPolizasFiltradas(
        @Param("email") String email,
        @Param("nroPza") String nroPza,
        @Param("clienteId") Long clienteId,
        @Param("companiaId") Long companiaId,
        @Param("ramoId") Long ramoId,
        Pageable pageable
    );

    // Trae solo las pólizas emitidas por el productor logueado
    Page<Poliza> findByProductorEmail(String email, Pageable pageable);

    // Busca una póliza por ID asegurando la propiedad de la misma
    Optional<Poliza> findByIdAndProductorEmail(Long id, String email);

    @Query("SELECT COUNT(p) FROM Poliza p WHERE p.productor.email = :email AND p.activo = true")
    Long countActivePolizasByProductorEmail(@Param("email") String email);

    @Query("SELECT COALESCE(SUM(p.premio), 0.0) FROM Poliza p WHERE p.productor.email = :email AND p.activo = true")
    Double sumPremioByProductorEmail(@Param("email") String email);

    @Query("SELECT COALESCE(SUM(p.prima), 0.0) FROM Poliza p WHERE p.productor.email = :email AND p.activo = true")
    Double sumPrimaByProductorEmail(@Param("email") String email);

    @Query("SELECT new com.first.api.first_api.dtoresponse.MesStat(YEAR(p.inicioVigencia), MONTH(p.inicioVigencia), COUNT(p)) " +
           "FROM Poliza p " +
           "WHERE p.productor.email = :email AND p.activo = true " +
           "GROUP BY YEAR(p.inicioVigencia), MONTH(p.inicioVigencia) " +
           "ORDER BY YEAR(p.inicioVigencia) ASC, MONTH(p.inicioVigencia) ASC")
    java.util.List<com.first.api.first_api.dtoresponse.MesStat> getPolizasPorMes(@Param("email") String email);

    @Query("SELECT new com.first.api.first_api.dtoresponse.CompaniaStat(p.compania.nombre, COUNT(p)) " +
           "FROM Poliza p " +
           "WHERE p.productor.email = :email AND p.activo = true " +
           "GROUP BY p.compania.nombre")
    java.util.List<com.first.api.first_api.dtoresponse.CompaniaStat> getPolizasPorCompania(@Param("email") String email);

    @Query("SELECT new com.first.api.first_api.dtoresponse.RamoStat(p.ramo.nombre, COUNT(p)) " +
           "FROM Poliza p " +
           "WHERE p.productor.email = :email AND p.activo = true " +
           "GROUP BY p.ramo.nombre")
    java.util.List<com.first.api.first_api.dtoresponse.RamoStat> getPolizasPorRamo(@Param("email") String email);

    @Query("SELECT new com.first.api.first_api.dtoresponse.PagoStat(p.tipoPago, COUNT(p)) " +
           "FROM Poliza p " +
           "WHERE p.productor.email = :email AND p.activo = true " +
           "GROUP BY p.tipoPago")
    java.util.List<com.first.api.first_api.dtoresponse.PagoStat> getPolizasPorTipoPago(@Param("email") String email);
}