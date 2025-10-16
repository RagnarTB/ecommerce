package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD: MARCA
 * 
 * Representa las marcas de productos.
 * Ejemplo: Samsung, Nike, Sony, etc.
 */

@Entity
@Table(name = "marcas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // INFORMACIÓN DE LA MARCA
    // ========================================

    /**
     * Nombre de la marca
     * Ejemplo: "Samsung", "Nike", "Sony"
     */
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    /**
     * Descripción de la marca
     */
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /**
     * Logo de la marca
     * Se guarda en /uploads/marcas/
     */
    @Column(name = "logo", length = 255)
    private String logo;

    /**
     * Estado de la marca (activo o inactivo)
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // ========================================
    // AUDITORÍA
    // ========================================

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Verifica si la marca está activa
     */
    public boolean estaActiva() {
        return this.activo != null && this.activo;
    }
}