package com.miempresa.ecommerce.models.enums;

/**
 * ENUMERACIÓN: MÉTODO DE PAGO
 * 
 * Representa las diferentes formas de pago que acepta el sistema.
 * Puede haber multipagos (combinación de varios métodos).
 */
public enum MetodoPago {

    /**
     * EFECTIVO - Pago en efectivo
     */
    EFECTIVO("Efectivo", "Pago en billetes y monedas", "fas fa-money-bill-wave"),

    /**
     * TARJETA - Pago con tarjeta de crédito o débito
     */
    TARJETA("Tarjeta", "Pago con tarjeta de crédito/débito", "fas fa-credit-card"),

    /**
     * TRANSFERENCIA - Transferencia bancaria
     */
    TRANSFERENCIA("Transferencia", "Transferencia bancaria", "fas fa-exchange-alt"),

    /**
     * YAPE - Pago con Yape (Perú)
     */
    YAPE("Yape", "Pago con aplicación Yape", "fas fa-mobile-alt");

    // ========================================
    // ATRIBUTOS
    // ========================================

    private final String nombre;
    private final String descripcion;
    private final String icono; // Icono de FontAwesome

    // ========================================
    // CONSTRUCTOR
    // ========================================

    MetodoPago(String nombre, String descripcion, String icono) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
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

    public String getIcono() {
        return icono;
    }
}