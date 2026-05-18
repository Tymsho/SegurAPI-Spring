package com.first.api.first_api.controllers;

import com.first.api.first_api.dto.PolizaDTO;
import com.first.api.first_api.services.PolizaService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/polizas")
public class PolizaController {

    @Autowired
    private PolizaService polizaService;

    // GET: Listar todas las pólizas activas (Ideal para el Dashboard general)
    @GetMapping
    public ResponseEntity<List<PolizaDTO>> obtenerPolizas() {
        return ResponseEntity.ok(polizaService.obtenerPolizasActivas());
    }

    // GET: Buscar una póliza por ID
    @GetMapping("/{id}")
    public ResponseEntity<PolizaDTO> buscarPolizaPorId(@PathVariable Long id) {
        Optional<PolizaDTO> polizaDTO = polizaService.buscarPorId(id);
        return polizaDTO.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET: Listar pólizas activas de un productor específico
    @GetMapping("/productor/{usuarioId}")
    public ResponseEntity<List<PolizaDTO>> obtenerPolizasPorProductor(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(polizaService.obtenerPolizasPorUsuario(usuarioId));
    }

    // POST: Crear una nueva póliza
    @PostMapping
    public ResponseEntity<PolizaDTO> crearPoliza(@Valid @RequestBody PolizaDTO polizadDTO) {
        PolizaDTO nuevaPoliza = polizaService.guardarPoliza(polizadDTO);
        return new ResponseEntity<>(nuevaPoliza, HttpStatus.CREATED);
    }

    // DELETE: Anular póliza (Baja Lógica)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anularPoliza(@PathVariable Long id) {
        polizaService.anularPoliza(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolizaDTO> actualizarPoliza(@PathVariable Long id, @Valid @RequestBody PolizaDTO polizaDTO) {
        PolizaDTO actualizada = polizaService.actualizarPoliza(id, polizaDTO); 
        return ResponseEntity.ok(actualizada);
    }
}
