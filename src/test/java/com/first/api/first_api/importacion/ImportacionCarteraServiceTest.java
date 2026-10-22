package com.first.api.first_api.importacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.models.*;
import com.first.api.first_api.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ImportacionCarteraServiceTest {

    @Mock private CompaniaRepository companiaRepository;
    @Mock private PlantillaImportacionRepository plantillaImportacionRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PolizaRepository polizaRepository;
    @Mock private RamoRepository ramoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private LocalidadRepository localidadRepository;
    @Mock private ProvinciaRepository provinciaRepository;
    @Mock private GeografiaSyncService geografiaSyncService;
    @Mock private AiMappingService aiMappingService;
    @Mock private ExcelCsvParserService excelCsvParserService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private ImportacionCarteraService service;

    private Compania compania;
    private Usuario productor;

    @BeforeEach
    void setUp() {
        compania = new Compania();
        compania.setId(1L);
        compania.setNombre("Sancor Seguros");

        productor = new Usuario();
        productor.setId(10L);
        productor.setEmail("productor@mail.com");
    }

    @Test
    void shouldThrowExceptionWhenCompaniaNotFoundEnAnalizar() {
        when(companiaRepository.findById(999L)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);

        assertThrows(ResourceNotFoundException.class, () -> service.analizarArchivo(file, 999L));
    }

    @Test
    void shouldAnalizarArchivoUsingCachedPlantilla() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "col1,col2".getBytes(StandardCharsets.UTF_8));
        when(companiaRepository.findById(1L)).thenReturn(Optional.of(compania));

        var rawData = ExcelCsvParserService.RawSheetData.builder()
            .headerRowIndex(0)
            .headers(List.of("POLIZA", "TOMADOR"))
            .dataRows(Collections.emptyList())
            .totalRawRows(0)
            .build();

        when(excelCsvParserService.parseFileToRawData(any(), any())).thenReturn(rawData);

        PlantillaImportacion plantilla = new PlantillaImportacion();
        plantilla.setConfiguracionJson("{\"headerRowIndex\":0}");
        when(plantillaImportacionRepository.findByCompaniaIdAndFirmaColumnas(eq(1L), anyString()))
            .thenReturn(Optional.of(plantilla));

        ImportMappingConfigDTO config = ImportMappingConfigDTO.builder()
            .headerRowIndex(0)
            .columnMapping(Collections.emptyMap())
            .ramoMappings(Collections.emptyMap())
            .build();
        when(objectMapper.readValue(eq("{\"headerRowIndex\":0}"), eq(ImportMappingConfigDTO.class))).thenReturn(config);
        when(excelCsvParserService.collapseRowsByPoliza(anyList())).thenReturn(Collections.emptyList());

        var res = service.analizarArchivo(file, 1L);

        assertNotNull(res);
        assertTrue(res.isPlantillaEncontradaEnCache());
        assertEquals("Sancor Seguros", res.getCompaniaNombre());
        verify(aiMappingService, never()).buildMappingConfig(any(), anyInt(), any(), any(), any(), any());
    }

    @Test
    void shouldEjecutarImportacionExitosamente() throws IOException {
        Authentication auth = mock(Authentication.class);
        SecurityContext secContext = mock(SecurityContext.class);
        when(secContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("productor@mail.com");
        SecurityContextHolder.setContext(secContext);

        when(usuarioRepository.findByEmail("productor@mail.com")).thenReturn(Optional.of(productor));
        when(companiaRepository.findById(1L)).thenReturn(Optional.of(compania));

        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "dummy".getBytes());

        var rawData = ExcelCsvParserService.RawSheetData.builder()
            .headerRowIndex(0)
            .headers(List.of("POLIZA", "TOMADOR"))
            .dataRows(List.of(Map.of("POLIZA", "POL-1", "TOMADOR", "Juan Perez")))
            .totalRawRows(1)
            .build();
        when(excelCsvParserService.parseFileToRawData(any(), any())).thenReturn(rawData);

        ImportMappingConfigDTO config = ImportMappingConfigDTO.builder()
            .headerRowIndex(0)
            .columnMapping(Map.of("nroPoliza", "POLIZA", "tomadorNombre", "TOMADOR"))
            .ramoMappings(Collections.emptyMap())
            .build();

        when(clienteRepository.findAllByProductorEmail("productor@mail.com")).thenReturn(Collections.emptyList());
        when(polizaRepository.findAllByProductorEmail("productor@mail.com")).thenReturn(Collections.emptyList());
        when(ramoRepository.findAll()).thenReturn(Collections.emptyList());
        when(localidadRepository.findAllBy()).thenReturn(Collections.emptyList());
        when(provinciaRepository.findAll()).thenReturn(Collections.emptyList());

        var parsedItem = ExcelCsvParserService.ParsedPolizaItem.builder()
            .nroPza("POL-1")
            .nroEndoso(0)
            .tomadorNombre("Juan Perez")
            .premio(1000.0)
            .activo(true)
            .build();

        when(excelCsvParserService.mapRowToPolizaItem(any(), eq(config))).thenReturn(parsedItem);
        when(excelCsvParserService.collapseRowsByPoliza(anyList())).thenReturn(List.of(parsedItem));
        when(excelCsvParserService.splitNombreApellido("Juan Perez", false))
            .thenReturn(new ExcelCsvParserService.NombreApellido("Juan", "Perez"));

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        when(polizaRepository.save(any(Poliza.class))).thenAnswer(inv -> {
            Poliza p = inv.getArgument(0);
            p.setId(200L);
            return p;
        });

        var result = service.ejecutarImportacion(file, 1L, config);

        assertNotNull(result);
        assertEquals(1, result.getFilasLeidas());
        assertEquals(1, result.getPolizasUnicasProcesadas());
        assertEquals(1, result.getClientesNuevos());
        assertEquals(1, result.getPolizasCreadas());
    }

    @Test
    void shouldUpdateExistingPolizaDatesAndPremio() throws IOException {
        Authentication auth = mock(Authentication.class);
        SecurityContext secContext = mock(SecurityContext.class);
        when(secContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("productor@mail.com");
        SecurityContextHolder.setContext(secContext);

        when(usuarioRepository.findByEmail("productor@mail.com")).thenReturn(Optional.of(productor));
        when(companiaRepository.findById(1L)).thenReturn(Optional.of(compania));

        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "dummy".getBytes());

        var rawData = ExcelCsvParserService.RawSheetData.builder()
            .headerRowIndex(0)
            .headers(List.of("POLIZA", "TOMADOR"))
            .dataRows(List.of(Map.of("POLIZA", "POL-EXISTENTE", "TOMADOR", "Juan Perez")))
            .totalRawRows(1)
            .build();
        when(excelCsvParserService.parseFileToRawData(any(), any())).thenReturn(rawData);

        ImportMappingConfigDTO config = ImportMappingConfigDTO.builder()
            .headerRowIndex(0)
            .columnMapping(Map.of("nroPoliza", "POLIZA", "tomadorNombre", "TOMADOR"))
            .ramoMappings(Collections.emptyMap())
            .build();

        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(100L);
        clienteExistente.setDni("12345678");
        clienteExistente.setNombre("Juan");
        clienteExistente.setApellido("Perez");

        Poliza polizaExistente = new Poliza();
        polizaExistente.setId(200L);
        polizaExistente.setNroPza("POL-EXISTENTE");
        polizaExistente.setCompania(compania);
        polizaExistente.setInicioVigencia(LocalDate.of(2020, 1, 1));
        polizaExistente.setFinVigencia(LocalDate.of(2021, 1, 1));
        polizaExistente.setPremio(0.0);

        when(clienteRepository.findAllByProductorEmail("productor@mail.com")).thenReturn(List.of(clienteExistente));
        when(polizaRepository.findAllByProductorEmail("productor@mail.com")).thenReturn(List.of(polizaExistente));
        when(ramoRepository.findAll()).thenReturn(Collections.emptyList());
        when(localidadRepository.findAllBy()).thenReturn(Collections.emptyList());
        when(provinciaRepository.findAll()).thenReturn(Collections.emptyList());

        var parsedItem = ExcelCsvParserService.ParsedPolizaItem.builder()
            .nroPza("POL-EXISTENTE")
            .nroEndoso(0)
            .tomadorNombre("Juan Perez")
            .rawDocumento("12345678")
            .fechaEmision(LocalDate.of(2010, 5, 5))
            .inicioVigencia(LocalDate.of(2026, 1, 1))
            .finVigencia(LocalDate.of(2027, 1, 1))
            .premio(85000.0)
            .activo(true)
            .build();

        when(excelCsvParserService.mapRowToPolizaItem(any(), eq(config))).thenReturn(parsedItem);
        when(excelCsvParserService.collapseRowsByPoliza(anyList())).thenReturn(List.of(parsedItem));

        when(polizaRepository.save(any(Poliza.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.ejecutarImportacion(file, 1L, config);

        assertNotNull(result);
        assertEquals(1, result.getPolizasActualizadas());
        assertEquals(0, result.getPolizasCreadas());
        assertEquals(LocalDate.of(2010, 5, 5), polizaExistente.getFechaEmision());
        assertEquals(LocalDate.of(2026, 1, 1), polizaExistente.getInicioVigencia());
        assertEquals(LocalDate.of(2027, 1, 1), polizaExistente.getFinVigencia());
        assertEquals(85000.0, polizaExistente.getPremio());
    }
}
