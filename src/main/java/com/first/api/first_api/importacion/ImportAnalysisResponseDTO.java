package com.first.api.first_api.importacion;

import lombok.Data;

import java.util.List;

/**
 * Respuesta devuelta por el endpoint de análisis (/api/importacion/analizar).
 * Permite al usuario revisar la configuración sugerida y previsualizar los primeros registros.
 */
@Data
public class ImportAnalysisResponseDTO {

    private Long companiaId;
    private String companiaNombre;
    private boolean plantillaEncontradaEnCache;
    private boolean mapeadoConIa;
    private String firmaColumnas;
    private List<String> excelColumns;
    private List<String> ramosOficiales;
    private ImportMappingConfigDTO suggestedConfig;
    private List<ImportPreviewRowDTO> previewRows;
    private int totalRawRows;
    private int totalUniquePolizas;

    public ImportAnalysisResponseDTO() {
    }

    public ImportAnalysisResponseDTO(Long companiaId, String companiaNombre, boolean plantillaEncontradaEnCache,
                                    boolean mapeadoConIa, String firmaColumnas, List<String> excelColumns,
                                    List<String> ramosOficiales, ImportMappingConfigDTO suggestedConfig,
                                    List<ImportPreviewRowDTO> previewRows, int totalRawRows, int totalUniquePolizas) {
        this.companiaId = companiaId;
        this.companiaNombre = companiaNombre;
        this.plantillaEncontradaEnCache = plantillaEncontradaEnCache;
        this.mapeadoConIa = mapeadoConIa;
        this.firmaColumnas = firmaColumnas;
        this.excelColumns = excelColumns;
        this.ramosOficiales = ramosOficiales;
        this.suggestedConfig = suggestedConfig;
        this.previewRows = previewRows;
        this.totalRawRows = totalRawRows;
        this.totalUniquePolizas = totalUniquePolizas;
    }

    public static ImportAnalysisResponseDTOBuilder builder() {
        return new ImportAnalysisResponseDTOBuilder();
    }

    public static class ImportAnalysisResponseDTOBuilder {
        private Long companiaId;
        private String companiaNombre;
        private boolean plantillaEncontradaEnCache;
        private boolean mapeadoConIa;
        private String firmaColumnas;
        private List<String> excelColumns;
        private List<String> ramosOficiales;
        private ImportMappingConfigDTO suggestedConfig;
        private List<ImportPreviewRowDTO> previewRows;
        private int totalRawRows;
        private int totalUniquePolizas;

        public ImportAnalysisResponseDTOBuilder companiaId(Long companiaId) {
            this.companiaId = companiaId;
            return this;
        }

        public ImportAnalysisResponseDTOBuilder companiaNombre(String companiaNombre) {
            this.companiaNombre = companiaNombre;
            return this;
        }

        public ImportAnalysisResponseDTOBuilder plantillaEncontradaEnCache(boolean plantillaEncontradaEnCache) {
            this.plantillaEncontradaEnCache = plantillaEncontradaEnCache;
            return this;
        }

        public ImportAnalysisResponseDTOBuilder mapeadoConIa(boolean mapeadoConIa) {
            this.mapeadoConIa = mapeadoConIa;
            return this;
        }

        public ImportAnalysisResponseDTOBuilder firmaColumnas(String firmaColumnas) {
            this.firmaColumnas = firmaColumnas;
            return this;
        }

        public ImportAnalysisResponseDTOBuilder excelColumns(List<String> excelColumns) {
            this.excelColumns = excelColumns;
            return this;
        }

        public ImportAnalysisResponseDTOBuilder ramosOficiales(List<String> ramosOficiales) {
            this.ramosOficiales = ramosOficiales;
            return this;
        }

        public ImportAnalysisResponseDTOBuilder suggestedConfig(ImportMappingConfigDTO suggestedConfig) {
            this.suggestedConfig = suggestedConfig;
            return this;
        }

        public ImportAnalysisResponseDTOBuilder previewRows(List<ImportPreviewRowDTO> previewRows) {
            this.previewRows = previewRows;
            return this;
        }

        public ImportAnalysisResponseDTOBuilder totalRawRows(int totalRawRows) {
            this.totalRawRows = totalRawRows;
            return this;
        }

        public ImportAnalysisResponseDTOBuilder totalUniquePolizas(int totalUniquePolizas) {
            this.totalUniquePolizas = totalUniquePolizas;
            return this;
        }

        public ImportAnalysisResponseDTO build() {
            return new ImportAnalysisResponseDTO(
                companiaId, companiaNombre, plantillaEncontradaEnCache, mapeadoConIa,
                firmaColumnas, excelColumns, ramosOficiales, suggestedConfig,
                previewRows, totalRawRows, totalUniquePolizas
            );
        }
    }
}
