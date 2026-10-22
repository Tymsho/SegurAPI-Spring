package com.first.api.first_api.controllers;

import com.first.api.first_api.dtorequest.PolizaRequest;
import com.first.api.first_api.dtoresponse.PolizaResponse;
import com.first.api.first_api.exceptions.GlobalExceptionHandler;
import com.first.api.first_api.security.JwtAuthenticationFilter;
import com.first.api.first_api.services.FileStorageService;
import com.first.api.first_api.services.PolizaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolizaController.class)
@Import({JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
@SuppressWarnings("null")
class PolizaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PolizaService polizaService;

    @MockitoBean
    private FileStorageService fileStorageService;

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

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn201WhenCrearPolizaExitoso() throws Exception {
        String jsonValido = """
                {
                    "nroPza": "POL-1001",
                    "clienteId": 1,
                    "tipoPago": "EFECTIVO",
                    "inicioVigencia": "2026-01-01",
                    "finVigencia": "2027-01-01",
                    "ramoId": 1,
                    "companiaId": 1,
                    "tipoFacturacion": "MENSUAL",
                    "premio": 1200.0
                }
                """;

        PolizaResponse response = new PolizaResponse();
        response.setId(1L);
        response.setNroPza("POL-1001");

        when(polizaService.guardarPoliza(any(PolizaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/polizas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nroPza").value("POL-1001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn200WhenPolizaExiste() throws Exception {
        PolizaResponse response = new PolizaResponse();
        response.setId(1L);
        response.setNroPza("POL-1001");

        when(polizaService.buscarPorId(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/polizas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nroPza").value("POL-1001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenPolizaNoExiste() throws Exception {
        when(polizaService.buscarPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/polizas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnPolizasWithDefaultSortAndFilter() throws Exception {
        org.springframework.data.domain.Page<PolizaResponse> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of());
        when(polizaService.obtenerMisPolizasActivas(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/polizas")
                .param("ocultarPremioCero", "true"))
                .andExpect(status().isOk());
    }
}