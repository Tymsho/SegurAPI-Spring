package com.first.api.first_api.importacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AiMappingServiceTest {

    private AiMappingService aiMappingService;
    private final List<String> ramosOficiales = List.of(
        "Automotores", "Motovehículos", "Combinado Familiar", "Integral de Comercio / Consorcio",
        "Incendio", "Transporte (Cascos y Carga)", "Caución", "Responsabilidad Civil",
        "Riesgos Agrícolas / Granizo", "Técnico", "Crédito", "Vida Individual",
        "Vida Colectivo", "Vida Obligatorio", "Accidentes Personales (AP)", "Retiro",
        "Sepelio", "Riesgos del Trabajo (ART)", "Salud"
    );

    @BeforeEach
    void setUp() {
        aiMappingService = new AiMappingService();
        ReflectionTestUtils.setField(aiMappingService, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(aiMappingService, "geminiApiKey", "");
        ReflectionTestUtils.setField(aiMappingService, "geminiModel", "gemini-2.5-flash");
    }

    @Test
    void shouldBuildHeuristicMappingForFederacionPatronalFormat() {
        List<String> headers = List.of(
            "PÓLIZA", "END.ÚLT.FACT.", "TOMADOR", "NRO. DOCUMENTO", "DOMICILIO",
            "LOCALIDAD", "PROVINCIA", "SECCIÓN", "PREMIO ÚLT.FACT.", "PREMIO FACT.ANT.",
            "Vig. desde", "Vig. hasta"
        );
        Set<String> uniqueRamos = Set.of("4", "8", "13", "36");

        ImportMappingConfigDTO config = aiMappingService.buildMappingConfig(
            "Federación Patronal", 0, headers, Collections.emptyList(), uniqueRamos, ramosOficiales
        );

        assertNotNull(config);
        assertFalse(config.isGeneradoPorIa());
        assertEquals("PÓLIZA", config.getColumnMapping().get("nroPoliza"));
        assertEquals("END.ÚLT.FACT.", config.getColumnMapping().get("nroEndoso"));
        assertEquals("TOMADOR", config.getColumnMapping().get("tomadorNombre"));
        assertEquals("NRO. DOCUMENTO", config.getColumnMapping().get("documento"));
        assertEquals("PREMIO ÚLT.FACT.", config.getColumnMapping().get("premio"));
        assertEquals("PREMIO FACT.ANT.", config.getColumnMapping().get("premioFallbackCentavos"));
        assertEquals("Vig. desde", config.getColumnMapping().get("inicioVigencia"));
        assertEquals("Vig. hasta", config.getColumnMapping().get("finVigencia"));

        // Verificar mapeo de códigos numéricos de ramo
        assertEquals("Automotores", config.getRamoMappings().get("4"));
        assertEquals("Responsabilidad Civil", config.getRamoMappings().get("8"));
        assertEquals("Caución", config.getRamoMappings().get("13"));
        assertEquals("Salud", config.getRamoMappings().get("36"));
    }

    @Test
    void shouldBuildHeuristicMappingForMapfreSpecialRules() {
        List<String> headers = List.of(
            "Póliza", "Endoso", "Tomador", "Nro. Doc.", "SECCIÓN", "RAMO",
            "FECHA VIG.", "Premio", "Suma asegurada ($)"
        );
        Set<String> uniqueRamos = Set.of("AUTOMOTORES", "COMBINADO FAMILIAR");

        ImportMappingConfigDTO config = aiMappingService.buildMappingConfig(
            "Mapfre Argentina", 0, headers, Collections.emptyList(), uniqueRamos, ramosOficiales
        );

        assertNotNull(config);
        assertEquals("FECHA VIG.", config.getColumnMapping().get("finVigencia"));
        assertEquals("SECCIÓN", config.getColumnMapping().get("ramoPrincipal"));
        assertEquals("RAMO", config.getColumnMapping().get("ramoSecundarioPlan"));
        assertEquals("RAMO", config.getColumnMapping().get("bienAsegurado"));

        assertEquals("Automotores", config.getRamoMappings().get("AUTOMOTORES"));
        assertEquals("Combinado Familiar", config.getRamoMappings().get("COMBINADO FAMILIAR"));
    }

    @Test
    void shouldHandleMissingGeminiKeyGracefullyWithoutException() {
        ReflectionTestUtils.setField(aiMappingService, "geminiApiKey", "TU_API_KEY_AQUI");

        List<String> headers = List.of("Póliza", "Tomador", "Premio");
        ImportMappingConfigDTO config = aiMappingService.buildMappingConfig(
            "Compañía X", 0, headers, Collections.emptyList(), Collections.emptySet(), ramosOficiales
        );

        assertNotNull(config);
        assertFalse(config.isGeneradoPorIa());
        assertEquals("Póliza", config.getColumnMapping().get("nroPoliza"));
    }

    @Test
    void shouldMapFechaEmisionAndAnualidadAutomatically() {
        List<String> federacionHeaders = List.of(
            "PÓLIZA", "TOMADOR", "Suscripción", "Vig. desde", "Vig. hasta", "PREMIO"
        );
        ImportMappingConfigDTO fedConfig = aiMappingService.buildMappingConfig(
            "Federación Patronal", 0, federacionHeaders, Collections.emptyList(), Collections.emptySet(), ramosOficiales
        );
        assertEquals("Suscripción", fedConfig.getColumnMapping().get("fechaEmision"));

        List<String> mapfreHeaders = List.of(
            "POLIZA", "TOMADOR", "FECHA VIG.", "ANUALIDAD", "PREMIO"
        );
        ImportMappingConfigDTO mapfreConfig = aiMappingService.buildMappingConfig(
            "Mapfre", 0, mapfreHeaders, Collections.emptyList(), Collections.emptySet(), ramosOficiales
        );
        assertEquals("ANUALIDAD", mapfreConfig.getColumnMapping().get("anualidad"));
        assertEquals("FECHA VIG.", mapfreConfig.getColumnMapping().get("finVigencia"));
    }
}
