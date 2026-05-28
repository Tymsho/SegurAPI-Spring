package com.first.api.first_api.dtoresponse;

import lombok.Data;
import java.util.List;

@Data
public class DashboardStatsResponse {
    private Long cantClientesActivos;
    private Long cantPolizasActivas;
    private Double totalPremios;
    private Double totalPrimas;
    private List<MesStat> polizasPorMes;
    private List<CompaniaStat> polizasPorCompania;
    private List<RamoStat> polizasPorRamo;
    private List<PagoStat> polizasPorTipoPago;
}