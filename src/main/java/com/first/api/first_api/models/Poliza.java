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

    @Column(name = "nro_pza", unique = true, nullable = false)
    private String nroPza;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente tomador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false)
    private TipoPago tipoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productor_id", nullable = false)
    private Usuario productor; // Mantenido para indexación de seguridad y estadísticas rápidas

    @Column(name = "inicio_vigencia", nullable = false)
    private LocalDate inicioVigencia;

    @Column(name = "fin_vigencia", nullable = false)
    private LocalDate finVigencia;

    @ManyToOne
    @JoinColumn(name = "ramo_id", nullable = false)
    private Ramo ramo;

    @ManyToOne
    @JoinColumn(name = "compania_id", nullable = false)
    private Compania compania;

    @Column(name = "tipo_facturacion")
    private String tipoFacturacion;

    private Double prima;
    private Double premio;

    private boolean activo = true;

    @Column(name = "documento_url")
    private String documentoUrl;
}