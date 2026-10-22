package com.first.api.first_api.dtoresponse;

import lombok.Data;

@Data
public class MesStat {
    private Integer anio;
    private Integer mes;
    private Long cantidad;
    private Double totalPremio;

    public MesStat() {
    }

    public MesStat(Integer anio, Integer mes, Long cantidad) {
        this.anio = anio;
        this.mes = mes;
        this.cantidad = cantidad;
        this.totalPremio = 0.0;
    }

    public MesStat(Integer anio, Integer mes, Long cantidad, Double totalPremio) {
        this.anio = anio;
        this.mes = mes;
        this.cantidad = cantidad;
        this.totalPremio = totalPremio != null ? Math.round(totalPremio * 100.0) / 100.0 : 0.0;
    }
}