package com.first.api.first_api.importacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.first.api.first_api.models.Localidad;
import com.first.api.first_api.models.Provincia;
import com.first.api.first_api.repositories.LocalidadRepository;
import com.first.api.first_api.repositories.ProvinciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Servicio para sincronizar el catálogo de provincias y localidades argentinas
 * desde Georef-AR con respaldo offline resiliente, y resolución dinámica de localidades
 * durante importaciones de cartera.
 */
@Service
public class GeografiaSyncService {

    private static final Logger log = LoggerFactory.getLogger(GeografiaSyncService.class);

    private static final String GEOREF_PROVINCIAS_URL =
        "https://infra.datos.gob.ar/catalog/modernizacion/dataset/7/distribution/7.2/download/provincias.json";

    private static final List<String> FALLBACK_PROVINCIAS = List.of(
        "Buenos Aires",
        "Ciudad Autónoma de Buenos Aires",
        "Catamarca",
        "Chaco",
        "Chubut",
        "Córdoba",
        "Corrientes",
        "Entre Ríos",
        "Formosa",
        "Jujuy",
        "La Pampa",
        "La Rioja",
        "Mendoza",
        "Misiones",
        "Neuquén",
        "Río Negro",
        "Salta",
        "San Juan",
        "San Luis",
        "Santa Cruz",
        "Santa Fe",
        "Santiago del Estero",
        "Tierra del Fuego, Antártida e Islas del Atlántico Sur",
        "Tucumán"
    );

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        syncProvinciasSiFaltan();
    }

    @Transactional
    public synchronized void syncProvinciasSiFaltan() {
        if (provinciaRepository.count() >= 20) {
            return;
        }

        log.info("Catálogo de provincias incompleto (< 20). Sincronizando con Georef-AR...");
        boolean syncOk = false;

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(5000);

            RestClient restClient = RestClient.builder()
                .requestFactory(factory)
                .build();

            String response = restClient.get()
                .uri(GEOREF_PROVINCIAS_URL)
                .retrieve()
                .body(String.class);

            if (response != null && !response.isBlank()) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode provinciasNode = root.path("provincias");
                if (provinciasNode.isArray() && !provinciasNode.isEmpty()) {
                    for (JsonNode pNode : provinciasNode) {
                        String nombre = pNode.path("nombre").asText();
                        if (nombre != null && !nombre.isBlank()) {
                            guardarProvinciaSiNoExiste(nombre);
                        }
                    }
                    syncOk = true;
                    log.info("Sincronización de provincias con Georef-AR exitosa.");
                }
            }
        } catch (Exception e) {
            log.warn("No fue posible descargar provincias desde Georef-AR ({}: {}). Aplicando catálogo offline de respaldo.",
                e.getClass().getSimpleName(), e.getMessage());
        }

        if (!syncOk) {
            for (String nombre : FALLBACK_PROVINCIAS) {
                guardarProvinciaSiNoExiste(nombre);
            }
            log.info("Catálogo offline de 24 provincias cargado exitosamente.");
        }
    }

    private void guardarProvinciaSiNoExiste(String nombre) {
        String cleanName = ArgentineDataNormalizer.toTitleCase(nombre.trim());
        String key = ArgentineDataNormalizer.stripAccentsAndClean(cleanName);
        boolean exists = provinciaRepository.findAll().stream()
            .anyMatch(p -> ArgentineDataNormalizer.stripAccentsAndClean(p.getNombre()).equalsIgnoreCase(key));
        if (!exists) {
            Provincia p = new Provincia();
            p.setNombre(cleanName);
            provinciaRepository.save(p);
        }
    }

    @Transactional
    public Localidad resolveOrCreateLocalidad(String rawLocalidad, String rawProvincia,
                                             Map<String, Localidad> cacheLocalidades,
                                             Map<String, Provincia> cacheProvincias) {
        String provNorm = ArgentineDataNormalizer.normalizeProvinciaName(rawProvincia, rawLocalidad);
        String locNorm = ArgentineDataNormalizer.normalizeLocalidadName(rawLocalidad);
        if (locNorm.isBlank()) {
            locNorm = "CENTRO";
        }

        String provKey = ArgentineDataNormalizer.stripAccentsAndClean(provNorm);
        String locKey = ArgentineDataNormalizer.stripAccentsAndClean(locNorm);
        String cacheKey = locKey + "|" + provKey;

        if (cacheLocalidades != null && cacheLocalidades.containsKey(cacheKey)) {
            return cacheLocalidades.get(cacheKey);
        }

        // Resolver Provincia
        Provincia provincia = null;
        if (cacheProvincias != null && cacheProvincias.containsKey(provKey)) {
            provincia = cacheProvincias.get(provKey);
        }

        if (provincia == null) {
            provincia = provinciaRepository.findByNombreIgnoreCase(provNorm)
                .orElseGet(() -> {
                    for (Provincia p : provinciaRepository.findAll()) {
                        if (ArgentineDataNormalizer.stripAccentsAndClean(p.getNombre()).equalsIgnoreCase(provKey)) {
                            return p;
                        }
                    }
                    Provincia nueva = new Provincia();
                    nueva.setNombre(ArgentineDataNormalizer.toTitleCase(provNorm));
                    return provinciaRepository.save(nueva);
                });

            if (cacheProvincias != null) {
                cacheProvincias.put(provKey, provincia);
            }
        }

        final Provincia provFinal = provincia;

        // Resolver Localidad
        String locTitle = ArgentineDataNormalizer.toTitleCase(locNorm);
        Localidad localidad = localidadRepository.findByNombreIgnoreCaseAndProvinciaId(locTitle, provFinal.getId())
            .orElseGet(() -> {
                for (Localidad l : localidadRepository.findAll()) {
                    if (l.getProvincia() != null && l.getProvincia().getId().equals(provFinal.getId())) {
                        if (ArgentineDataNormalizer.stripAccentsAndClean(l.getNombre()).equalsIgnoreCase(locKey)) {
                            return l;
                        }
                    }
                }
                Localidad nueva = new Localidad();
                nueva.setNombre(locTitle);
                nueva.setProvincia(provFinal);
                return localidadRepository.save(nueva);
            });

        if (cacheLocalidades != null) {
            cacheLocalidades.put(cacheKey, localidad);
        }

        return localidad;
    }
}
