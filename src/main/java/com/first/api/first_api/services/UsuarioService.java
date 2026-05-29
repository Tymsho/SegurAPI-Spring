package com.first.api.first_api.services;

import com.first.api.first_api.dtorequest.UsuarioRequest;
import com.first.api.first_api.models.Rol;
import com.first.api.first_api.models.Usuario;
import com.first.api.first_api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Usuario registrarUsuario(UsuarioRequest dto) {
        Usuario nuevo = new Usuario();
        nuevo.setNombre(dto.getNombre());
        nuevo.setEmail(dto.getEmail());
        nuevo.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // Asignación directa del Enum
        nuevo.setRol(Rol.PRODUCTOR); 
        
        // Nace inactivo para el futuro sistema de correos
        nuevo.setActivo(false); 

        // Generar código de verificación aleatorio (6 dígitos)
        String codigo = String.format("%06d", new Random().nextInt(1000000));
        nuevo.setCodigoVerificacion(codigo);
        nuevo.setCodigoVencimiento(LocalDateTime.now().plusMinutes(15));
        
        Usuario usuarioGuardado = usuarioRepository.save(nuevo);

        // Enviar el correo de verificación
        emailService.enviarCorreoVerificacion(usuarioGuardado.getEmail(), codigo);
        
        return usuarioGuardado;
    }

    @Transactional
    public void verificarUsuario(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("El usuario con email " + email + " no existe."));

        if (usuario.isActivo()) {
            throw new RuntimeException("La cuenta ya se encuentra activa.");
        }

        if (usuario.getCodigoVerificacion() == null || !usuario.getCodigoVerificacion().equals(codigo)) {
            throw new RuntimeException("Código de verificación incorrecto.");
        }

        if (usuario.getCodigoVencimiento() == null || usuario.getCodigoVencimiento().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El código de verificación ha expirado.");
        }

        // Activar la cuenta y limpiar los campos temporales de verificación
        usuario.setActivo(true);
        usuario.setCodigoVerificacion(null);
        usuario.setCodigoVencimiento(null);
        usuarioRepository.save(usuario);
    }
}