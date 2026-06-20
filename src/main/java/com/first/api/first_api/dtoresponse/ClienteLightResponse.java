package com.first.api.first_api.dtoresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteLightResponse {
    private Long id;
    private String nombreCompleto;
    private String dni;
}
