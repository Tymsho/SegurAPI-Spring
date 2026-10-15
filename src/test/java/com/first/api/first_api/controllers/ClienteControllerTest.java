package com.first.api.first_api.controllers;

import com.first.api.first_api.dtorequest.ClienteRequest;
import com.first.api.first_api.dtoresponse.ClienteResponse;
import com.first.api.first_api.exceptions.DuplicateResourceException;
import com.first.api.first_api.exceptions.GlobalExceptionHandler;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.services.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import com.first.api.first_api.security.JwtAuthenticationFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@Import({JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
@SuppressWarnings("null")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private com.first.api.first_api.security.JwtUtils jwtUtils;

    @MockitoBean
    private com.first.api.first_api.security.UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenClienteNotFound() throws Exception {
        // ARRANGE
        doThrow(new ResourceNotFoundException("Cliente no encontrado con id: 999"))
                .when(clienteService).bajaLogica(999L);

        // ACT & ASSERT
        mockMvc.perform(delete("/api/clientes/999")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cliente no encontrado con id: 999"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenClienteInvalido() throws Exception {
        // ARRANGE
        String jsonInvalido = """
                {
                    "nombre": "",
                    "email": "esto-no-es-un-mail"
                }
                """;

        // ACT & ASSERT
        mockMvc.perform(post("/api/clientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").exists())
                .andExpect(jsonPath("$.apellido").exists())
                .andExpect(jsonPath("$.dni").exists())
                .andExpect(jsonPath("$.email").value("El formato del email es inválido"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn201WhenCreateClienteSuccess() throws Exception {
        // ARRANGE
        String jsonValido = """
                {
                    "nombre": "Juan",
                    "apellido": "Perez",
                    "fechaNacimiento": "1990-01-01",
                    "localidadId": 1,
                    "email": "juan@test.com",
                    "dni": "12345678",
                    "sexo": "MASCULINO",
                    "tipoIva": "CONSUMIDOR_FINAL"
                }
                """;

        ClienteResponse responseMock = new ClienteResponse();
        responseMock.setId(1L);
        responseMock.setNombre("Juan");
        responseMock.setApellido("Perez");
        responseMock.setDni("12345678");
        responseMock.setEmail("juan@test.com");

        when(clienteService.crearCliente(any(ClienteRequest.class))).thenReturn(responseMock);

        // ACT & ASSERT
        mockMvc.perform(post("/api/clientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.dni").value("12345678"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenClienteDniDuplicado() throws Exception {
        // ARRANGE
        String jsonValido = """
                {
                    "nombre": "Juan",
                    "apellido": "Perez",
                    "fechaNacimiento": "1990-01-01",
                    "localidadId": 1,
                    "email": "juan@test.com",
                    "dni": "12345678",
                    "sexo": "MASCULINO",
                    "tipoIva": "CONSUMIDOR_FINAL"
                }
                """;

        when(clienteService.crearCliente(any(ClienteRequest.class)))
                .thenThrow(new DuplicateResourceException("Ya tenés un cliente registrado con este DNI"));

        // ACT & ASSERT
        mockMvc.perform(post("/api/clientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya tenés un cliente registrado con este DNI"));
    }
}