package com.first.api.first_api.repositories;

import com.first.api.first_api.models.PlantillaImportacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlantillaImportacionRepository extends JpaRepository<PlantillaImportacion, Long> {

    Optional<PlantillaImportacion> findByCompaniaIdAndFirmaColumnas(Long companiaId, String firmaColumnas);
}
