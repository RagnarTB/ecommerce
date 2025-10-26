package com.miempresa.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonBackReference; // Importar
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonBackReference("order-details")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @ToString.Exclude
    private Order pedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Product producto;

    @Column(name = "nombre_producto", nullable = false, length = 200)
    private String nombreProducto;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Métodos útiles (sin cambios)...
    // <<--- CÓDIGO DE MÉTODOS ÚTILES OMITIDO POR BREVEDAD --->>
    /**
     * Calcula el subtotal del detalle
     */
    public void calcularSubtotal() {
        if (this.precioUnitario != null && this.cantidad != null && this.cantidad > 0) {
            this.subtotal = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad))
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
            // Usar getPrecioActual() para capturar el precio de oferta si existe
            this.precioUnitario = this.producto.getPrecioActual()
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}