package com.first.api.first_api.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "localidades")
@Data
public class Localidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;
}