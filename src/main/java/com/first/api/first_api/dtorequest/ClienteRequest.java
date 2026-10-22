package com.first.api.first_api.dtorequest;

import com.first.api.first_api.models.Sexo;
import com.first.api.first_api.models.TipoIva;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    private LocalDate fechaNacimiento;

    private String direccion;

    private Long localidadId;

    private String telefono;

    @Email(message = "El formato del email es inválido")
    private String email;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo;

    @NotNull(message = "El tipo de IVA es obligatorio")
    private TipoIva tipoIva;

    public ClienteRequest() {
    }
}
