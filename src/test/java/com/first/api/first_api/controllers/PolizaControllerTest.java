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
import org.springframework.context.annotation.Import;
import com.first.api.first_api.security.JwtAuthenticationFilter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolizaController.class)
@Import(JwtAuthenticationFilter.class)
@SuppressWarnings("null")
class PolizaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PolizaService polizaService;

    @MockitoBean
    private com.first.api.first_api.security.JwtUtils jwtUtils;

    @MockitoBean
    private com.first.api.first_api.security.UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenPolizaInvalida() throws Exception {
        String jsonInvalido = "{}";

        mockMvc.perform(post("/api/polizas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nroPza").exists())
                .andExpect(jsonPath("$.inicioVigencia").exists())
                .andExpect(jsonPath("$.finVigencia").exists())
                .andExpect(jsonPath("$.clienteId").exists())
                .andExpect(jsonPath("$.companiaId").exists())
                .andExpect(jsonPath("$.ramoId").exists());
    }
}