package com.first.api.first_api.dto;

import com.first.api.first_api.models.Sexo;
import com.first.api.first_api.models.TipoIva;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    private String direccion;

    @NotNull(message = "La localidad es obligatoria")
    private Long localidadId; // Pasamos solo el ID para simplificar el JSON de entrada

    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email es inválido")
    private String email;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo; // Validación nativa del Enum

    @NotNull(message = "El tipo de IVA es obligatorio")
    private TipoIva tipoIva;
    
    // IMPORTANTE: Omitimos el productorId. No se recibe desde el front-end por seguridad.
}