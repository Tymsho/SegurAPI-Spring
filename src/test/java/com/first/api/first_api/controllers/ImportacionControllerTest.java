package com.first.api.first_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.first.api.first_api.exceptions.GlobalExceptionHandler;
import com.first.api.first_api.importacion.ImportAnalysisResponseDTO;
import com.first.api.first_api.importacion.ImportExecutionResultDTO;
import com.first.api.first_api.importacion.ImportMappingConfigDTO;
import com.first.api.first_api.importacion.ImportacionCarteraService;
import com.first.api.first_api.security.JwtAuthenticationFilter;
import com.first.api.first_api.security.JwtUtils;
import com.first.api.first_api.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImportacionController.class)
@Import({JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
@SuppressWarnings("null")
class ImportacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ImportacionCarteraService importacionCarteraService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "PRODUCTOR")
    void shouldReturn200WhenAnalizarExitoso() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "cartera.csv", "text/csv", "Poliza;Tomador\nPOL-1;Juan".getBytes()
        );

        ImportAnalysisResponseDTO response = ImportAnalysisResponseDTO.builder()
            .companiaId(1L)
            .companiaNombre("Sancor")
            .totalRawRows(1)
            .totalUniquePolizas(1)
            .build();

        when(importacionCarteraService.analizarArchivo(any(), eq(1L))).thenReturn(response);

        mockMvc.perform(multipart("/api/importacion/analizar")
                .file(file)
                .param("companiaId", "1")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.companiaId").value(1L))
            .andExpect(jsonPath("$.companiaNombre").value("Sancor"));
    }

    @Test
    @WithMockUser(roles = "PRODUCTOR")
    void shouldReturn200WhenEjecutarExitoso() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "cartera.csv", "text/csv", "Poliza;Tomador\nPOL-1;Juan".getBytes()
        );

        ImportMappingConfigDTO config = ImportMappingConfigDTO.builder()
            .headerRowIndex(0)
            .columnMapping(Collections.emptyMap())
            .ramoMappings(Collections.emptyMap())
            .build();

        ImportExecutionResultDTO result = ImportExecutionResultDTO.builder()
            .filasLeidas(1)
            .polizasUnicasProcesadas(1)
            .polizasCreadas(1)
            .build();

        when(importacionCarteraService.ejecutarImportacion(any(), eq(1L), any(ImportMappingConfigDTO.class))).thenReturn(result);

        mockMvc.perform(multipart("/api/importacion/ejecutar")
                .file(file)
                .param("companiaId", "1")
                .param("config", objectMapper.writeValueAsString(config))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.polizasCreadas").value(1))
            .andExpect(jsonPath("$.polizasUnicasProcesadas").value(1));
    }
}
