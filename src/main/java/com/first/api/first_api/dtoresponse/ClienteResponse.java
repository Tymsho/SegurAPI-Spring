package com.first.api.first_api.dtoresponse;

import com.first.api.first_api.models.Sexo;
import com.first.api.first_api.models.TipoIva;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private String direccion;
    private Long localidadId;
    private String telefono;
    private String email;
    private String dni;
    private Sexo sexo;
    private TipoIva tipoIva;
}
