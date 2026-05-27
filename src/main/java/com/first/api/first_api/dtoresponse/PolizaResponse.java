package com.first.api.first_api.dtoresponse;

import com.first.api.first_api.models.TipoPago;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PolizaResponse {
    private Long id;
    private String nroPza;
    private Long clienteId;
    private String nombreCliente;
    private TipoPago tipoPago;
    private LocalDate inicioVigencia;
    private LocalDate finVigencia;
    private Long ramoId;
    private String nombreRamo;
    private Long companiaId;
    private String nombreCompania;
    private String tipoFacturacion;
    private Double prima;
    private Double premio;
}
