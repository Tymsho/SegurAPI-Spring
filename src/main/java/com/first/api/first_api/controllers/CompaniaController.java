package com.first.api.first_api.controllers;

import com.first.api.first_api.models.Compania;
import com.first.api.first_api.repositories.CompaniaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companias")
public class CompaniaController {
    @Autowired
    private CompaniaRepository companiaRepository;

    @GetMapping
    public ResponseEntity<List<Compania>> obtenerCompanias() {
        return ResponseEntity.ok(companiaRepository.findAll());
    }
}
