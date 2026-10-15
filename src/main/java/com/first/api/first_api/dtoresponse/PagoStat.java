package com.first.api.first_api.dtoresponse;

import com.first.api.first_api.models.TipoPago;
import lombok.Data;

@Data
public class PagoStat {
    private TipoPago tipoPago;
    private Long cantidad;

    public PagoStat() {
    }

    public PagoStat(TipoPago tipoPago, Long cantidad) {
        this.tipoPago = tipoPago;
        this.cantidad = cantidad;
    }
}
