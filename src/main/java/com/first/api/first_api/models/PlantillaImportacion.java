package com.first.api.first_api.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "plantillas_importacion", uniqueConstraints = @UniqueConstraint(columnNames = {"compania_id", "firma_columnas"}))
@Data
@NoArgsConstructor
public class PlantillaImportacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compania_id", nullable = false)
    private Compania compania;

    @Column(name = "firma_columnas", nullable = false, length = 500)
    private String firmaColumnas;

    @Column(name = "fila_encabezado", nullable = false)
    private Integer filaEncabezado;

    @Column(name = "configuracion_json", columnDefinition = "TEXT", nullable = false)
    private String configuracionJson;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
}
