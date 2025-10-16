package com.miempresa.ecommerce.models.enums;

/**
 * ENUMERACIÓN: MOTIVO DE MOVIMIENTO DE INVENTARIO
 * 
 * Especifica la razón del movimiento de stock.
 */
public enum MotivoMovimiento {

    // ========================================
    // MOTIVOS DE ENTRADA (aumentan stock)
    // ========================================

    /**
     * COMPRA - Compra de productos a proveedor
     */
    COMPRA(TipoMovimiento.ENTRADA, "Compra a proveedor", "Se compró stock al proveedor"),

    /**
     * AJUSTE_POSITIVO - Corrección manual que aumenta stock
     */
    AJUSTE_POSITIVO(TipoMovimiento.ENTRADA, "Ajuste positivo", "Corrección manual de inventario (aumento)"),

    /**
     * DEVOLUCION - Cliente devuelve producto
     */
    DEVOLUCION(TipoMovimiento.ENTRADA, "Devolución de cliente", "El cliente devolvió el producto"),

    // ========================================
    // MOTIVOS DE SALIDA (disminuyen stock)
    // ========================================

    /**
     * VENTA - Venta de producto
     */
    VENTA(TipoMovimiento.SALIDA, "Venta", "Producto vendido a cliente"),

    /**
     * AJUSTE_NEGATIVO - Corrección manual que disminuye stock
     */
    AJUSTE_NEGATIVO(TipoMovimiento.SALIDA, "Ajuste negativo", "Corrección manual de inventario (disminución)"),

    /**
     * MERMA - Producto dañado, vencido o perdido
     */
    MERMA(TipoMovimiento.SALIDA, "Merma", "Producto dañado, vencido o extraviado");

    // ========================================
    // ATRIBUTOS
    // ========================================

    private final TipoMovimiento tipoMovimiento;
    private final String nombre;
    private final String descripcion;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    MotivoMovimiento(TipoMovimiento tipoMovimiento, String nombre, String descripcion) {
        this.tipoMovimiento = tipoMovimiento;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // ========================================
    // GETTERS
    // ========================================

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Verifica si este motivo es de entrada
     */
    public boolean esEntrada() {
        return this.tipoMovimiento == TipoMovimiento.ENTRADA;
    }

    /**
     * Verifica si este motivo es de salida
     */
    public boolean esSalida() {
        return this.tipoMovimiento == TipoMovimiento.SALIDA;
    }
}