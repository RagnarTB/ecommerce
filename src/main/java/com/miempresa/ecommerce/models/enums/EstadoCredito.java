package com.miempresa.ecommerce.models.enums;

/**
 * ENUMERACIÓN: ESTADO DE CRÉDITO
 * 
 * Representa el estado general de un crédito (venta a cuotas).
 */
public enum EstadoCredito {

    /**
     * ACTIVO - El crédito está activo con cuotas pendientes
     */
    ACTIVO("Activo", "El crédito tiene cuotas pendientes", "#FFC107"),

    /**
     * COMPLETADO - Todas las cuotas han sido pagadas
     */
    COMPLETADO("Completado", "Todas las cuotas han sido pagadas", "#28A745"),

    /**
     * ANULADO - El crédito fue anulado
     */
    ANULADO("Anulado", "El crédito ha sido anulado", "#DC3545");

    // ========================================
    // ATRIBUTOS
    // ========================================

    private final String nombre;
    private final String descripcion;
    private final String colorHex;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    EstadoCredito(String nombre, String descripcion, String colorHex) {
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
            case ACTIVO -> "badge-warning";
            case COMPLETADO -> "badge-success";
            case ANULADO -> "badge-danger";
        };
    }
}