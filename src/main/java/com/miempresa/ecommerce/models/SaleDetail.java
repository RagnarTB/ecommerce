package com.miempresa.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonBackReference; // Importar
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venta_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonBackReference("sale-details") // Mismo identificador que en Sale
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @ToString.Exclude
    private Sale venta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Product producto;

    @Column(name = "nombre_producto", nullable = false, length = 200)
    private String nombreProducto;

    @Column(name = "codigo_sku", length = 50)
    private String codigoSku;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "descuento", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Métodos útiles (sin cambios)...
    // <<--- CÓDIGO DE MÉTODOS ÚTILES OMITIDO POR BREVEDAD --->>
    /**
     * Calcula el subtotal del detalle
     */
    public void calcularSubtotal() {
        if (this.precioUnitario != null && this.cantidad != null) {
            BigDecimal totalSinDescuento = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
            BigDecimal descAplicado = this.descuento != null ? this.descuento : BigDecimal.ZERO;
            this.subtotal = totalSinDescuento.subtract(descAplicado)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    /**
     * Establece los datos del producto (snapshot)
     */
    public void establecerDatosProducto() {
        if (this.producto != null) {
            this.nombreProducto = this.producto.getNombre();
            this.codigoSku = this.producto.getCodigoSku();
            // Usar getPrecioActual() para capturar el precio de oferta si existe
            this.precioUnitario = this.producto.getPrecioActual()
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}