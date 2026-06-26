package com.first.api.first_api.controllers;

import com.first.api.first_api.dtorequest.UsuarioRequest;
import com.first.api.first_api.models.Usuario;
import com.first.api.first_api.repositories.UsuarioRepository;
import com.first.api.first_api.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody UsuarioRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtils.generarToken(userDetails);
        
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", usuario.getId());
        userMap.put("nombre", usuario.getNombre());
        userMap.put("email", usuario.getEmail());
        userMap.put("rol", usuario.getRol() != null ? usuario.getRol().name() : "PRODUCTOR");
        
        response.put("user", userMap);
        return response;
    }
}