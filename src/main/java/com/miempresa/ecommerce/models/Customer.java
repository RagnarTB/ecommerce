package com.miempresa.ecommerce.models;

import com.miempresa.ecommerce.models.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD: CLIENTE
 * 
 * Representa a los clientes que compran en la tienda.
 * Los clientes NO tienen usuario/contraseña.
 * Se registran automáticamente al hacer su primer pedido.
 * 
 * Tipos de cliente:
 * - PERSONA_NATURAL (DNI - 8 dígitos)
 * - EMPRESA (RUC - 11 dígitos)
 */

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // TIPO Y DOCUMENTO
    // ========================================

    /**
     * Tipo de documento (DNI o RUC)
     * Se guarda como String en la BD
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private TipoDocumento tipoDocumento;

    /**
     * Número de documento
     * Debe ser único (no pueden existir 2 clientes con el mismo DNI/RUC)
     */
    @Column(name = "numero_documento", nullable = false, unique = true, length = 11)
    private String numeroDocumento;

    // ========================================
    // DATOS PERSONALES (PERSONA NATURAL)
    // ========================================

    /**
     * Nombres (solo para persona natural)
     * Ejemplo: "Juan Carlos"
     */
    @Column(name = "nombres", length = 100)
    private String nombres;

    /**
     * Apellido paterno (solo para persona natural)
     */
    @Column(name = "apellido_paterno", length = 100)
    private String apellidoPaterno;

    /**
     * Apellido materno (solo para persona natural)
     */
    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    // ========================================
    // DATOS EMPRESARIALES (EMPRESA)
    // ========================================

    /**
     * Razón social (solo para empresa)
     * Ejemplo: "TECNOLOGÍA Y SERVICIOS SAC"
     */
    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    // ========================================
    // DATOS DE CONTACTO
    // ========================================

    /**
     * Dirección completa
     */
    @Column(name = "direccion", length = 300)
    private String direccion;

    /**
     * Distrito
     */
    @Column(name = "distrito", length = 100)
    private String distrito;

    /**
     * Provincia
     */
    @Column(name = "provincia", length = 100)
    private String provincia;

    /**
     * Departamento
     */
    @Column(name = "departamento", length = 100)
    private String departamento;

    /**
     * Teléfono
     */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /**
     * Email
     */
    @Column(name = "email", length = 150)
    private String email;

    // ========================================
    // ESTADO
    // ========================================

    /**
     * Estado del cliente (activo o inactivo)
     * Si está inactivo, no puede hacer pedidos
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

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
     * Obtiene el nombre completo del cliente
     * 
     * @return Nombre completo según tipo de documento
     */
    public String getNombreCompleto() {
        if (this.tipoDocumento == TipoDocumento.DNI) {
            // Persona natural: apellidos + nombres
            return (this.apellidoPaterno != null ? this.apellidoPaterno + " " : "")
                    + (this.apellidoMaterno != null ? this.apellidoMaterno + " " : "")
                    + (this.nombres != null ? this.nombres : "");
        } else {
            // Empresa: razón social
            return this.razonSocial != null ? this.razonSocial : "";
        }
    }

    /**
     * Verifica si es persona natural
     */
    public boolean esPersonaNatural() {
        return this.tipoDocumento == TipoDocumento.DNI;
    }

    /**
     * Verifica si es empresa
     */
    public boolean esEmpresa() {
        return this.tipoDocumento == TipoDocumento.RUC;
    }

    /**
     * Obtiene la dirección completa con distrito, provincia y departamento
     */
    public String getDireccionCompleta() {
        StringBuilder direccionCompleta = new StringBuilder();

        if (this.direccion != null) {
            direccionCompleta.append(this.direccion);
        }

        if (this.distrito != null) {
            direccionCompleta.append(", ").append(this.distrito);
        }

        if (this.provincia != null) {
            direccionCompleta.append(", ").append(this.provincia);
        }

        if (this.departamento != null) {
            direccionCompleta.append(", ").append(this.departamento);
        }

        return direccionCompleta.toString();
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Qué es @Enumerated?
 * - Permite guardar un ENUM en la base de datos
 * - EnumType.STRING → guarda el texto ("DNI", "RUC")
 * - EnumType.ORDINAL → guarda el número (0, 1) - NO RECOMENDADO
 * 
 * Ejemplo en BD:
 * | id | tipo_documento | numero_documento |
 * |----|----------------|------------------|
 * | 1 | DNI | 46027896 |
 * | 2 | RUC | 20601030013 |
 * 
 * 2. ¿Por qué algunos campos pueden ser NULL?
 * - Si es persona natural (DNI):
 * * nombres, apellidoPaterno, apellidoMaterno → tienen datos
 * * razonSocial → NULL
 * 
 * - Si es empresa (RUC):
 * * razonSocial → tiene datos
 * * nombres, apellidoPaterno, apellidoMaterno → NULL
 * 
 * 3. ¿Cómo se registra un cliente automáticamente?
 * 
 * Flujo:
 * 1. Cliente en web ingresa DNI: 46027896
 * 2. Sistema busca en BD: SELECT * FROM clientes WHERE numero_documento =
 * '46027896'
 * 3. Si NO existe:
 * - Llama API Decolecta
 * - Obtiene datos (nombres, apellidos)
 * - Crea nuevo registro en tabla clientes
 * 4. Si SÍ existe:
 * - Usa los datos existentes
 * - NO llama a la API (ahorra dinero)
 * 
 * 4. ¿Para qué sirven los métodos getNombreCompleto() y getDireccionCompleta()?
 * - Son métodos auxiliares que no se guardan en la BD
 * - Facilitan mostrar información en las vistas
 * - Ejemplo en HTML: ${cliente.nombreCompleto}
 */