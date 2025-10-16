package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD: USUARIO
 * 
 * Representa a los usuarios que pueden acceder al sistema (Admin y
 * Trabajadores).
 * Los clientes NO tienen usuario, solo se registran al hacer un pedido.
 * 
 * ¿Qué es una entidad?
 * - Es una clase Java que se convierte en una TABLA en la base de datos
 * - Cada objeto Usuario = una FILA en la tabla
 * - Cada atributo = una COLUMNA en la tabla
 * 
 * Anotaciones importantes:
 * 
 * @Entity - Le dice a Spring que esto es una tabla
 * @Table - Define el nombre de la tabla en la BD
 * @Id - Define la clave primaria (identificador único)
 * @GeneratedValue - El ID se genera automáticamente
 * @Column - Define las características de la columna
 * @ManyToOne - Relación muchos a uno (muchos usuarios tienen un perfil)
 */

@Entity // Esto es una entidad JPA (tabla en BD)
@Table(name = "usuarios") // Nombre de la tabla en MySQL
@Data // Lombok: genera getters, setters, toString, equals, hashCode automáticamente
@NoArgsConstructor // Lombok: genera constructor sin parámetros
@AllArgsConstructor // Lombok: genera constructor con todos los parámetros
@Builder // Lombok: permite crear objetos con patrón Builder
public class User {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    /**
     * ID único del usuario
     * Se genera automáticamente (auto-increment)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // DATOS PERSONALES
    // ========================================

    /**
     * Nombre del usuario
     * No puede ser nulo (obligatorio)
     * Máximo 100 caracteres
     */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Apellido del usuario
     */
    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    /**
     * Correo electrónico
     * Debe ser único (no pueden existir 2 usuarios con el mismo email)
     */
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    // ========================================
    // CREDENCIALES DE ACCESO
    // ========================================

    /**
     * Nombre de usuario para login
     * Debe ser único
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Contraseña encriptada con BCrypt
     * Nunca se guarda en texto plano por seguridad
     */
    @Column(name = "password", nullable = false)
    private String password;

    // ========================================
    // ESTADO Y PERFIL
    // ========================================

    /**
     * Estado del usuario (activo o inactivo)
     * Solo usuarios activos pueden iniciar sesión
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * RELACIÓN: Muchos usuarios pertenecen a UN perfil
     * 
     * @ManyToOne - Muchos a Uno
     * @JoinColumn - Define la columna que almacena la relación (perfil_id)
     *             fetch = FetchType.EAGER - Carga el perfil automáticamente al
     *             consultar el usuario
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Profile perfil;

    // ========================================
    // AUDITORÍA (Fechas automáticas)
    // ========================================

    /**
     * Fecha de creación del usuario
     * Se establece automáticamente al crear
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización
     * Se actualiza automáticamente al modificar
     */
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Obtiene el nombre completo del usuario
     * 
     * @return Nombre + Apellido
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }

    /**
     * Verifica si el usuario puede iniciar sesión
     * 
     * @return true si está activo, false si no
     */
    public boolean puedeIniciarSesion() {
        return this.activo != null && this.activo;
    }
}

/**
 * EXPLICACIÓN ADICIONAL PARA PRINCIPIANTES:
 * 
 * 1. ¿Qué es @Entity?
 * - Le dice a Spring Boot que esta clase representa una tabla en la base de
 * datos
 * 
 * 2. ¿Qué es @Id y @GeneratedValue?
 * - @Id: Define cual es la clave primaria (el identificador único)
 * - @GeneratedValue: El ID se crea automáticamente (1, 2, 3, 4...)
 * 
 * 3. ¿Qué es @Column?
 * - Define las características de la columna:
 * * nullable = false → NO puede estar vacío (es obligatorio)
 * * unique = true → NO puede repetirse (ej: email único)
 * * length = 100 → Máximo 100 caracteres
 * 
 * 4. ¿Qué es @ManyToOne?
 * - Define una relación entre tablas
 * - Muchos usuarios (Many) tienen UN perfil (One)
 * - Ejemplo:
 * * Usuario "Juan" → Perfil "ADMINISTRADOR"
 * * Usuario "María" → Perfil "ADMINISTRADOR"
 * * Usuario "Pedro" → Perfil "TRABAJADOR"
 * 
 * 5. ¿Qué hace Lombok (@Data, @Builder, etc.)?
 * - Genera código automáticamente para no escribir tanto
 * - @Data: genera getters, setters, toString, equals, hashCode
 * - @NoArgsConstructor: crea constructor vacío: new User()
 * - @AllArgsConstructor: crea constructor con todo: new User(id, nombre,
 * apellido...)
 * - @Builder: permite crear objetos así:
 * User user = User.builder()
 * .nombre("Juan")
 * .apellido("Pérez")
 * .email("juan@ejemplo.com")
 * .build();
 * 
 * 6. ¿Qué es @CreationTimestamp y @UpdateTimestamp?
 * - @CreationTimestamp: guarda la fecha ACTUAL cuando se crea el registro
 * - @UpdateTimestamp: guarda la fecha ACTUAL cada vez que se modifica
 * - ¡Automático! No tienes que poner las fechas manualmente
 */