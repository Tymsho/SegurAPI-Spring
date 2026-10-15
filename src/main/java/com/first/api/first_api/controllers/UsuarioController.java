package com.first.api.first_api.controllers;

import com.first.api.first_api.dtorequest.UsuarioRequest;
import com.first.api.first_api.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@Valid @RequestBody UsuarioRequest usuarioDTO) {
        usuarioService.registrarUsuario(usuarioDTO);
        return new ResponseEntity<>(
                "Registro exitoso. Se ha enviado un código de verificación a tu correo.", 
                HttpStatus.CREATED
        );
    }

    @PostMapping("/verificar")
    public ResponseEntity<String> verificarCorreo(
            @RequestParam("email") String email, 
            @RequestParam("codigo") String codigo) {
        usuarioService.verificarUsuario(email, codigo);
        return new ResponseEntity<>("Cuenta verificada exitosamente. Ahora puedes iniciar sesión.", HttpStatus.OK);
    }
}