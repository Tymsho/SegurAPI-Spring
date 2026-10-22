package com.first.api.first_api.importacion;

import com.first.api.first_api.models.TipoPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ExcelCsvParserServiceTest {

    private ExcelCsvParserService parser;

    @BeforeEach
    void setUp() {
        parser = new ExcelCsvParserService();
    }

    @Test
    void shouldDetectHeaderRowIndex() {
        List<List<String>> rows = List.of(
            List.of("Titulo", "Reporte de Cartera", "", ""),
            List.of("Fecha", "2026-10-21", "", ""),
            List.of("Póliza", "Endoso", "Tomador", "Ramo", "Premio")
        );
        assertEquals(2, parser.detectHeaderRowIndex(rows));
    }

    @Test
    void shouldParseFlexibleDate() {
        assertEquals(LocalDate.of(2026, 7, 12), parser.parseFlexibleDate("2026-07-12"));
        assertEquals(LocalDate.of(2026, 7, 12), parser.parseFlexibleDate("12/07/2026"));
        assertEquals(LocalDate.of(2026, 7, 12), parser.parseFlexibleDate("2026-07-12 12:00:00"));
        assertEquals(LocalDate.of(1945, 5, 6), parser.parseFlexibleDate("06/05/1945 12:00:00 a.m."));
        assertNull(parser.parseFlexibleDate(null));
        assertNull(parser.parseFlexibleDate("invalido"));
    }

    @Test
    void shouldParseMonetaryAmount() {
        assertEquals(12345.67, parser.parseMonetaryAmount("$ 12.345,67"));
        assertEquals(-63762.21, parser.parseMonetaryAmount("-$ 63.762,21"));
        assertEquals(1000.0, parser.parseMonetaryAmount("1000"));
        assertEquals(-500.50, parser.parseMonetaryAmount("(500,50)"));
        assertNull(parser.parseMonetaryAmount(""));
    }

    @Test
    void shouldParseDoubleValueBothDotAndArgentineFormats() {
        assertEquals(74164.14, parser.parseDoubleValue("74164.14"));
        assertEquals(129703.89, parser.parseDoubleValue("129703.89"));
        assertEquals(74164.14, parser.parseDoubleValue("74.164,14"));
        assertEquals(74164.14, parser.parseDoubleValue("74164,14"));
        assertEquals(1234567.89, parser.parseDoubleValue("1.234.567,89"));
        assertEquals(6745661.0, parser.parseDoubleValue("6745661"));
        assertNull(parser.parseDoubleValue(null));
        assertNull(parser.parseDoubleValue("   "));
    }

    @Test
    void shouldFindColumnCaseInsensitiveAndAccents() {
        Map<String, String> row = new HashMap<>();
        row.put("PREMIO ÚLT.FACT.", "74164.14");
        row.put("Póliza", "POL-999");
        row.put("Suscripción", "2015-05-20");

        assertEquals("74164.14", parser.getVal(row, "PREMIO ÚLT.FACT."));
        assertEquals("74164.14", parser.getVal(row, "premio ult.fact."));
        assertEquals("74164.14", parser.getVal(row, "PREMIO ULT.FACT."));
        assertEquals("74164.14", parser.getVal(row, " PREMIO ÚLT.FACT. "));
        assertEquals("POL-999", parser.getVal(row, "POLIZA"));
        assertEquals("POL-999", parser.getVal(row, "poliza"));
        assertEquals("2015-05-20", parser.getVal(row, "suscripcion"));
        assertEquals("2015-05-20", parser.getVal(row, "SUSCRIPCIÓN"));
    }

    @Test
    void shouldDivideMapfreFallbackCentavosBy100Strictly() {
        ImportMappingConfigDTO config = ImportMappingConfigDTO.builder()
            .columnMapping(Map.of(
                "nroPoliza", "POLIZA",
                "premio", "PREMIO ÚLT.FACT.",
                "premioFallbackCentavos", "PREMIO FACT.ANT."
            ))
            .build();

        Map<String, String> row1 = Map.of(
            "POLIZA", "MAP-001",
            "PREMIO ÚLT.FACT.", "0",
            "PREMIO FACT.ANT.", "6745661"
        );
        var item1 = parser.mapRowToPolizaItem(row1, config);
        assertNotNull(item1);
        assertEquals(67456.61, item1.getPremio());

        Map<String, String> row2 = Map.of(
            "POLIZA", "MAP-002",
            "PREMIO ÚLT.FACT.", "",
            "PREMIO FACT.ANT.", "13144995"
        );
        var item2 = parser.mapRowToPolizaItem(row2, config);
        assertNotNull(item2);
        assertEquals(131449.95, item2.getPremio());
    }

    @Test
    void shouldCalculateHistoricalDatesForMapfreAndFederacion() {
        // Mapfre con ANUALIDAD = 17 y FECHA VIG. = 2026-10-15
        ImportMappingConfigDTO mapfreConfig = ImportMappingConfigDTO.builder()
            .columnMapping(Map.of(
                "nroPoliza", "POLIZA",
                "finVigencia", "FECHA VIG.",
                "anualidad", "ANUALIDAD"
            ))
            .build();

        Map<String, String> mapfreRow = Map.of(
            "POLIZA", "MAP-HIST-1",
            "FECHA VIG.", "2026-10-15",
            "ANUALIDAD", "17"
        );
        var mapfreItem = parser.mapRowToPolizaItem(mapfreRow, mapfreConfig);
        assertNotNull(mapfreItem);
        assertEquals(LocalDate.of(2025, 10, 15), mapfreItem.getInicioVigencia());
        assertEquals(LocalDate.of(2026, 10, 15), mapfreItem.getFinVigencia());
        assertEquals(LocalDate.of(2009, 10, 15), mapfreItem.getFechaEmision());

        // Federación con Suscripción
        ImportMappingConfigDTO fedConfig = ImportMappingConfigDTO.builder()
            .columnMapping(Map.of(
                "nroPoliza", "PÓLIZA",
                "inicioVigencia", "Vig. desde",
                "finVigencia", "Vig. hasta",
                "fechaEmision", "Suscripción"
            ))
            .build();

        Map<String, String> fedRow = Map.of(
            "PÓLIZA", "FED-HIST-1",
            "Vig. desde", "2025-03-10",
            "Vig. hasta", "2026-03-10",
            "Suscripción", "2010-03-10"
        );
        var fedItem = parser.mapRowToPolizaItem(fedRow, fedConfig);
        assertNotNull(fedItem);
        assertEquals(LocalDate.of(2025, 3, 10), fedItem.getInicioVigencia());
        assertEquals(LocalDate.of(2026, 3, 10), fedItem.getFinVigencia());
        assertEquals(LocalDate.of(2010, 3, 10), fedItem.getFechaEmision());
    }

    @Test
    void shouldParseTipoPago() {
        assertEquals(TipoPago.EFECTIVO, parser.parseTipoPago("EFECTIVO"));
        assertEquals(TipoPago.EFECTIVO, parser.parseTipoPago("AG"));
        assertEquals(TipoPago.TARJETA_CREDITO, parser.parseTipoPago("TC VISA"));
        assertEquals(TipoPago.TARJETA_DEBITO, parser.parseTipoPago("DEBITO"));
        assertEquals(TipoPago.DEBITO_CBU, parser.parseTipoPago("DEBITO AUTOMATICO CBU"));
        assertEquals(TipoPago.NO_ESPECIFICADO, parser.parseTipoPago("OTRO"));
    }

    @Test
    void shouldSplitNombreApellido() {
        var pfConComa = parser.splitNombreApellido("SCHNEIDER, MACANERA LUJAN", false);
        assertEquals("Schneider", pfConComa.apellido());
        assertEquals("Macanera Lujan", pfConComa.nombre());

        var pfSinComa = parser.splitNombreApellido("DIAZ VEGA EDUARDO JUAN", false);
        assertEquals("Diaz", pfSinComa.apellido());
        assertEquals("Vega Eduardo Juan", pfSinComa.nombre());

        var pj = parser.splitNombreApellido("LA BUENA SUERTE S.A.", true);
        assertEquals("-", pj.nombre());
        assertEquals("La Buena Suerte S.a.", pj.apellido());
    }

    @Test
    void shouldCollapseRowsByPolizaWithCounterEndorsementCancel() {
        var item1 = ExcelCsvParserService.ParsedPolizaItem.builder()
            .nroPza("POL-100")
            .nroEndoso(0)
            .tomadorNombre("Perez Juan")
            .premio(63762.21)
            .sumaAsegurada(100000.0)
            .activo(true)
            .build();

        var item2 = ExcelCsvParserService.ParsedPolizaItem.builder()
            .nroPza("POL-100")
            .nroEndoso(1)
            .tomadorNombre("Perez Juan")
            .premio(-63762.21)
            .sumaAsegurada(0.0)
            .activo(true)
            .build();

        List<ExcelCsvParserService.ParsedPolizaItem> collapsed = parser.collapseRowsByPoliza(List.of(item1, item2));
        assertEquals(1, collapsed.size());
        assertFalse(collapsed.get(0).isActivo());
        assertEquals(63762.21, collapsed.get(0).getPremio());
    }

    @Test
    void shouldCollapseRowsByPolizaWithZeroEndorsementKeepingPreviousPositivePremio() {
        var item1 = ExcelCsvParserService.ParsedPolizaItem.builder()
            .nroPza("POL-200")
            .nroEndoso(0)
            .tomadorNombre("Gomez Carlos")
            .premio(25000.0)
            .activo(true)
            .build();

        var item2 = ExcelCsvParserService.ParsedPolizaItem.builder()
            .nroPza("POL-200")
            .nroEndoso(1)
            .tomadorNombre("Gomez Carlos")
            .premio(0.0)
            .bienAsegurado("Nuevo Accesorio")
            .activo(true)
            .build();

        List<ExcelCsvParserService.ParsedPolizaItem> collapsed = parser.collapseRowsByPoliza(List.of(item1, item2));
        assertEquals(1, collapsed.size());
        assertTrue(collapsed.get(0).isActivo());
        assertEquals(25000.0, collapsed.get(0).getPremio());
        assertEquals("Nuevo Accesorio", collapsed.get(0).getBienAsegurado());
    }

    @Test
    void shouldParseCsvFileCorrectly() throws IOException {
        String csv = """
                Póliza;Endoso;Tomador;Premio
                POL-CSV-1;0;Juan Perez;15000
                POL-CSV-2;0;Maria Lopez;22000
                """;

        var rawData = parser.parseFileToRawData(csv.getBytes(StandardCharsets.UTF_8), "test.csv");
        assertNotNull(rawData);
        assertEquals(4, rawData.getHeaders().size());
        assertEquals(2, rawData.getDataRows().size());
        assertEquals("POL-CSV-1", rawData.getDataRows().get(0).get("Póliza"));
    }
}
