package com.first.api.first_api.importacion;

import lombok.Data;

import java.util.Map;

/**
 * Configuración de mapeo de columnas y equivalencias de ramos para la importación
 * de carteras de aseguradoras.
 */
@Data
public class ImportMappingConfigDTO {

    private Integer headerRowIndex;
    private boolean generadoPorIa;
    private Map<String, String> columnMapping;
    private Map<String, String> ramoMappings;

    public ImportMappingConfigDTO() {
    }

    public ImportMappingConfigDTO(Integer headerRowIndex, boolean generadoPorIa,
                                 Map<String, String> columnMapping, Map<String, String> ramoMappings) {
        this.headerRowIndex = headerRowIndex;
        this.generadoPorIa = generadoPorIa;
        this.columnMapping = columnMapping;
        this.ramoMappings = ramoMappings;
    }

    public static ImportMappingConfigDTOBuilder builder() {
        return new ImportMappingConfigDTOBuilder();
    }

    public static class ImportMappingConfigDTOBuilder {
        private Integer headerRowIndex;
        private boolean generadoPorIa;
        private Map<String, String> columnMapping;
        private Map<String, String> ramoMappings;

        public ImportMappingConfigDTOBuilder headerRowIndex(Integer headerRowIndex) {
            this.headerRowIndex = headerRowIndex;
            return this;
        }

        public ImportMappingConfigDTOBuilder generadoPorIa(boolean generadoPorIa) {
            this.generadoPorIa = generadoPorIa;
            return this;
        }

        public ImportMappingConfigDTOBuilder columnMapping(Map<String, String> columnMapping) {
            this.columnMapping = columnMapping;
            return this;
        }

        public ImportMappingConfigDTOBuilder ramoMappings(Map<String, String> ramoMappings) {
            this.ramoMappings = ramoMappings;
            return this;
        }

        public ImportMappingConfigDTO build() {
            return new ImportMappingConfigDTO(headerRowIndex, generadoPorIa, columnMapping, ramoMappings);
        }
    }
}
