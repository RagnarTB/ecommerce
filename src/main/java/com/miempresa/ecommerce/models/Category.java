package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD: CATEGORÍA
 * 
 * Representa las categorías de productos.
 * Ejemplo: Electrónica, Ropa, Alimentos, etc.
 */

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // INFORMACIÓN DE LA CATEGORÍA
    // ========================================

    /**
     * Nombre de la categoría
     * Ejemplo: "Electrónica", "Ropa", "Alimentos"
     */
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    /**
     * Descripción de la categoría
     */
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /**
     * Ruta de la imagen de la categoría
     * Se guarda en /uploads/categorias/
     */
    @Column(name = "imagen", length = 255)
    private String imagen;

    /**
     * Estado de la categoría (activo o inactivo)
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * Orden de aparición en el catálogo
     */
    @Column(name = "orden")
    private Integer orden = 0;

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
     * Verifica si la categoría está activa
     */
    public boolean estaActiva() {
        return this.activo != null && this.activo;
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Para qué sirve el campo "imagen"?
 * - Guarda la ruta del archivo de imagen
 * - Ejemplo: "categorias/electronica.jpg"
 * - La imagen física se guarda en: /uploads/categorias/electronica.jpg
 * 
 * 2. ¿Para qué sirve el campo "orden"?
 * - Define en qué orden se muestran las categorías en el catálogo
 * - Ejemplo:
 * * Electrónica → orden = 1 (se muestra primero)
 * * Ropa → orden = 2
 * * Alimentos → orden = 3
 */