package com.first.api.first_api.dtoresponse;

import lombok.Data;
 
@Data
public class CompaniaStat {
    private String nombre;
    private Long cantidad;

    public CompaniaStat() {
    }

    public CompaniaStat(String nombre, Long cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }
}