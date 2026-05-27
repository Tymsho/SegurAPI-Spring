package com.first.api.first_api.dtoresponse;

import com.first.api.first_api.models.Rol;
import lombok.Data;

@Data
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private boolean activo;
}
