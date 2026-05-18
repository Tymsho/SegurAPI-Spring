package com.first.api.first_api.services;

import com.first.api.first_api.dto.UsuarioDTO;
import com.first.api.first_api.models.Rol;
import com.first.api.first_api.models.Usuario;
import com.first.api.first_api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(UsuarioDTO dto) {
        Usuario nuevo = new Usuario();
        nuevo.setNombre(dto.getNombre());
        nuevo.setEmail(dto.getEmail());
        nuevo.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // Asignación directa del Enum
        nuevo.setRol(Rol.PRODUCTOR); 
        
        // Nace inactivo para el futuro sistema de correos
        nuevo.setActivo(false); 
        
        return usuarioRepository.save(nuevo);
    }
}