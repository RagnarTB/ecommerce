package com.miempresa.ecommerce.models.enums;

/**
 * ENUMERACIÓN: ESTADO DE PEDIDO
 * 
 * Representa los diferentes estados por los que puede pasar un pedido
 * desde que el cliente lo crea hasta que se cancela o confirma.
 */
public enum EstadoPedido {

    /**
     * PENDIENTE - El pedido fue creado pero aún no confirmado por el admin
     */
    PENDIENTE("Pendiente", "El pedido está esperando confirmación", "#FFA500"),

    /**
     * CONFIRMADO - El admin revisó y confirmó el pedido
     * En este punto se puede convertir a venta
     */
    CONFIRMADO("Confirmado", "El pedido ha sido confirmado", "#28A745"),

    /**
     * CANCELADO - El pedido fue cancelado (por cliente o admin)
     */
    CANCELADO("Cancelado", "El pedido ha sido cancelado", "#DC3545");

    // ========================================
    // ATRIBUTOS
    // ========================================

    private final String nombre;
    private final String descripcion;
    private final String colorHex; // Color para mostrar en la interfaz

    // ========================================
    // CONSTRUCTOR
    // ========================================

    EstadoPedido(String nombre, String descripcion, String colorHex) {
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

    /**
     * Obtiene la clase CSS de Bootstrap según el estado
     * Para usar en badges/etiquetas
     */
    public String getClassBootstrap() {
        return switch (this) {
            case PENDIENTE -> "badge-warning";
            case CONFIRMADO -> "badge-success";
            case CANCELADO -> "badge-danger";
        };
    }
}