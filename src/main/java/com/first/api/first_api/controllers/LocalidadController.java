package com.first.api.first_api.controllers;

import com.first.api.first_api.models.Localidad;
import com.first.api.first_api.repositories.LocalidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localidades")
public class LocalidadController {

    @Autowired
    private LocalidadRepository localidadRepository;

    @GetMapping
    public ResponseEntity<List<Localidad>> obtenerLocalidades() {
        return ResponseEntity.ok(localidadRepository.findAll());
    }
}
