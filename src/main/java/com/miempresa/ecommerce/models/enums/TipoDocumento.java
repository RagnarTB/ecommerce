package com.miempresa.ecommerce.models.enums;

/**
 * ENUMERACIÓN: TIPO DE DOCUMENTO
 * 
 * Un ENUM es un tipo de dato que solo puede tener valores específicos.
 * En este caso, los tipos de documento pueden ser DNI o RUC.
 * 
 * ¿Por qué usar ENUM?
 * - Evita errores de tipeo (no puedes poner "dni" en minúsculas por error)
 * - El código es más claro y seguro
 * - IntelliJ te ayuda con autocompletado
 */
public enum TipoDocumento {

    /**
     * DNI - Documento Nacional de Identidad
     * Para personas naturales (8 dígitos)
     */
    DNI("DNI", "Documento Nacional de Identidad", 8),

    /**
     * RUC - Registro Único de Contribuyentes
     * Para empresas (11 dígitos)
     */
    RUC("RUC", "Registro Único de Contribuyentes", 11);

    // ========================================
    // ATRIBUTOS DEL ENUM
    // ========================================

    /**
     * Código corto del tipo de documento
     */
    private final String codigo;

    /**
     * Descripción completa del tipo de documento
     */
    private final String descripcion;

    /**
     * Cantidad de dígitos que debe tener
     */
    private final int longitudEsperada;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    /**
     * Constructor del ENUM
     * Los ENUMs tienen constructores privados
     */
    TipoDocumento(String codigo, String descripcion, int longitudEsperada) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.longitudEsperada = longitudEsperada;
    }

    // ========================================
    // GETTERS (métodos para obtener valores)
    // ========================================

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getLongitudEsperada() {
        return longitudEsperada;
    }

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Valida si un número de documento tiene la longitud correcta
     * 
     * @param numeroDocumento El número a validar
     * @return true si la longitud es correcta, false si no
     */
    public boolean validarLongitud(String numeroDocumento) {
        return numeroDocumento != null &&
                numeroDocumento.length() == this.longitudEsperada;
    }

    /**
     * Obtiene el tipo de documento según el número
     * Si tiene 8 dígitos → DNI
     * Si tiene 11 dígitos → RUC
     * 
     * @param numeroDocumento El número de documento
     * @return El tipo de documento correspondiente o null si no coincide
     */
    public static TipoDocumento obtenerPorNumero(String numeroDocumento) {
        if (numeroDocumento == null) {
            return null;
        }

        int longitud = numeroDocumento.length();

        if (longitud == 8) {
            return DNI;
        } else if (longitud == 11) {
            return RUC;
        }

        return null;
    }
}