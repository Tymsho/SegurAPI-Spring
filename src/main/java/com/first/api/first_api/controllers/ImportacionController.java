package com.first.api.first_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.first.api.first_api.importacion.ImportAnalysisResponseDTO;
import com.first.api.first_api.importacion.ImportExecutionResultDTO;
import com.first.api.first_api.importacion.ImportMappingConfigDTO;
import com.first.api.first_api.importacion.ImportacionCarteraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controlador REST para el proceso de importación inteligente de carteras.
 */
@RestController
@RequestMapping("/api/importacion")
@Tag(name = "Importación de Carteras", description = "Endpoints para analizar y ejecutar importaciones masivas de aseguradoras")
public class ImportacionController {

    @Autowired
    private ImportacionCarteraService importacionCarteraService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/analizar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Analiza una planilla Excel o CSV y sugiere la configuración de mapeo")
    public ResponseEntity<ImportAnalysisResponseDTO> analizar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("companiaId") Long companiaId) {
        return ResponseEntity.ok(importacionCarteraService.analizarArchivo(file, companiaId));
    }

    @PostMapping(value = "/ejecutar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ejecuta la importación transaccional con la configuración de mapeo confirmada")
    public ResponseEntity<ImportExecutionResultDTO> ejecutar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("companiaId") Long companiaId,
            @RequestParam("config") String configJson) throws IOException {
        ImportMappingConfigDTO config = objectMapper.readValue(configJson, ImportMappingConfigDTO.class);
        return ResponseEntity.ok(importacionCarteraService.ejecutarImportacion(file, companiaId, config));
    }
}
