package com.first.api.first_api.services;

import com.first.api.first_api.dto.UsuarioDTO;
import com.first.api.first_api.models.Rol;
import com.first.api.first_api.models.Usuario;
import com.first.api.first_api.repositories.RolRepository;
import com.first.api.first_api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registrarUsuario(UsuarioDTO dto) {
        // 1. Verificar si el email ya existe
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // 2. Buscar o crear el rol USER
        Rol rolUser = rolRepository.findByNombre("ROLE_USER")
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombre("ROLE_USER");
                    return rolRepository.save(nuevoRol);
                });

        // 3. Crear el usuario y encriptar la password
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(dto.getEmail());
        nuevoUsuario.setNombre("Usuario Nuevo"); // Valor por defecto
        nuevoUsuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        nuevoUsuario.setRoles(Set.of(rolUser));
        nuevoUsuario.setActivo(true);

        usuarioRepository.save(nuevoUsuario);

        return "Usuario registrado exitosamente";
    }
}