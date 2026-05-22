package com.first.api.first_api.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "companias")
@Data
public class Compania {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombre;

    private boolean activo = true;
}