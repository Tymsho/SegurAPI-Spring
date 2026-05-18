package com.first.api.first_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "polizas")
@Data
public class Poliza {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_poliza", unique = true)
    private String numeroPoliza;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private boolean activa = true;

    // Relaciones (Muchos a Uno)
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "compania_id")
    private Compania compania;

    @ManyToOne
    @JoinColumn(name = "ramo_id")
    private Ramo ramo;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // El productor que la cargó
}