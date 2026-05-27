package com.first.api.first_api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.first.api.first_api.models.Cliente;
import com.first.api.first_api.dtoresponse.ClienteResponse;
import com.first.api.first_api.repositories.ClienteRepository;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.mappers.ClienteMapper;

@SuppressWarnings("null")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clientePrueba;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        clientePrueba = new Cliente();
        clientePrueba.setId(1L);
        clientePrueba.setNombre("Juan");
        clientePrueba.setApellido("Perez");
        clientePrueba.setDni("12345678");
        clientePrueba.setEmail("juan@ejemplo.com");
        clientePrueba.setActivo(true);
    }

    @Test
    void shouldReturnClienteDtoWhenIdExists() {
        // ARRANGE
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clientePrueba));
        
        ClienteResponse dtoPrueba = new ClienteResponse();
        dtoPrueba.setNombre("Juan");
        dtoPrueba.setDni("12345678");
        when(clienteMapper.toResponse(clientePrueba)).thenReturn(dtoPrueba);

        // ACT
        Optional<ClienteResponse> resultado = clienteService.buscarPorId(1L);

        // ASSERT
        assertTrue(resultado.isPresent());
        assertEquals("Juan", resultado.get().getNombre());
        assertEquals("12345678", resultado.get().getDni());
        verify(clienteRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenBajaLogicaFails() {
        // ARRANGE
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.bajaLogica(999L);
        });

        assertEquals("Cliente no encontrado con id: 999", exception.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }
}