package com.miempresa.ecommerce.models;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Representa un producto en el carrito de compras
 * No se persist en BD, solo en sesión HTTP
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productoId;
    private String nombre;
    private BigDecimal precio;
    private Integer cantidad;
    private String imagenUrl;
    private Integer stockDisponible;

    /**
     * Calcula el subtotal del item
     */
    public BigDecimal getSubtotal() {
        if (precio == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return precio.multiply(BigDecimal.valueOf(cantidad))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Incrementa la cantidad
     */
    public void incrementarCantidad() {
        if (cantidad == null) {
            cantidad = 1;
        } else {
            cantidad++;
        }
    }

    /**
     * Decrementa la cantidad
     */
    public void decrementarCantidad() {
        if (cantidad != null && cantidad > 1) {
            cantidad--;
        }
    }

    /**
     * Verifica si hay stock suficiente
     */
    public boolean hayStockSuficiente() {
        if (stockDisponible == null || cantidad == null) {
            return false;
        }
        return cantidad <= stockDisponible;
    }
}
