package com.miempresa.ecommerce.models.enums;

/**
 * ENUMERACIÓN: ESTADO DE VENTA
 * 
 * Representa los estados de una venta después de que
 * un pedido ha sido confirmado.
 */
public enum EstadoVenta {

    /**
     * COMPLETADA - La venta fue completada exitosamente
     */
    COMPLETADA("Completada", "Venta completada exitosamente", "#28A745"),

    /**
     * ANULADA - La venta fue anulada (devolución, error, etc.)
     */
    ANULADA("Anulada", "La venta ha sido anulada", "#DC3545");

    // ========================================
    // ATRIBUTOS
    // ========================================

    private final String nombre;
    private final String descripcion;
    private final String colorHex;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    EstadoVenta(String nombre, String descripcion, String colorHex) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.colorHex = colorHex;
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

    public String getColorHex() {
        return colorHex;
    }

    public String getClassBootstrap() {
        return switch (this) {
            case COMPLETADA -> "badge-success";
            case ANULADA -> "badge-danger";
        };
    }
}