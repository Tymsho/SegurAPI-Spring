package com.first.api.first_api.dtoresponse;

import lombok.Data;

@Data
public class ClienteLightResponse {
    private Long id;
    private String nombreCompleto;
    private String dni;

    public ClienteLightResponse() {
    }

    public ClienteLightResponse(Long id, String nombreCompleto, String dni) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.dni = dni;
    }
}
