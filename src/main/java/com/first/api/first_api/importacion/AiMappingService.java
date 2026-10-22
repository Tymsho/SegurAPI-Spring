package com.first.api.first_api.importacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Servicio para mapear inteligentemente las columnas y ramos de planillas de aseguradoras
 * argentinas mediante una base heurística con diccionario local y enriquecimiento con
 * Google Gemini Flash API (gemini-2.5-flash) con tolerancia total a fallos.
 */
@Service
public class AiMappingService {

    private static final Logger log = LoggerFactory.getLogger(AiMappingService.class);

    private static final Map<String, String> DICT_RAMOS = Map.ofEntries(
        Map.entry("2", "Transporte (Cascos y Carga)"),
        Map.entry("4", "Automotores"),
        Map.entry("4.0", "Automotores"),
        Map.entry("04", "Automotores"),
        Map.entry("AUTOMOTORES", "Automotores"),
        Map.entry("7", "Combinado Familiar"),
        Map.entry("7.0", "Combinado Familiar"),
        Map.entry("07", "Combinado Familiar"),
        Map.entry("COMBINADO FAMILIAR", "Combinado Familiar"),
        Map.entry("8", "Responsabilidad Civil"),
        Map.entry("80.0", "Responsabilidad Civil"),
        Map.entry("RESP.CIVIL PROFESIONAL", "Responsabilidad Civil"),
        Map.entry("RESPONSABILIDAD CIVIL", "Responsabilidad Civil"),
        Map.entry("9", "Incendio"),
        Map.entry("09", "Incendio"),
        Map.entry("INCENDIO", "Incendio"),
        Map.entry("10", "Accidentes Personales (AP)"),
        Map.entry("10.0", "Accidentes Personales (AP)"),
        Map.entry("12", "Accidentes Personales (AP)"),
        Map.entry("35", "Accidentes Personales (AP)"),
        Map.entry("ACCIDENTES PERSONALES", "Accidentes Personales (AP)"),
        Map.entry("13", "Caución"),
        Map.entry("CAUCION", "Caución"),
        Map.entry("17", "Vida Obligatorio"),
        Map.entry("VIDA OBLIGATORIO", "Vida Obligatorio"),
        Map.entry("18", "Vida Colectivo"),
        Map.entry("VIDA COLECTIVO", "Vida Colectivo"),
        Map.entry("19", "Vida Individual"),
        Map.entry("VIDA INDIVIDUAL", "Vida Individual"),
        Map.entry("36", "Salud"),
        Map.entry("SALUD", "Salud"),
        Map.entry("37", "Técnico"),
        Map.entry("TECNICO", "Técnico"),
        Map.entry("44", "Motovehículos"),
        Map.entry("MOTOS", "Motovehículos"),
        Map.entry("MOTOVEHICULOS", "Motovehículos"),
        Map.entry("80", "Riesgos del Trabajo (ART)"),
        Map.entry("RIESGOS DEL TRABAJO", "Riesgos del Trabajo (ART)"),
        Map.entry("ART", "Riesgos del Trabajo (ART)"),
        Map.entry("INTEGRAL DE COMERCIO", "Integral de Comercio / Consorcio"),
        Map.entry("COMBINADO DE CONSORCIOS", "Integral de Comercio / Consorcio")
    );

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String geminiModel;

    public ImportMappingConfigDTO buildMappingConfig(String companiaNombre,
                                                    int headerRowIndex,
                                                    List<String> headers,
                                                    List<Map<String, String>> sampleRows,
                                                    Set<String> uniqueRawRamos,
                                                    List<String> ramosOficiales) {

        // --- PASO A: Base Heurística Determinística ---
        ImportMappingConfigDTO config = buildHeuristicConfig(headerRowIndex, headers, uniqueRawRamos, ramosOficiales);

        // --- PASO B: Inferencia con Gemini Flash API si la clave está provista ---
        if (geminiApiKey == null || geminiApiKey.isBlank()
                || geminiApiKey.equals("TU_API_KEY_AQUI")
                || geminiApiKey.startsWith("tu_clave")) {
            log.info("Gemini API key no configurada. Mapeo generado mediante reglas heurísticas locales.");
            return config;
        }

        try {
            log.info("Invocando Gemini API ({}) para enriquecer el mapeo de columnas y ramos de '{}'...",
                    geminiModel, companiaNombre);

            String prompt = buildPrompt(companiaNombre, headers, sampleRows, uniqueRawRamos, ramosOficiales);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.1,
                    "responseMimeType", "application/json"
                )
            );

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(6000);
            factory.setReadTimeout(12000);

