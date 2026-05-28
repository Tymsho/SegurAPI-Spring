package com.first.api.first_api.dtoresponse;

import com.first.api.first_api.models.TipoPago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoStat {
    private TipoPago tipoPago;
    private Long cantidad;
}
