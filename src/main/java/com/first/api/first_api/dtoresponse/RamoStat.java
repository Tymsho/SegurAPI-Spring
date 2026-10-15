package com.first.api.first_api.dtoresponse;

import lombok.Data;

@Data
public class RamoStat {
    private String nombre;
    private Long cantidad;

    public RamoStat() {
    }

    public RamoStat(String nombre, Long cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }
}