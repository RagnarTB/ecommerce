package com.miempresa.ecommerce.models.enums;

/**
 * ENUMERACIÓN: TIPO DE ENTREGA
 * 
 * Define cómo se entregará el pedido al cliente.
 */
public enum TipoEntrega {

    /**
     * RECOJO_TIENDA - El cliente recoge en la tienda física
     */
    RECOJO_TIENDA("Recojo en tienda", "El cliente recoge el pedido en la tienda", 0.0),

    /**
     * DELIVERY - Entrega a domicilio
     */
    DELIVERY("Delivery", "Entrega a domicilio del cliente", 0.0); // El costo se calcula según zona

    // ========================================
    // ATRIBUTOS
    // ========================================

    private final String nombre;
    private final String descripcion;
    private final Double costoBase; // Costo base (puede variar)

    // ========================================
    // CONSTRUCTOR
    // ========================================

    TipoEntrega(String nombre, String descripcion, Double costoBase) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoBase = costoBase;
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

    public Double getCostoBase() {
        return costoBase;
    }
}