package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD: IMAGEN DE PRODUCTO
 * 
 * Representa las imágenes de un producto.
 * Cada producto puede tener hasta 5 imágenes.
 * Una de ellas debe ser la imagen principal.
 */

@Entity
@Table(name = "producto_imagenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // RELACIÓN CON PRODUCTO
    // ========================================

    /**
     * RELACIÓN: Muchas imágenes pertenecen a UN producto
     * 
     * @ManyToOne - Muchas imágenes → Un producto
     *            Esta es la relación "dueña" (tiene la FK)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude // Evita recursión infinita en toString()
    private Product producto;

    // ========================================
    // INFORMACIÓN DE LA IMAGEN
    // ========================================

    /**
     * URL o ruta de la imagen
     * Ejemplo: "productos/laptop-hp-001.jpg"
     */
    @Column(name = "url", nullable = false, length = 255)
    private String url;

    /**
     * Orden de aparición
     * 1 = primera imagen, 2 = segunda, etc.
     */
    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    /**
     * Indica si es la imagen principal
     * Solo una imagen por producto debe ser principal
     */
    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = false;

    // ========================================
    // AUDITORÍA
    // ========================================

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Obtiene la URL completa de la imagen
     */
    public String getUrlCompleta() {
        return "/uploads/" + this.url;
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Qué es FetchType.LAZY?
 * - LAZY = perezoso → NO carga el producto automáticamente
 * - EAGER = ansioso → SÍ carga el producto automáticamente
 * 
 * En este caso usamos LAZY porque:
 * - Cuando cargamos una imagen, no siempre necesitamos el producto completo
 * - Mejora el rendimiento (menos consultas a la BD)
 * 
 * 2. ¿Qué es @ToString.Exclude?
 * - Evita que toString() incluya el producto
 * - Previene recursión infinita:
 * * Producto llama a toString() → muestra imágenes
 * * Imagen llama a toString() → muestra producto
 * * Producto llama a toString() → ... ¡bucle infinito!
 * 
 * 3. ¿Cómo se guardan las imágenes?
 * 
 * Flujo:
 * 1. Usuario sube imagen desde formulario
 * 2. Backend guarda archivo físico en: /uploads/productos/laptop-001.jpg
 * 3. Backend guarda registro en BD:
 * | id | producto_id | url | orden | es_principal |
 * |----|-------------|----------------------------|-------|--------------|
 * | 1 | 5 | productos/laptop-001.jpg | 1 | true |
 * | 2 | 5 | productos/laptop-002.jpg | 2 | false |
 * 
 * 4. En HTML se muestra: <img src="/uploads/productos/laptop-001.jpg">
 * 
 * 4. ¿Cómo aseguramos que solo haya UNA imagen principal?
 * - Se valida en el Service antes de guardar
 * - Si se marca una nueva como principal, las demás se marcan como NO principal
 */