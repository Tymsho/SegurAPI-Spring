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
import com.first.api.first_api.dto.PolizaDTO;
import com.first.api.first_api.repositories.PolizaRepository;
import com.first.api.first_api.exceptions.ResourceNotFoundException;

class PolizaServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @InjectMocks
    private PolizaService polizaService;

    private Poliza polizaPrueba;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        polizaPrueba = new Poliza();
        polizaPrueba.setId(1L);
        polizaPrueba.setNumeroPoliza("POL-123");
        polizaPrueba.setFechaInicio(LocalDate.now());
        polizaPrueba.setFechaFin(LocalDate.now().plusYears(1));
        polizaPrueba.setActiva(true);
    }

    @Test
    void shouldReturnPolizaDtoWhenIdExists() {
        // ARRANGE
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaPrueba));

        // ACT
        Optional<PolizaDTO> resultado = polizaService.buscarPorId(1L);

        // ASSERT
        assertTrue(resultado.isPresent());
        assertEquals("POL-123", resultado.get().getNumeroPoliza());
        verify(polizaRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenActualizarFails() {
        // ARRANGE
        when(polizaRepository.findById(999L)).thenReturn(Optional.empty());
        PolizaDTO dtoActualizacion = new PolizaDTO();
        
        // ACT & ASSERT
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            polizaService.actualizarPoliza(999L, dtoActualizacion);
        });

        assertEquals("Póliza no encontrada con id: 999", exception.getMessage());
    }
}