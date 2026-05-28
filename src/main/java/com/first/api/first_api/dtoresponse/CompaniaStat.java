package com.first.api.first_api.dtoresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompaniaStat {
    private String nombre;
    private Long cantidad;
}