package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * ENTIDAD: DETALLE DE PEDIDO
 * 
 * Representa cada producto incluido en un pedido.
 * Un pedido puede tener múltiples detalles (varios productos).
 * 
 * Ejemplo:
 * Pedido #1:
 * - Detalle 1: 2 Laptops HP x S/ 2500 = S/ 5000
 * - Detalle 2: 3 Mouse Logitech x S/ 50 = S/ 150
 * Total: S/ 5150
 */

@Entity
@Table(name = "pedido_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // RELACIONES
    // ========================================

    /**
     * Pedido al que pertenece este detalle
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @ToString.Exclude
    private Order pedido;

    /**
     * Producto incluido en el pedido
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Product producto;

    // ========================================
    // INFORMACIÓN DEL PRODUCTO (snapshot)
    // ========================================

    /**
     * Nombre del producto al momento del pedido
     * Se guarda para mantener historial (por si cambia después)
     */
    @Column(name = "nombre_producto", nullable = false, length = 200)
    private String nombreProducto;

    /**
     * Precio unitario al momento del pedido
     * Se guarda para mantener historial (por si cambia después)
     */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // ========================================
    // CANTIDAD Y TOTALES
    // ========================================

    /**
     * Cantidad de unidades del producto
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Subtotal (precio_unitario * cantidad)
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Calcula el subtotal del detalle
     */
    public void calcularSubtotal() {
        if (this.precioUnitario != null && this.cantidad != null) {
            this.subtotal = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
        }
    }

    /**
     * Establece los datos del producto (snapshot)
     */
    public void establecerDatosProducto() {
        if (this.producto != null) {
            this.nombreProducto = this.producto.getNombre();
            this.precioUnitario = this.producto.getPrecioActual();
        }
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Por qué guardar nombreProducto y precioUnitario?
 * 
 * PROBLEMA sin snapshot:
 * - Cliente compra "Laptop HP" a S/ 2500
 * - Después, cambias el nombre a "Laptop HP Pavilion"
 * - Después, cambias el precio a S/ 2800
 * - Al ver el historial, se muestra el nuevo nombre y precio ❌
 * 
 * SOLUCIÓN con snapshot:
 * - Guardas nombre y precio al momento de la compra
 * - El historial siempre muestra lo que el cliente pagó ✅
 * 
 * 2. ¿Qué es un "snapshot"?
 * - Es una "fotografía" de los datos en un momento específico
 * - Se guarda para mantener historial exacto
 * 
 * 3. ¿Cómo se crea un detalle de pedido?
 * 
 * Código ejemplo:
 * ```java
 * OrderDetail detalle = OrderDetail.builder()
 * .producto(laptop)
 * .cantidad(2)
 * .build();
 * 
 * detalle.establecerDatosProducto(); // Copia nombre y precio
 * detalle.calcularSubtotal(); // Calcula: 2500 x 2 = 5000
 * 
 * pedido.agregarDetalle(detalle);
 * ```
 * 
 * 4. Estructura en la base de datos:
 * 
 * Tabla: pedido_detalles
 * | id | pedido_id | producto_id | nombre_producto | precio_unitario | cantidad
 * | subtotal |
 * |----|-----------|-------------|-----------------|-----------------|----------|----------|
 * | 1 | 5 | 10 | Laptop HP | 2500.00 | 2 | 5000.00 |
 * | 2 | 5 | 15 | Mouse Logitech | 50.00 | 3 | 150.00 |
 */