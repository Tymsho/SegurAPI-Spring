package com.first.api.first_api.importacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.models.*;
import com.first.api.first_api.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio transaccional para el análisis y ejecución de importación de carteras masivas.
 * Soporta deduplicación inteligente de clientes, normalización de pólizas, endosos y anulaciones.
 */
@Service
public class ImportacionCarteraService {

    private static final Logger log = LoggerFactory.getLogger(ImportacionCarteraService.class);

    @Autowired
    private CompaniaRepository companiaRepository;

    @Autowired
    private PlantillaImportacionRepository plantillaImportacionRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PolizaRepository polizaRepository;

    @Autowired
    private RamoRepository ramoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private GeografiaSyncService geografiaSyncService;

    @Autowired
    private AiMappingService aiMappingService;

    @Autowired
    private ExcelCsvParserService excelCsvParserService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Analiza el archivo subido, detecta encabezados, busca plantilla en caché o invoca a la IA,
     * y genera una previsualización de las primeras filas.
     */
    public ImportAnalysisResponseDTO analizarArchivo(MultipartFile file, Long companiaId) {
        Compania compania = companiaRepository.findById(companiaId)
            .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada con id: " + companiaId));

        ExcelCsvParserService.RawSheetData rawData;
        try {
            rawData = excelCsvParserService.parseFileToRawData(file.getBytes(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo: " + e.getMessage(), e);
        }

        String firmaColumnas = String.join("|", rawData.getHeaders()).toUpperCase();
        if (firmaColumnas.length() > 500) {
            firmaColumnas = firmaColumnas.substring(0, 500);
        }

        List<String> ramosOficiales = ramoRepository.findAll().stream()
            .map(Ramo::getNombre)
            .sorted()
            .collect(Collectors.toList());

        Optional<PlantillaImportacion> plantillaOpt = plantillaImportacionRepository
            .findByCompaniaIdAndFirmaColumnas(companiaId, firmaColumnas);

        ImportMappingConfigDTO config;
        boolean enCache = false;
        boolean conIa = false;

        if (plantillaOpt.isPresent()) {
            try {
                config = objectMapper.readValue(plantillaOpt.get().getConfiguracionJson(), ImportMappingConfigDTO.class);
                enCache = true;
                log.info("Plantilla de importación recuperada de caché para compañía '{}'.", compania.getNombre());
            } catch (Exception e) {
                log.warn("Fallo al deserializar plantilla en caché. Generando nueva configuración.");
                config = null;
            }
        } else {
            config = null;
        }

        if (config == null) {
            Set<String> uniqueRawRamos = extractUniqueRawRamos(rawData);
            config = aiMappingService.buildMappingConfig(
                compania.getNombre(),
                rawData.getHeaderRowIndex(),
                rawData.getHeaders(),
                rawData.getDataRows().stream().limit(10).collect(Collectors.toList()),
                uniqueRawRamos,
                ramosOficiales
            );
            conIa = config.isGeneradoPorIa();
        }

        // Parsear filas con la configuración y colapsar endosos
        List<ExcelCsvParserService.ParsedPolizaItem> items = new ArrayList<>();
        for (Map<String, String> row : rawData.getDataRows()) {
            ExcelCsvParserService.ParsedPolizaItem item = excelCsvParserService.mapRowToPolizaItem(row, config);
            if (item != null) {
                items.add(item);
            }
        }

        List<ExcelCsvParserService.ParsedPolizaItem> collapsed = excelCsvParserService.collapseRowsByPoliza(items);

        // Generar previsualización (hasta 15 filas)
        List<ImportPreviewRowDTO> previewRows = new ArrayList<>();
        int previewLimit = Math.min(15, collapsed.size());
        for (int i = 0; i < previewLimit; i++) {
            ExcelCsvParserService.ParsedPolizaItem item = collapsed.get(i);
            String rawRamo = item.getRawRamoPrincipal();
            String ramoResuelto = (config.getRamoMappings() != null && rawRamo != null)
                ? config.getRamoMappings().getOrDefault(rawRamo, rawRamo)
                : rawRamo;

            ArgentineDataNormalizer.DocExtractionResult docRes =
                ArgentineDataNormalizer.extractDocumento(item.getRawDocumento(), item.getTomadorNombre());

            previewRows.add(ImportPreviewRowDTO.builder()
                .nroPza(item.getNroPza())
                .nroEndoso(item.getNroEndoso())
                .clienteNombreCompleto(item.getTomadorNombre())
                .clienteDni(docRes.dniLimpio() != null ? docRes.dniLimpio() : "SINDNI")
                .localidad(item.getRawLocalidad())
                .ramoResuelto(ramoResuelto)
                .bienAsegurado(item.getBienAsegurado())
                .fechaEmision(item.getFechaEmision() != null ? item.getFechaEmision().toString() : "")
                .inicioVigencia(item.getInicioVigencia() != null ? item.getInicioVigencia().toString() : "")
                .finVigencia(item.getFinVigencia() != null ? item.getFinVigencia().toString() : "")
                .premio(item.getPremio())
                .tipoPago(item.getTipoPago() != null ? item.getTipoPago().name() : "NO_ESPECIFICADO")
                .estadoInferido(item.isActivo() ? "ACTIVA" : "ANULADA / BAJA")
                .build());
        }

        return ImportAnalysisResponseDTO.builder()
            .companiaId(companiaId)
            .companiaNombre(compania.getNombre())
            .plantillaEncontradaEnCache(enCache)
            .mapeadoConIa(conIa)
            .firmaColumnas(firmaColumnas)
            .excelColumns(rawData.getHeaders())
            .ramosOficiales(ramosOficiales)
            .suggestedConfig(config)
            .previewRows(previewRows)
            .totalRawRows(rawData.getTotalRawRows())
            .totalUniquePolizas(collapsed.size())
            .build();
    }

    private Set<String> extractUniqueRawRamos(ExcelCsvParserService.RawSheetData rawData) {
        Set<String> uniqueRamos = new LinkedHashSet<>();
        List<String> ramoColCandidates = new ArrayList<>();
        for (String h : rawData.getHeaders()) {
            String clean = ArgentineDataNormalizer.stripAccentsAndClean(h);
            if (clean.contains("RAMO") || clean.contains("SECCION")) {
                ramoColCandidates.add(h);
            }
        }
        for (Map<String, String> row : rawData.getDataRows()) {
            for (String col : ramoColCandidates) {
                String val = row.get(col);
                if (val != null && !val.isBlank()) {
                    uniqueRamos.add(val.trim());
                }
            }
        }
        return uniqueRamos;
    }

    /**
     * Ejecuta la importación transaccional: guarda/actualiza la plantilla, upsert de clientes y pólizas.
     */
    @Transactional
    public ImportExecutionResultDTO ejecutarImportacion(MultipartFile file, Long companiaId, ImportMappingConfigDTO config) {
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario productor = usuarioRepository.findByEmail(emailLogueado)
            .orElseThrow(() -> new ResourceNotFoundException("Productor no encontrado con email: " + emailLogueado));

        Compania compania = companiaRepository.findById(companiaId)
            .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada con id: " + companiaId));

        ExcelCsvParserService.RawSheetData rawData;
        try {
            rawData = excelCsvParserService.parseFileToRawData(file.getBytes(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo para importación: " + e.getMessage(), e);
        }

        // 1. Guardar o actualizar la plantilla en BD para reutilización futura
        String firmaColumnas = String.join("|", rawData.getHeaders()).toUpperCase();
        if (firmaColumnas.length() > 500) {
            firmaColumnas = firmaColumnas.substring(0, 500);
        }

        try {
            PlantillaImportacion plantilla = plantillaImportacionRepository
                .findByCompaniaIdAndFirmaColumnas(companiaId, firmaColumnas)
                .orElseGet(PlantillaImportacion::new);
            plantilla.setCompania(compania);
            plantilla.setFirmaColumnas(firmaColumnas);
            plantilla.setFilaEncabezado(config.getHeaderRowIndex() != null ? config.getHeaderRowIndex() : rawData.getHeaderRowIndex());
            plantilla.setConfiguracionJson(objectMapper.writeValueAsString(config));
            plantilla.setFechaActualizacion(LocalDateTime.now());
            plantillaImportacionRepository.save(plantilla);
        } catch (Exception ex) {
            log.warn("No se pudo persistir la plantilla de importación: {}", ex.getMessage());
        }

        // 2. Precarga de datos en memoria para operaciones O(1) masivas
        List<Cliente> clientesExistentes = clienteRepository.findAllByProductorEmail(emailLogueado);
        Map<String, Cliente> clientesPorDni = new HashMap<>();
        Map<String, Cliente> clientesPorNombreNorm = new HashMap<>();

        for (Cliente c : clientesExistentes) {
            if (c.getDni() != null && !c.getDni().isBlank()) {
                clientesPorDni.put(c.getDni(), c);
            }
            String key = ArgentineDataNormalizer.normalizeCompanyOrPersonKey(
                (c.getNombre() != null ? c.getNombre() : "") + " " + (c.getApellido() != null ? c.getApellido() : "")
            );
            if (!key.isBlank()) {
                clientesPorNombreNorm.put(key, c);
            }
        }

        List<Poliza> polizasProductor = polizaRepository.findAllByProductorEmail(emailLogueado);
        Map<String, Poliza> polizasExistentes = new HashMap<>();
        for (Poliza p : polizasProductor) {
            if (p.getCompania() != null && p.getCompania().getId().equals(companiaId)) {
                polizasExistentes.put(p.getNroPza(), p);
            }
        }

        Map<String, Ramo> ramosPorNombre = new HashMap<>();
        for (Ramo r : ramoRepository.findAll()) {
            ramosPorNombre.put(ArgentineDataNormalizer.stripAccentsAndClean(r.getNombre()), r);
        }

        Ramo ramoDefault = ramosPorNombre.values().stream().findFirst().orElse(null);

        Map<String, Localidad> cacheLocalidades = new HashMap<>();
        for (Localidad l : localidadRepository.findAllBy()) {
            String lKey = ArgentineDataNormalizer.stripAccentsAndClean(l.getNombre());
            String pKey = (l.getProvincia() != null) ? ArgentineDataNormalizer.stripAccentsAndClean(l.getProvincia().getNombre()) : "";
            cacheLocalidades.put(lKey + "|" + pKey, l);
        }

        Map<String, Provincia> cacheProvincias = new HashMap<>();
        for (Provincia p : provinciaRepository.findAll()) {
            cacheProvincias.put(ArgentineDataNormalizer.stripAccentsAndClean(p.getNombre()), p);
        }

        // 3. Parsear filas crudas a items estructurados y colapsar endosos
        List<ExcelCsvParserService.ParsedPolizaItem> items = new ArrayList<>();
        for (Map<String, String> row : rawData.getDataRows()) {
            ExcelCsvParserService.ParsedPolizaItem item = excelCsvParserService.mapRowToPolizaItem(row, config);
            if (item != null) {
                items.add(item);
            }
        }

        List<ExcelCsvParserService.ParsedPolizaItem> collapsedPolizas = excelCsvParserService.collapseRowsByPoliza(items);

        int clientesNuevos = 0;
        int clientesActualizados = 0;
        int polizasCreadas = 0;
        int polizasActualizadas = 0;
        int polizasMarcadasBaja = 0;
        List<String> advertencias = new ArrayList<>();

        // 4. Procesamiento de pólizas colapsadas
        for (ExcelCsvParserService.ParsedPolizaItem pItem : collapsedPolizas) {
            // A. Deduplicación inteligente de Cliente
            ArgentineDataNormalizer.DocExtractionResult docRes =
                ArgentineDataNormalizer.extractDocumento(pItem.getRawDocumento(), pItem.getTomadorNombre());

            String dniLimpio = docRes.dniLimpio();
            boolean esJuridica = docRes.esPersonaJuridica();
            String nombreNormKey = ArgentineDataNormalizer.normalizeCompanyOrPersonKey(pItem.getTomadorNombre());

            Cliente cliente = null;

            // 1. Buscar por DNI
            if (dniLimpio != null) {
                cliente = clientesPorDni.get(dniLimpio);
            }

            // 2. Si no se encontró por DNI, buscar por nombre/razón social normalizada
            if (cliente == null && !nombreNormKey.isBlank()) {
                cliente = clientesPorNombreNorm.get(nombreNormKey);
            }

            if (cliente != null) {
                boolean modificado = false;

                // Si tenía DNI sintético ("SINDNI-...") y el nuevo archivo trae su CUIT/DNI real, actualizarlo
                if (cliente.getDni().startsWith("SINDNI-") && dniLimpio != null) {
                    if (!clientesPorDni.containsKey(dniLimpio)) {
                        clientesPorDni.remove(cliente.getDni());
                        cliente.setDni(dniLimpio);
                        clientesPorDni.put(dniLimpio, cliente);
                        modificado = true;
                    }
                }

                // Completar datos faltantes sin pisar con nulos
                if ((cliente.getDireccion() == null || cliente.getDireccion().isBlank())
                        && pItem.getDomicilio() != null && !pItem.getDomicilio().isBlank()) {
                    cliente.setDireccion(pItem.getDomicilio());
                    modificado = true;
                }

                if ((cliente.getTelefono() == null || cliente.getTelefono().isBlank())
                        && pItem.getTelefono() != null && !pItem.getTelefono().isBlank()) {
                    cliente.setTelefono(pItem.getTelefono());
                    modificado = true;
                }

                if (cliente.getFechaNacimiento() == null && pItem.getRawFechaNacimiento() != null) {
                    LocalDate fn = excelCsvParserService.parseFlexibleDate(pItem.getRawFechaNacimiento());
                    if (fn != null) {
                        cliente.setFechaNacimiento(fn);
                        modificado = true;
                    }
                }

                if (modificado) {
                    cliente = clienteRepository.save(cliente);
                    clientesActualizados++;
                }

            } else {
                // Crear nuevo cliente
                cliente = new Cliente();
                String docFinal = dniLimpio != null
                    ? dniLimpio
                    : ("SINDNI-" + Math.abs(nombreNormKey.hashCode()));

                if (clientesPorDni.containsKey(docFinal)) {
                    docFinal = docFinal + "-" + (System.nanoTime() % 1000);
                }

                cliente.setDni(docFinal);
                ExcelCsvParserService.NombreApellido na =
                    excelCsvParserService.splitNombreApellido(pItem.getTomadorNombre(), esJuridica);
                cliente.setNombre(na.nombre());
                cliente.setApellido(na.apellido());
                cliente.setSexo(docRes.sexoInferido());
                cliente.setTipoIva(docRes.ivaInferido());
                cliente.setDireccion(pItem.getDomicilio());
                cliente.setTelefono(pItem.getTelefono());

                if (pItem.getRawFechaNacimiento() != null) {
                    cliente.setFechaNacimiento(excelCsvParserService.parseFlexibleDate(pItem.getRawFechaNacimiento()));
                }

                Localidad loc = geografiaSyncService.resolveOrCreateLocalidad(
                    pItem.getRawLocalidad(),
                    pItem.getRawProvincia(),
                    cacheLocalidades,
                    cacheProvincias
                );
                cliente.setLocalidad(loc);
                cliente.setProductor(productor);
                cliente.setActivo(true);

                cliente = clienteRepository.save(cliente);
                clientesPorDni.put(cliente.getDni(), cliente);
                if (!nombreNormKey.isBlank()) {
                    clientesPorNombreNorm.put(nombreNormKey, cliente);
                }
                clientesNuevos++;
            }

            // B. Resolver Ramo
            String rawRamo = pItem.getRawRamoPrincipal();
            String ramoTraducido = (config.getRamoMappings() != null && rawRamo != null)
                ? config.getRamoMappings().get(rawRamo)
                : rawRamo;

            Ramo ramo = null;
            if (ramoTraducido != null) {
                ramo = ramosPorNombre.get(ArgentineDataNormalizer.stripAccentsAndClean(ramoTraducido));
            }
            if (ramo == null && rawRamo != null) {
                ramo = ramosPorNombre.get(ArgentineDataNormalizer.stripAccentsAndClean(rawRamo));
            }
            if (ramo == null) {
                ramo = ramoDefault;
            }

            // C. Upsert de Póliza
            Poliza poliza = polizasExistentes.get(pItem.getNroPza());
            if (poliza != null) {
                // Actualizar póliza existente
                poliza.setTomador(cliente);
                poliza.setCompania(compania);
                if (ramo != null) {
                    poliza.setRamo(ramo);
                }
                if (pItem.getInicioVigencia() != null) {
                    poliza.setInicioVigencia(pItem.getInicioVigencia());
                }
                if (pItem.getFinVigencia() != null) {
                    poliza.setFinVigencia(pItem.getFinVigencia());
                }
                poliza.setFechaEmision(pItem.getFechaEmision() != null ? pItem.getFechaEmision() : poliza.getInicioVigencia());
                poliza.setPremio(pItem.getPremio() != null ? pItem.getPremio() : 0.0);

                if (pItem.getBienAsegurado() != null && !pItem.getBienAsegurado().isBlank()) {
                    poliza.setBienAsegurado(pItem.getBienAsegurado());
                }
                if (pItem.getTipoPago() != null && pItem.getTipoPago() != TipoPago.NO_ESPECIFICADO) {
                    poliza.setTipoPago(pItem.getTipoPago());
                }
                if (pItem.getTipoFacturacion() != null && !pItem.getTipoFacturacion().isBlank()) {
                    poliza.setTipoFacturacion(pItem.getTipoFacturacion());
                }
                if (pItem.getNroEndoso() != null) {
                    poliza.setNroEndoso(pItem.getNroEndoso());
                }
                if (!pItem.isActivo()) {
                    poliza.setActivo(false);
                    polizasMarcadasBaja++;
                } else {
                    poliza.setActivo(true);
                }

                polizaRepository.save(poliza);
                polizasActualizadas++;

            } else {
                // Crear nueva póliza
                poliza = new Poliza();
                poliza.setNroPza(pItem.getNroPza());
                poliza.setTomador(cliente);
                poliza.setCompania(compania);
                poliza.setRamo(ramo);
                poliza.setProductor(productor);
                poliza.setInicioVigencia(pItem.getInicioVigencia() != null ? pItem.getInicioVigencia() : LocalDate.now());
                poliza.setFinVigencia(pItem.getFinVigencia() != null ? pItem.getFinVigencia() : LocalDate.now().plusYears(1));
                poliza.setFechaEmision(pItem.getFechaEmision() != null ? pItem.getFechaEmision() : poliza.getInicioVigencia());
                poliza.setBienAsegurado(pItem.getBienAsegurado());
                poliza.setPremio(pItem.getPremio() != null ? pItem.getPremio() : 0.0);
                poliza.setTipoPago(pItem.getTipoPago() != null ? pItem.getTipoPago() : TipoPago.NO_ESPECIFICADO);
                poliza.setTipoFacturacion(pItem.getTipoFacturacion() != null ? pItem.getTipoFacturacion() : "Mensual");
                poliza.setNroEndoso(pItem.getNroEndoso() != null ? pItem.getNroEndoso() : 0);
                poliza.setActivo(pItem.isActivo());

                if (!pItem.isActivo()) {
                    polizasMarcadasBaja++;
                }

                poliza = polizaRepository.save(poliza);
                polizasExistentes.put(poliza.getNroPza(), poliza);
                polizasCreadas++;
            }
        }

        return ImportExecutionResultDTO.builder()
            .filasLeidas(rawData.getTotalRawRows())
            .polizasUnicasProcesadas(collapsedPolizas.size())
            .clientesNuevos(clientesNuevos)
            .clientesActualizados(clientesActualizados)
            .polizasCreadas(polizasCreadas)
            .polizasActualizadas(polizasActualizadas)
            .polizasMarcadasBaja(polizasMarcadasBaja)
            .advertencias(advertencias)
            .build();
    }
}
