package com.first.api.first_api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.first.api.first_api.dtorequest.UsuarioRequest;
import com.first.api.first_api.exceptions.BusinessRuleException;
import com.first.api.first_api.models.Rol;
import com.first.api.first_api.models.Usuario;
import com.first.api.first_api.repositories.UsuarioRepository;

@SuppressWarnings("null")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegistrarUsuarioConPasswordCodificadaInactivoYCodigoVerificacion() {
        // ARRANGE
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Carlos Gomez");
        request.setEmail("carlos@mail.com");
        request.setPassword("password123");

        when(usuarioRepository.findByEmail("carlos@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Usuario resultado = usuarioService.registrarUsuario(request);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("encodedPassword123", resultado.getPassword());
        assertFalse(resultado.isActivo());
        assertEquals(Rol.PRODUCTOR, resultado.getRol());
        assertNotNull(resultado.getCodigoVerificacion());
        assertEquals(6, resultado.getCodigoVerificacion().length());
        assertTrue(resultado.getCodigoVerificacion().matches("\\d{6}"));
        assertNotNull(resultado.getCodigoVencimiento());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(emailService, times(1)).enviarCorreoVerificacion(eq("carlos@mail.com"), eq(resultado.getCodigoVerificacion()));
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void shouldVerificarUsuarioExitoso() {
        // ARRANGE
        Usuario usuario = new Usuario();
        usuario.setEmail("carlos@mail.com");
        usuario.setActivo(false);
        usuario.setCodigoVerificacion("123456");
        usuario.setCodigoVencimiento(LocalDateTime.now().plusMinutes(10));

        when(usuarioRepository.findByEmail("carlos@mail.com")).thenReturn(Optional.of(usuario));

        // ACT
        usuarioService.verificarUsuario("carlos@mail.com", "123456");

        // ASSERT
        assertTrue(usuario.isActivo());
        assertNull(usuario.getCodigoVerificacion());
        assertNull(usuario.getCodigoVencimiento());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenCodigoIncorrecto() {
        // ARRANGE
        Usuario usuario = new Usuario();
        usuario.setEmail("carlos@mail.com");
        usuario.setActivo(false);
        usuario.setCodigoVerificacion("123456");
        usuario.setCodigoVencimiento(LocalDateTime.now().plusMinutes(10));

        when(usuarioRepository.findByEmail("carlos@mail.com")).thenReturn(Optional.of(usuario));

        // ACT & ASSERT
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            usuarioService.verificarUsuario("carlos@mail.com", "999999");
        });

        assertEquals("Código de verificación incorrecto.", ex.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenCodigoExpirado() {
        // ARRANGE
        Usuario usuario = new Usuario();
        usuario.setEmail("carlos@mail.com");
        usuario.setActivo(false);
        usuario.setCodigoVerificacion("123456");
        usuario.setCodigoVencimiento(LocalDateTime.now().minusMinutes(5));

        when(usuarioRepository.findByEmail("carlos@mail.com")).thenReturn(Optional.of(usuario));

        // ACT & ASSERT
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> {
            usuarioService.verificarUsuario("carlos@mail.com", "123456");
        });

        assertEquals("El código de verificación ha expirado.", ex.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
