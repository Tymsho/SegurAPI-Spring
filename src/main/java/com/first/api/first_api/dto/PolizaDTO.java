package com.first.api.first_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PolizaDTO {
    private Long id;

    @NotBlank(message = "El número de póliza es requerido")
    private String numeroPoliza;

    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es requerida")
    private LocalDate fechaFin;
    
    // --- Campos para LECTURA (Dashboard) ---
    private String nombreCliente;
    private String nombreCompania;
    private String nombreRamo;

    // --- Campos para ESCRITURA (Crear/Actualizar) ---
    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El ID de la compañía es obligatorio")
    private Long companiaId;

    @NotNull(message = "El ID del ramo es obligatorio")
    private Long ramoId;
}