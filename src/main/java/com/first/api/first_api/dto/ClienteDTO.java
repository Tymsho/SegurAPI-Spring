package com.first.api.first_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClienteDTO {
    private Long id;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El apellido es requerido")
    private String apellido;

    @NotBlank(message = "El DNI/CUIT es obligatorio")
    private String dniCuit;

    private String telefono;

    @Email(message = "Formato de email inválido")
    private String email;
}