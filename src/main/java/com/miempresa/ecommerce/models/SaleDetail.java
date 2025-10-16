package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * ENTIDAD: DETALLE DE VENTA
 * 
 * Representa cada producto vendido en una venta.
 * Igual que OrderDetail, pero para ventas confirmadas.
 */

@Entity
@Table(name = "venta_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDetail {

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
     * Venta a la que pertenece este detalle
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @ToString.Exclude
    private Sale venta;

    /**
     * Producto vendido
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Product producto;

    // ========================================
    // SNAPSHOT DEL PRODUCTO
    // ========================================

    /**
     * Nombre del producto al momento de la venta
     */
    @Column(name = "nombre_producto", nullable = false, length = 200)
    private String nombreProducto;

    /**
     * Código SKU del producto al momento de la venta
     */
    @Column(name = "codigo_sku", length = 50)
    private String codigoSku;

    /**
     * Precio unitario al momento de la venta
     */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // ========================================
    // CANTIDAD Y TOTALES
    // ========================================

    /**
     * Cantidad de unidades vendidas
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Descuento aplicado al detalle (si existe)
     */
    @Column(name = "descuento", precision = 10, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    /**
     * Subtotal (precio_unitario * cantidad - descuento)
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
            BigDecimal total = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
            BigDecimal desc = this.descuento != null ? this.descuento : BigDecimal.ZERO;
            this.subtotal = total.subtract(desc);
        }
    }

    /**
     * Establece los datos del producto (snapshot)
     */
    public void establecerDatosProducto() {
        if (this.producto != null) {
            this.nombreProducto = this.producto.getNombre();
            this.codigoSku = this.producto.getCodigoSku();
            this.precioUnitario = this.producto.getPrecioActual();
        }
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. Diferencia entre OrderDetail y SaleDetail:
 * 
 * OrderDetail (Pedido):
 * - No descuenta stock
 * - No genera comprobante
 * - Puede cancelarse fácilmente
 * 
 * SaleDetail (Venta):
 * - SÍ descuenta stock
 * - SÍ aparece en comprobante
 * - Anular requiere proceso formal
 * 
 * 2. Ejemplo en la base de datos:
 * 
 * Tabla: venta_detalles
 * | id | venta_id | producto_id | nombre_producto | precio_unitario | cantidad
 * | descuento | subtotal |
 * |----|----------|-------------|-----------------|-----------------|----------|-----------|----------|
 * | 1 | 10 | 25 | Laptop HP | 2500.00 | 2 | 100.00 | 4900.00 |
 * | 2 | 10 | 30 | Mouse Logitech | 50.00 | 1 | 0.00 | 50.00 |
 */