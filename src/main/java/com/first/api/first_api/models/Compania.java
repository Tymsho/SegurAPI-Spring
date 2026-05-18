package com.first.api.first_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

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
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "companias_ramos", // Nombre de la tabla intermedia en la BD
        joinColumns = @JoinColumn(name = "compania_id"),
        inverseJoinColumns = @JoinColumn(name = "ramo_id")
    )
    private Set<Ramo> ramos;
}