package com.first.api.first_api.importacion;

import lombok.Data;

import java.util.List;

/**
 * Resumen del resultado de ejecutar la importación final (/api/importacion/ejecutar).
 */
@Data
public class ImportExecutionResultDTO {

    private int filasLeidas;
    private int polizasUnicasProcesadas;
    private int clientesNuevos;
    private int clientesActualizados;
    private int polizasCreadas;
    private int polizasActualizadas;
    private int polizasMarcadasBaja;
    private List<String> advertencias;

    public ImportExecutionResultDTO() {}

    public ImportExecutionResultDTO(int filasLeidas, int polizasUnicasProcesadas, int clientesNuevos,
                                    int clientesActualizados, int polizasCreadas, int polizasActualizadas,
                                    int polizasMarcadasBaja, List<String> advertencias) {
        this.filasLeidas = filasLeidas;
        this.polizasUnicasProcesadas = polizasUnicasProcesadas;
        this.clientesNuevos = clientesNuevos;
        this.clientesActualizados = clientesActualizados;
        this.polizasCreadas = polizasCreadas;
        this.polizasActualizadas = polizasActualizadas;
        this.polizasMarcadasBaja = polizasMarcadasBaja;
        this.advertencias = advertencias;
    }

    public static ImportExecutionResultDTOBuilder builder() {
        return new ImportExecutionResultDTOBuilder();
    }

    public static class ImportExecutionResultDTOBuilder {
        private int filasLeidas;
        private int polizasUnicasProcesadas;
        private int clientesNuevos;
        private int clientesActualizados;
        private int polizasCreadas;
        private int polizasActualizadas;
        private int polizasMarcadasBaja;
        private List<String> advertencias;

        ImportExecutionResultDTOBuilder() {}

        public ImportExecutionResultDTOBuilder filasLeidas(int filasLeidas) {
            this.filasLeidas = filasLeidas;
            return this;
        }

        public ImportExecutionResultDTOBuilder polizasUnicasProcesadas(int polizasUnicasProcesadas) {
            this.polizasUnicasProcesadas = polizasUnicasProcesadas;
            return this;
        }

        public ImportExecutionResultDTOBuilder clientesNuevos(int clientesNuevos) {
            this.clientesNuevos = clientesNuevos;
            return this;
        }

        public ImportExecutionResultDTOBuilder clientesActualizados(int clientesActualizados) {
            this.clientesActualizados = clientesActualizados;
            return this;
        }

        public ImportExecutionResultDTOBuilder polizasCreadas(int polizasCreadas) {
            this.polizasCreadas = polizasCreadas;
            return this;
        }

        public ImportExecutionResultDTOBuilder polizasActualizadas(int polizasActualizadas) {
            this.polizasActualizadas = polizasActualizadas;
            return this;
        }

        public ImportExecutionResultDTOBuilder polizasMarcadasBaja(int polizasMarcadasBaja) {
            this.polizasMarcadasBaja = polizasMarcadasBaja;
            return this;
        }

        public ImportExecutionResultDTOBuilder advertencias(List<String> advertencias) {
            this.advertencias = advertencias;
            return this;
        }

        public ImportExecutionResultDTO build() {
            return new ImportExecutionResultDTO(filasLeidas, polizasUnicasProcesadas, clientesNuevos,
                    clientesActualizados, polizasCreadas, polizasActualizadas, polizasMarcadasBaja, advertencias);
        }
    }
}
