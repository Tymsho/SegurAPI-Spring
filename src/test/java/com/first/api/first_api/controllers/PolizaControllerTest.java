package com.first.api.first_api.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import com.first.api.first_api.services.PolizaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolizaController.class)
class PolizaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PolizaService polizaService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenPolizaInvalida() throws Exception {
        String jsonInvalido = "{}";

        mockMvc.perform(post("/api/polizas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.numeroPoliza").exists())
                .andExpect(jsonPath("$.fechaInicio").exists())
                .andExpect(jsonPath("$.fechaFin").exists())
                .andExpect(jsonPath("$.clienteId").exists())
                .andExpect(jsonPath("$.companiaId").exists())
                .andExpect(jsonPath("$.ramoId").exists());
    }
}