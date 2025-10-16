package com.miempresa.ecommerce.models.enums;

/**
 * ENUMERACIÓN: ESTADO DE CUOTA
 * 
 * Representa el estado de pago de una cuota de crédito.
 */
public enum EstadoCuota {

    /**
     * PENDIENTE - La cuota no ha sido pagada y no ha vencido
     */
    PENDIENTE("Pendiente", "La cuota aún no ha sido pagada", "#FFC107"),

    /**
     * PAGADA_PARCIAL - La cuota tiene abonos pero no está completamente pagada
     */
    PAGADA_PARCIAL("Pagada Parcial", "Se han realizado abonos parciales", "#17A2B8"),

    /**
     * PAGADA - La cuota ha sido pagada completamente
     */
    PAGADA("Pagada", "La cuota ha sido pagada en su totalidad", "#28A745"),

    /**
     * VENCIDA - La fecha de vencimiento pasó y la cuota no está pagada
     */
    VENCIDA("Vencida", "La cuota ha vencido sin ser pagada", "#DC3545");

    // ========================================
    // ATRIBUTOS
    // ========================================

    private final String nombre;
    private final String descripcion;
    private final String colorHex;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    EstadoCuota(String nombre, String descripcion, String colorHex) {
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
            case PENDIENTE -> "badge-warning";
            case PAGADA_PARCIAL -> "badge-info";
            case PAGADA -> "badge-success";
            case VENCIDA -> "badge-danger";
        };
    }
}