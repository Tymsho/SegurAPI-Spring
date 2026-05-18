package com.first.api.first_api.controllers;

import com.first.api.first_api.dto.UsuarioDTO;
import com.first.api.first_api.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrar(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        try {
            String mensaje = usuarioService.registrarUsuario(usuarioDTO);
            return new ResponseEntity<>(Collections.singletonMap("mensaje", mensaje), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Collections.singletonMap("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}