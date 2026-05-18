package com.first.api.first_api.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "provincias")
@Data
public class Provincia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;
}