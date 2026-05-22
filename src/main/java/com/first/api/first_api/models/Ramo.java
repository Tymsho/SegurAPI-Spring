package com.first.api.first_api.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ramos")
@Data
public class Ramo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombre; // Ej: Automotor, Vida, Hogar

    private boolean activo = true;
}