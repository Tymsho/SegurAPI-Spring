package com.first.api.first_api.controllers;

import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.services.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Import para simular usuario
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Import para token
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    @WithMockUser(roles = "ADMIN") // Simulamos que somos ADMIN
    void shouldReturn404WhenClienteNotFound() throws Exception {
        // ARRANGE
        doThrow(new ResourceNotFoundException("Cliente no encontrado con id: 999"))
                .when(clienteService).bajaLogica(999L);

        // ACT & ASSERT
        mockMvc.perform(delete("/api/clientes/999")
                .with(csrf())) // Pasamos el token de seguridad
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cliente no encontrado con id: 999"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Simulamos que somos ADMIN
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
                .with(csrf()) // Pasamos el token de seguridad
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").exists())
                .andExpect(jsonPath("$.apellido").exists())
                .andExpect(jsonPath("$.dniCuit").exists())
                .andExpect(jsonPath("$.email").value("Formato de email inválido"));
    }
}