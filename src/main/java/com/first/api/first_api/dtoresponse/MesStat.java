package com.first.api.first_api.dtoresponse;

import lombok.Data;

@Data
public class MesStat {
    private Integer anio;
    private Integer mes;
    private Long cantidad;

    public MesStat() {
    }

    public MesStat(Integer anio, Integer mes, Long cantidad) {
        this.anio = anio;
        this.mes = mes;
        this.cantidad = cantidad;
    }
}