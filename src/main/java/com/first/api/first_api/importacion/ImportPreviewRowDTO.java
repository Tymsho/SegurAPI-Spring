package com.first.api.first_api.importacion;

import lombok.Data;

/**
 * Fila procesada de vista previa para validar el mapeo antes de la importación final.
 */
@Data
public class ImportPreviewRowDTO {

    private String nroPza;
    private Integer nroEndoso;
    private String clienteNombreCompleto;
    private String clienteDni;
    private String localidad;
    private String ramoResuelto;
    private String bienAsegurado;
    private String fechaEmision;
    private String inicioVigencia;
    private String finVigencia;
    private Double premio;
    private String tipoPago;
    private String estadoInferido; // "ACTIVA" o "ANULADA / BAJA"

    public ImportPreviewRowDTO() {
    }

    public ImportPreviewRowDTO(String nroPza, Integer nroEndoso, String clienteNombreCompleto,
                              String clienteDni, String localidad, String ramoResuelto,
                              String bienAsegurado, String fechaEmision, String inicioVigencia, String finVigencia,
                              Double premio, String tipoPago, String estadoInferido) {
        this.nroPza = nroPza;
        this.nroEndoso = nroEndoso;
        this.clienteNombreCompleto = clienteNombreCompleto;
        this.clienteDni = clienteDni;
        this.localidad = localidad;
        this.ramoResuelto = ramoResuelto;
        this.bienAsegurado = bienAsegurado;
        this.fechaEmision = fechaEmision;
        this.inicioVigencia = inicioVigencia;
        this.finVigencia = finVigencia;
        this.premio = premio;
        this.tipoPago = tipoPago;
        this.estadoInferido = estadoInferido;
    }

    public static ImportPreviewRowDTOBuilder builder() {
        return new ImportPreviewRowDTOBuilder();
    }

    public static class ImportPreviewRowDTOBuilder {
        private String nroPza;
        private Integer nroEndoso;
        private String clienteNombreCompleto;
        private String clienteDni;
        private String localidad;
        private String ramoResuelto;
        private String bienAsegurado;
        private String fechaEmision;
        private String inicioVigencia;
        private String finVigencia;
        private Double premio;
        private String tipoPago;
        private String estadoInferido;

        public ImportPreviewRowDTOBuilder nroPza(String nroPza) {
            this.nroPza = nroPza;
            return this;
        }

        public ImportPreviewRowDTOBuilder nroEndoso(Integer nroEndoso) {
            this.nroEndoso = nroEndoso;
            return this;
        }

        public ImportPreviewRowDTOBuilder clienteNombreCompleto(String clienteNombreCompleto) {
            this.clienteNombreCompleto = clienteNombreCompleto;
            return this;
        }

        public ImportPreviewRowDTOBuilder clienteDni(String clienteDni) {
            this.clienteDni = clienteDni;
            return this;
        }

        public ImportPreviewRowDTOBuilder localidad(String localidad) {
            this.localidad = localidad;
            return this;
        }

        public ImportPreviewRowDTOBuilder ramoResuelto(String ramoResuelto) {
            this.ramoResuelto = ramoResuelto;
            return this;
        }

        public ImportPreviewRowDTOBuilder bienAsegurado(String bienAsegurado) {
            this.bienAsegurado = bienAsegurado;
            return this;
        }

        public ImportPreviewRowDTOBuilder fechaEmision(String fechaEmision) {
            this.fechaEmision = fechaEmision;
            return this;
        }

        public ImportPreviewRowDTOBuilder inicioVigencia(String inicioVigencia) {
            this.inicioVigencia = inicioVigencia;
            return this;
        }

        public ImportPreviewRowDTOBuilder finVigencia(String finVigencia) {
            this.finVigencia = finVigencia;
            return this;
        }

        public ImportPreviewRowDTOBuilder premio(Double premio) {
            this.premio = premio;
            return this;
        }

        public ImportPreviewRowDTOBuilder tipoPago(String tipoPago) {
            this.tipoPago = tipoPago;
            return this;
        }

        public ImportPreviewRowDTOBuilder estadoInferido(String estadoInferido) {
            this.estadoInferido = estadoInferido;
            return this;
        }

        public ImportPreviewRowDTO build() {
            return new ImportPreviewRowDTO(
                nroPza, nroEndoso, clienteNombreCompleto, clienteDni, localidad,
                ramoResuelto, bienAsegurado, fechaEmision, inicioVigencia, finVigencia, premio,
                tipoPago, estadoInferido
            );
        }
    }
}
