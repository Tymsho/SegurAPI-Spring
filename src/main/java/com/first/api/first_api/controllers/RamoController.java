package com.first.api.first_api.controllers;

import com.first.api.first_api.models.Ramo;
import com.first.api.first_api.repositories.RamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ramos")
public class RamoController {
    @Autowired
    private RamoRepository ramoRepository;

    @GetMapping
    public ResponseEntity<List<Ramo>> obtenerRamos() {
        return ResponseEntity.ok(ramoRepository.findAll());
    }
}
