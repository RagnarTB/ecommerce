package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * ENTIDAD: PROVEEDOR
 * 
 * Representa a los proveedores de productos.
 * Los proveedores son empresas (RUC) que venden productos al negocio.
 */

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // IDENTIFICACIÓN
    // ========================================

    /**
     * Número de RUC del proveedor
     * Debe ser único
     */

    @NotBlank(message = "RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "RUC debe tener 11 dígitos")
    @Column(name = "ruc", nullable = false, unique = true, length = 11)
    private String ruc;

    /**
     * Razón social del proveedor
     */
    @Column(name = "razon_social", nullable = false, length = 200)
    @NotBlank(message = "Razón Social es obligatoria")
    private String razonSocial;

    // ========================================
    // DATOS DE CONTACTO
    // ========================================

    /**
     * Dirección del proveedor
     */
    @Column(name = "direccion", length = 300)
    private String direccion;

    /**
     * Teléfono del proveedor
     */
    @Column(name = "telefono", length = 20)
    @Pattern(regexp = "\\+?\\d{7,15}", message = "Teléfono inválido")
    private String telefono;

    /**
     * Email del proveedor
     */
    @Column(name = "email", length = 150)
    @Email(message = "Email inválido")
    private String email;

    /**
     * Nombre de la persona de contacto
     */
    @Column(name = "contacto_nombre", length = 150)
    private String contactoNombre;

    /**
     * Teléfono del contacto
     */
    @Column(name = "contacto_telefono", length = 20)
    private String contactoTelefono;

    // ========================================
    // ESTADO
    // ========================================

    /**
     * Estado del proveedor (activo o inactivo)
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    private String notas;

    // ========================================
    // AUDITORÍA
    // ========================================

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Verifica si el proveedor está activo
     */
    public boolean estaActivo() {
        return this.activo != null && this.activo;
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Para qué sirven los proveedores?
 * - Son las empresas que venden productos al negocio
 * - Cuando compras stock, registras:
 * * Proveedor
 * * Productos comprados
 * * Cantidades
 * * Precios de compra
 * 
 * 2. ¿Cómo se relaciona con las compras?
 * Flujo:
 * 1. Registras proveedor (RUC, razón social, contacto)
 * 2. Haces una compra al proveedor
 * 3. Registras entrada de inventario
 * 4. El stock de los productos aumenta
 */