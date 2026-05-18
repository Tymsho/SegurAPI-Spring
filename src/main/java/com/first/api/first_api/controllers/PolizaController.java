package com.first.api.first_api.controllers;

import com.first.api.first_api.dto.PolizaDTO;
import com.first.api.first_api.services.PolizaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/polizas")
public class PolizaController {

    @Autowired
    private PolizaService polizaService;

    @PostMapping
    public ResponseEntity<PolizaDTO> crearPoliza(@Valid @RequestBody PolizaDTO polizaDTO) {
        PolizaDTO nuevaPoliza = polizaService.guardarPoliza(polizaDTO);
        return new ResponseEntity<>(nuevaPoliza, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PolizaDTO>> obtenerMisPolizas() {
        // Trae las activas del productor logueado automáticamente
        List<PolizaDTO> polizas = polizaService.obtenerMisPolizasActivas();
        return ResponseEntity.ok(polizas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolizaDTO> obtenerPolizaPorId(@PathVariable("id") Long id) {
        return polizaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolizaDTO> actualizarPoliza(
            @PathVariable("id") Long id, 
            @Valid @RequestBody PolizaDTO polizaDTO) {
        PolizaDTO actualizada = polizaService.actualizarPoliza(id, polizaDTO);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anularPoliza(@PathVariable("id") Long id) {
        polizaService.anularPoliza(id);
        return ResponseEntity.noContent().build();
    }
}