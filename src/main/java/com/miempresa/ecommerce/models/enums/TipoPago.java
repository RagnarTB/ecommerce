package com.miempresa.ecommerce.models.enums;

/**
 * ENUMERACIÓN: TIPO DE PAGO
 * 
 * Define si la venta es al CONTADO (pago inmediato completo)
 * o a CRÉDITO (pago en cuotas).
 */
public enum TipoPago {

    /**
     * CONTADO - Pago inmediato y completo
     */
    CONTADO("Contado", "Pago completo al momento de la compra"),

    /**
     * CRÉDITO - Pago en cuotas
     */
    CREDITO("Crédito", "Pago diferido en cuotas");

    // ========================================
    // ATRIBUTOS
    // ========================================

    private final String nombre;
    private final String descripcion;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    TipoPago(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // ========================================
    // GETTERS
    // ========================================

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}