package com.first.api.first_api.dtorequest;

import com.first.api.first_api.models.TipoPago;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PolizaRequest {
    @NotBlank(message = "El número de póliza es obligatorio")
    private String nroPza;

    @NotNull(message = "El cliente (tomador) es obligatorio")
    private Long clienteId;

    @NotNull(message = "El tipo de pago es obligatorio")
    private TipoPago tipoPago;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate inicioVigencia;

    @NotNull(message = "La fecha de fin de vigencia es obligatoria")
    private LocalDate finVigencia;

    private LocalDate fechaEmision;

    @NotNull(message = "El ramo es obligatorio")
    private Long ramoId;

    @NotNull(message = "La compañía aseguradora es obligatoria")
    private Long companiaId;

    private String tipoFacturacion;

    private String bienAsegurado;

    private Integer nroEndoso = 0;

    @Min(value = 0, message = "El premio no puede ser negativo")
    private Double premio;

    public PolizaRequest() {
    }
}
