package com.miempresa.ecommerce.models.enums;

/**
 * ENUMERACIÓN: TIPO DE MOVIMIENTO DE INVENTARIO
 * 
 * Define si el movimiento aumenta (ENTRADA) o disminuye (SALIDA) el stock.
 */
public enum TipoMovimiento {

    /**
     * ENTRADA - Aumenta el stock
     */
    ENTRADA("Entrada", "Incremento de stock", "#28A745", "+"),

    /**
     * SALIDA - Disminuye el stock
     */
    SALIDA("Salida", "Disminución de stock", "#DC3545", "-");

    // ========================================
    // ATRIBUTOS
    // ========================================

    private final String nombre;
    private final String descripcion;
    private final String colorHex;
    private final String simbolo;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    TipoMovimiento(String nombre, String descripcion, String colorHex, String simbolo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.colorHex = colorHex;
        this.simbolo = simbolo;
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

    public String getSimbolo() {
        return simbolo;
    }
}