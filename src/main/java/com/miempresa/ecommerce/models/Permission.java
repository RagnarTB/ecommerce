package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * ENTIDAD: PERMISO
 * 
 * Representa los permisos del sistema que se asignan a los perfiles.
 * Un permiso da acceso a un módulo completo.
 * 
 * Permisos disponibles:
 * - MODULO_PRODUCTOS
 * - MODULO_VENTAS
 * - MODULO_CLIENTES
 * - MODULO_REPORTES
 * - MODULO_USUARIOS (solo admin)
 * - MODULO_CONFIGURACION (solo admin)
 * - MODULO_PROVEEDORES (solo admin)
 * - MODULO_INVENTARIO
 */

@Entity
@Table(name = "permisos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // INFORMACIÓN DEL PERMISO
    // ========================================

    /**
     * Código único del permiso
     * Ejemplo: "MODULO_PRODUCTOS", "MODULO_VENTAS"
     * Se usa en el código para verificar permisos
     */
    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    /**
     * Nombre descriptivo del permiso
     * Ejemplo: "Gestión de Productos"
     */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Descripción de qué permite hacer
     * Ejemplo: "Permite crear, editar y eliminar productos"
     */
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /**
     * Icono para mostrar en el menú (FontAwesome)
     * Ejemplo: "fas fa-boxes"
     */
    @Column(name = "icono", length = 50)
    private String icono;

    /**
     * Orden de aparición en el menú
     */
    @Column(name = "orden")
    private Integer orden = 0;

    /**
     * Estado del permiso (activo o inactivo)
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Verifica si este permiso está activo
     */
    public boolean estaActivo() {
        return this.activo != null && this.activo;
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Cómo funcionan los permisos?
 * 
 * Flujo:
 * Usuario "Juan" → Perfil "TRABAJADOR" → Permisos: [MODULO_PRODUCTOS,
 * MODULO_VENTAS]
 * 
 * Cuando Juan inicia sesión:
 * - El sistema carga su perfil
 * - El perfil trae sus permisos
 * - El menú lateral solo muestra las opciones con permisos
 * - Si intenta acceder a un módulo sin permiso → Error 403 (Prohibido)
 * 
 * 2. ¿Por qué usar código en lugar de ID?
 * 
 * MAL:
 * if (usuario.tienePermiso(1)) { ... } // ¿Qué es el permiso 1?
 * 
 * BIEN:
 * if (usuario.tienePermiso("MODULO_PRODUCTOS")) { ... } // Claro y entendible
 * 
 * 3. ¿Para qué sirve el campo "orden"?
 * - Define en qué orden aparecen los módulos en el menú
 * - Ejemplo:
 * * MODULO_PRODUCTOS → orden = 1
 * * MODULO_VENTAS → orden = 2
 * * MODULO_CLIENTES → orden = 3
 * 
 * 4. ¿Para qué sirve el campo "icono"?
 * - Se muestra en el menú lateral para que sea más visual
 * - Usa iconos de FontAwesome
 * - Ejemplo: "fas fa-boxes" muestra un icono de cajas
 */