package com.first.api.first_api.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String apellido;

    @Column(unique = true, name = "dni_cuit")
    private String dniCuit; 

    private String telefono;

    private String email;

    private boolean activo = true; 
}