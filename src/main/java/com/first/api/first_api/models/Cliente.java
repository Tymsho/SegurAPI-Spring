package com.first.api.first_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String direccion;

    @ManyToOne
    @JoinColumn(name = "localidadd_id")
    private Localidad localidad;

    private String telefono;

    @Column(nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String dni;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_iva")
    private TipoIva tipoIva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productor_id", nullable = false)
    private Usuario productor; // Clave para el aislamiento de cartera (Multi-tenant)

    private boolean activo = true;
}