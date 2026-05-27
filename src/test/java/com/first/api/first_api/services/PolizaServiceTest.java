package com.first.api.first_api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.first.api.first_api.models.Poliza;
import com.first.api.first_api.dtorequest.PolizaRequest;
import com.first.api.first_api.dtoresponse.PolizaResponse;
import com.first.api.first_api.repositories.PolizaRepository;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.mappers.PolizaMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class PolizaServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private PolizaMapper polizaMapper;

    @InjectMocks
    private PolizaService polizaService;

    private Poliza polizaPrueba;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@mail.com");
        SecurityContextHolder.setContext(securityContext);

        polizaPrueba = new Poliza();
        polizaPrueba.setId(1L);
        polizaPrueba.setNroPza("POL-123");
        polizaPrueba.setInicioVigencia(LocalDate.now());
        polizaPrueba.setFinVigencia(LocalDate.now().plusYears(1));
        polizaPrueba.setActivo(true);
    }

    @Test
    void shouldReturnPolizaDtoWhenIdExists() {
        // ARRANGE
        when(polizaRepository.findByIdAndProductorEmail(1L, "test@mail.com")).thenReturn(Optional.of(polizaPrueba));
        
        PolizaResponse dtoPrueba = new PolizaResponse();
        dtoPrueba.setNroPza("POL-123");
        when(polizaMapper.toResponse(polizaPrueba)).thenReturn(dtoPrueba);

        // ACT
        Optional<PolizaResponse> resultado = polizaService.buscarPorId(1L);

        // ASSERT
        assertTrue(resultado.isPresent());
        assertEquals("POL-123", resultado.get().getNroPza());
        verify(polizaRepository, times(1)).findByIdAndProductorEmail(1L, "test@mail.com");
    }

    @Test
    void shouldThrowExceptionWhenActualizarFails() {
        // ARRANGE
        when(polizaRepository.findByIdAndProductorEmail(999L, "test@mail.com")).thenReturn(Optional.empty());
        PolizaRequest dtoActualizacion = new PolizaRequest();
        
        // ACT & ASSERT
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            polizaService.actualizarPoliza(999L, dtoActualizacion);
        });

        assertEquals("Póliza no encontrada o acceso denegado", exception.getMessage());
    }
}