            RestClient restClient = RestClient.builder().requestFactory(factory).build();
            String responseStr = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(requestBody))
                .retrieve()
                .body(String.class);

            if (responseStr != null && !responseStr.isBlank()) {
                JsonNode root = objectMapper.readTree(responseStr);
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    String jsonText = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                    if (jsonText != null && !jsonText.isBlank()) {
                        applyAiMapping(config, jsonText, headers, ramosOficiales);
                        config.setGeneradoPorIa(true);
                        log.info("Mapeo enriquecido exitosamente por Gemini.");
                    }
                }
            }

        } catch (Exception ex) {
            log.warn("No fue posible consultar Gemini API ({}: {}). Manteniendo mapeo heurístico base.",
                    ex.getClass().getSimpleName(), ex.getMessage());
        }

        return config;
    }

    private ImportMappingConfigDTO buildHeuristicConfig(int headerRowIndex,
                                                       List<String> headers,
                                                       Set<String> uniqueRawRamos,
                                                       List<String> ramosOficiales) {
        Map<String, String> columnMapping = new LinkedHashMap<>();

        columnMapping.put("nroPoliza", findMatchingHeader(headers, List.of("PÓLIZA", "POLIZA", "Nro. Póliza", "NRO. POLIZA", "NRO POLIZA", "NUMERO POLIZA", "NRO DE POLIZA", "N POLIZA")));
        columnMapping.put("nroEndoso", findMatchingHeader(headers, List.of("END.ÚLT.FACT.", "END.ULT.FACT.", "ENDOSO", "Endoso", "Nro. Endoso", "NRO. ENDOSO", "NRO ENDOSO", "END")));
        columnMapping.put("tomadorNombre", findMatchingHeader(headers, List.of("TOMADOR", "Contratante", "Tomador", "NOMBRE TOMADOR", "TITULAR", "APELLIDO Y NOMBRE", "NOMBRE Y APELLIDO", "CLIENTE", "RAZON SOCIAL")));
        columnMapping.put("aseguradoNombre", findMatchingHeader(headers, List.of("ASEGURADO", "Asegurado", "NOMBRE ASEGURADO", "BENEFICIARIO")));
        columnMapping.put("documento", findMatchingHeader(headers, List.of("NRO. DOCUMENTO", "NRO DOCUMENTO", "Nro. Doc.", "Datos Tomador", "CUIT", "CUIL", "DNI", "DOCUMENTO", "IDENTIFICACION")));
        columnMapping.put("domicilio", findMatchingHeader(headers, List.of("DOMICILIO", "Dirección asegurados", "DIRECCION", "CALLE", "DOMICILIO TOMADOR")));
        columnMapping.put("localidad", findMatchingHeader(headers, List.of("LOCALIDAD", "Localidad", "CIUDAD")));
        columnMapping.put("provincia", findMatchingHeader(headers, List.of("PROVINCIA", "Provincia")));
        columnMapping.put("telefono", findMatchingHeader(headers, List.of("TELÉFONO", "TELEFONO", "Teléfono", "CELULAR", "TEL")));
        columnMapping.put("fechaNacimiento", findMatchingHeader(headers, List.of("FEC.NAC.BENEF", "FEC. NAC. BENEF", "FECHA NACIMIENTO", "FEC. NAC.", "FEC NAC", "FECHA NAC")));
        columnMapping.put("fechaEmision", findMatchingHeader(headers, List.of("Suscripción", "Suscripcion", "SUSCRIPCIÓN", "SUSCRIPCION", "Fecha de Emisión", "Fecha de Emision", "FECHA DE EMISIÓN", "FECHA DE EMISION", "Emisión", "Emision", "EMISIÓN", "EMISION", "FEC.EMISION", "FEC. EMISION")));
        columnMapping.put("inicioVigencia", findMatchingHeader(headers, List.of("Vig. desde", "Vigencia Desde", "INICIO VIGENCIA", "VIGENCIA DESDE", "FECHA INICIO", "DESDE")));
        columnMapping.put("finVigencia", findMatchingHeader(headers, List.of("Vig. hasta", "Vigencia Hasta", "FIN VIGENCIA", "VIGENCIA HASTA", "FECHA FIN", "HASTA")));
        columnMapping.put("anualidad", findMatchingHeader(headers, List.of("ANUALIDAD", "Anualidad", "ANTIGUEDAD", "Antigüedad", "AÑOS")));
        columnMapping.put("ramoPrincipal", findMatchingHeader(headers, List.of("SECCIÓN", "SECCION", "Ramo", "RAMO", "RAMO PRINCIPAL")));
        columnMapping.put("ramoSecundarioPlan", findMatchingHeader(headers, List.of("Plan Cobertura", "PLAN", "COBERTURA", "SUBRAMO")));
        columnMapping.put("bienAsegurado", findMatchingHeader(headers, List.of("Modelo", "Ubicación riesgo", "UBICACION RIESGO", "BIEN ASEGURADO", "RIESGO", "VEHICULO", "PATENTE", "DOMINIO")));
        columnMapping.put("tipoFacturacion", findMatchingHeader(headers, List.of("TIPO FACT.", "TIPO FACT", "TIPO FACTURACION", "FACTURACION", "PERIODICIDAD")));
        columnMapping.put("tipoPago", findMatchingHeader(headers, List.of("FORMA DE COBRO", "Conducto de Pago", "TIPO PAGO", "FORMA PAGO", "MEDIO DE PAGO")));
        columnMapping.put("premio", findMatchingHeader(headers, List.of("PREMIO ÚLT.FACT.", "PREMIO ULT.FACT.", "Premio", "PREMIO", "TOTAL PREMIO", "IMPORTE PREMIO", "PREMIO TOTAL")));
        columnMapping.put("premioFallbackCentavos", findMatchingHeader(headers, List.of("PREMIO FACT.ANT.", "PREMIO FACT ANT", "PREMIO ANTERIOR")));
        columnMapping.put("sumaAsegurada", findMatchingHeader(headers, List.of("SUMA ASEG.ÚLT.FACT.", "SUMA ASEG.ULT.FACT.", "Suma asegurada ($)", "Suma", "SUMA ASEGURADA", "CAPITAL ASEGURADO")));

        // Reglas específicas Mapfre
        String fechaVig = findHeaderByExactOrClean(headers, "FECHA VIG.");
        if (fechaVig != null && columnMapping.get("finVigencia") == null) {
            columnMapping.put("finVigencia", fechaVig);
        }

        String seccionCol = findHeaderByExactOrClean(headers, "SECCION");
        String ramoCol = findHeaderByExactOrClean(headers, "RAMO");
        if (seccionCol != null && ramoCol != null) {
            columnMapping.put("ramoPrincipal", seccionCol);
            columnMapping.put("ramoSecundarioPlan", ramoCol);
            columnMapping.put("bienAsegurado", ramoCol);
        }

        // Mapeo de Ramos
        Map<String, String> ramoMappings = new LinkedHashMap<>();
        if (uniqueRawRamos != null) {
            for (String raw : uniqueRawRamos) {
                if (raw == null || raw.isBlank()) continue;
                String clean = ArgentineDataNormalizer.stripAccentsAndClean(raw.trim());

                if (DICT_RAMOS.containsKey(clean)) {
                    ramoMappings.put(raw, DICT_RAMOS.get(clean));
                } else if (DICT_RAMOS.containsKey(raw.trim())) {
                    ramoMappings.put(raw, DICT_RAMOS.get(raw.trim()));
                } else {
                    // Match con ramos oficiales
                    for (String oficial : ramosOficiales) {
                        String ofClean = ArgentineDataNormalizer.stripAccentsAndClean(oficial);
                        if (ofClean.equalsIgnoreCase(clean)) {
                            ramoMappings.put(raw, oficial);
                            break;
                        }
                    }
                }
            }
        }

        return ImportMappingConfigDTO.builder()
            .headerRowIndex(headerRowIndex)
            .generadoPorIa(false)
            .columnMapping(columnMapping)
            .ramoMappings(ramoMappings)
            .build();
    }

    private void applyAiMapping(ImportMappingConfigDTO config, String jsonText,
                                List<String> headers, List<String> ramosOficiales) {
        try {
            JsonNode aiRoot = objectMapper.readTree(jsonText);
            JsonNode aiColMapping = aiRoot.path("columnMapping");
            if (aiColMapping.isObject()) {
                aiColMapping.fields().forEachRemaining(entry -> {
                    String standardKey = entry.getKey();
                    String mappedHeader = entry.getValue().isNull() ? null : entry.getValue().asText();
                    if (mappedHeader != null && headers.contains(mappedHeader)) {
                        // Preservar reglas críticas previas si ya estaban definidas
                        if (!"premioFallbackCentavos".equals(standardKey) || config.getColumnMapping().get(standardKey) == null) {
                            config.getColumnMapping().put(standardKey, mappedHeader);
                        }
                    }
                });
            }

            JsonNode aiRamoMappings = aiRoot.path("ramoMappings");
            if (aiRamoMappings.isObject()) {
                aiRamoMappings.fields().forEachRemaining(entry -> {
                    String rawRamo = entry.getKey();
                    String mappedOficial = entry.getValue().isNull() ? null : entry.getValue().asText();
                    if (mappedOficial != null && ramosOficiales.contains(mappedOficial)) {
                        config.getRamoMappings().put(rawRamo, mappedOficial);
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Error al procesar el JSON devuelto por Gemini: {}. Se conserva la base heurística.", e.getMessage());
        }
    }

    private String buildPrompt(String companiaNombre, List<String> headers,
                               List<Map<String, String>> sampleRows,
                               Set<String> uniqueRawRamos, List<String> ramosOficiales) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un experto en liquidaciones de seguros y carteras de aseguradoras en Argentina.\n");
        sb.append("Tu tarea es analizar las columnas y datos de muestra de una aseguradora y generar el mapeo hacia las columnas estándar del sistema y la equivalencia de ramos.\n\n");
        sb.append("Aseguradora: ").append(companiaNombre != null ? companiaNombre : "No especificada").append("\n\n");
        sb.append("Columnas detectadas en el archivo Excel:\n").append(headers).append("\n\n");

        sb.append("Primeras filas de muestra (hasta 3):\n");
        if (sampleRows != null && !sampleRows.isEmpty()) {
            int count = 0;
            for (Map<String, String> row : sampleRows) {
                if (++count > 3) break;
                sb.append("Fila ").append(count).append(": ");
                Map<String, String> anon = new LinkedHashMap<>();
                row.forEach((k, v) -> {
                    if (v != null) {
                        String s = v.trim();
                        if (s.length() > 60) s = s.substring(0, 57) + "...";
                        if (s.matches("\\d{7,11}")) {
                            s = s.substring(0, 3) + "****" + s.substring(s.length() - 2);
                        }
                        anon.put(k, s);
                    }
                });
                sb.append(anon).append("\n");
            }
        } else {
            sb.append("No hay filas de muestra disponibles.\n");
        }
        sb.append("\n");

        sb.append("Valores únicos de ramo encontrados en el archivo que requieren equivalencia:\n");
        sb.append(uniqueRawRamos != null ? uniqueRawRamos : "[]").append("\n\n");

        sb.append("Lista cerrada de Ramos Oficiales del sistema (DEBES ELEGIR EXACTAMENTE UNO DE ESTOS O DEJAR NULL):\n");
        sb.append(ramosOficiales).append("\n\n");

        sb.append("Campos estándar del sistema:\n");
        sb.append("- nroPoliza: Número de póliza\n");
        sb.append("- nroEndoso: Número de endoso o certificado (ej. END.ÚLT.FACT., Endoso)\n");
        sb.append("- tomadorNombre: Nombre o Razón Social del tomador o contratante\n");
        sb.append("- aseguradoNombre: Nombre del asegurado si difiere del tomador\n");
        sb.append("- documento: CUIT, CUIL o DNI del tomador o asegurado\n");
        sb.append("- domicilio: Dirección o domicilio\n");
        sb.append("- localidad: Localidad o ciudad\n");
        sb.append("- provincia: Provincia\n");
        sb.append("- telefono: Teléfono o celular\n");
        sb.append("- fechaNacimiento: Fecha de nacimiento\n");
        sb.append("- fechaEmision: Fecha original de suscripción o emisión (ej. Suscripción, Fecha de Emisión)\n");
        sb.append("- inicioVigencia: Fecha de inicio de vigencia de la póliza\n");
        sb.append("- finVigencia: Fecha de fin de vigencia de la póliza\n");
        sb.append("- anualidad: Años de antigüedad o anualidad en Mapfre (ej. ANUALIDAD)\n");
        sb.append("- ramoPrincipal: Columna de ramo o sección principal\n");
        sb.append("- ramoSecundarioPlan: Plan de cobertura o subramo\n");
        sb.append("- bienAsegurado: Modelo de vehículo, patente, o ubicación de riesgo asegurado\n");
        sb.append("- tipoFacturacion: Frecuencia o tipo de facturación\n");
        sb.append("- tipoPago: Forma o conducto de pago/cobro\n");
        sb.append("- premio: Monto de premio total\n");
        sb.append("- premioFallbackCentavos: Monto de premio anterior o secundario si existiera\n");
        sb.append("- sumaAsegurada: Suma o capital asegurado\n\n");

        sb.append("Reglas estrictas:\n");
        sb.append("1. Si un campo no existe en el archivo, su valor debe ser null.\n");
        sb.append("2. Los valores de columnMapping DEBEN ser exactamente el nombre de alguna de las columnas del archivo provistas.\n");
        sb.append("3. Los valores de ramoMappings DEBEN ser exactamente uno de los Ramos Oficiales provistos.\n");
        sb.append("4. Devuelve ÚNICAMENTE un objeto JSON válido con las claves \"columnMapping\" y \"ramoMappings\".\n");

        return sb.toString();
    }

    private String findMatchingHeader(List<String> headers, List<String> patterns) {
        if (headers == null || patterns == null) return null;

        // Fase 1: Coincidencia exacta limpia
        for (String p : patterns) {
            String pClean = ArgentineDataNormalizer.stripAccentsAndClean(p);
            for (String h : headers) {
                if (h == null) continue;
                String hClean = ArgentineDataNormalizer.stripAccentsAndClean(h);
                if (hClean.equalsIgnoreCase(pClean)) {
                    return h;
                }
            }
        }

        // Fase 2: Prefijo o palabra contenida
        for (String p : patterns) {
            String pClean = ArgentineDataNormalizer.stripAccentsAndClean(p);
            for (String h : headers) {
                if (h == null) continue;
                String hClean = ArgentineDataNormalizer.stripAccentsAndClean(h);
                if (hClean.startsWith(pClean + " ") || hClean.endsWith(" " + pClean) || hClean.contains(" " + pClean + " ")) {
                    return h;
                }
            }
        }

        return null;
    }

    private String findHeaderByExactOrClean(List<String> headers, String target) {
        if (headers == null || target == null) return null;
        String cleanTarget = ArgentineDataNormalizer.stripAccentsAndClean(target);
        for (String h : headers) {
            if (h == null) continue;
            if (ArgentineDataNormalizer.stripAccentsAndClean(h).equalsIgnoreCase(cleanTarget)) {
                return h;
            }
        }
        return null;
    }
}
