package com.first.api.first_api.dto;

import com.first.api.first_api.models.TipoPago;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PolizaDTO {
    private Long id;

    @NotBlank(message = "El número de póliza es obligatorio")
    private String nroPza;

    @NotNull(message = "El cliente (tomador) es obligatorio")
    private Long clienteId;
    private String nombreCliente; // Nombre del cliente asociado

    @NotNull(message = "El tipo de pago es obligatorio")
    private TipoPago tipoPago;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate inicioVigencia;

    @NotNull(message = "La fecha de fin de vigencia es obligatoria")
    private LocalDate finVigencia;

    @NotNull(message = "El ramo es obligatorio")
    private Long ramoId;
    private String nombreRamo;

    @NotNull(message = "La compañía aseguradora es obligatoria")
    private Long companiaId;
    private String nombreCompania;

    private String tipoFacturacion;

    @Min(value = 0, message = "La prima no puede ser negativa")
    private Double prima;

    @DecimalMin(value = "0.01", message = "El premio debe ser mayor a cero")
    private Double premio;
    
    // El productorId tampoco se pide acá, se asigna en el servicio mediante el token
}