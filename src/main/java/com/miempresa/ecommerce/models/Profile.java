package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ENTIDAD: PERFIL (ROL)
 * 
 * Representa los roles o perfiles de usuario (ADMINISTRADOR, TRABAJADOR).
 * Cada perfil tiene una lista de permisos asociados.
 * 
 * Ejemplo:
 * - Perfil "ADMINISTRADOR" → tiene TODOS los permisos
 * - Perfil "TRABAJADOR" → tiene permisos limitados (productos, ventas,
 * clientes, reportes)
 */

@Entity
@Table(name = "perfiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // INFORMACIÓN DEL PERFIL
    // ========================================

    /**
     * Nombre del perfil
     * Ejemplo: "ADMINISTRADOR", "TRABAJADOR"
     */
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    /**
     * Descripción del perfil
     * Ejemplo: "Acceso completo al sistema"
     */
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /**
     * Estado del perfil (activo o inactivo)
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // ========================================
    // RELACIONES
    // ========================================

    /**
     * RELACIÓN: Un perfil tiene MUCHOS permisos
     * 
     * @ManyToMany - Muchos a Muchos
     *             - Un perfil puede tener varios permisos
     *             - Un permiso puede estar en varios perfiles
     * 
     * @JoinTable - Tabla intermedia que conecta perfiles con permisos
     *            - Nombre de la tabla: perfil_permiso
     *            - joinColumns: columna que referencia a perfiles (perfil_id)
     *            - inverseJoinColumns: columna que referencia a permisos
     *            (permiso_id)
     * 
     *            fetch = FetchType.EAGER - Carga los permisos automáticamente
     *            cascade = CascadeType.MERGE - Actualiza permisos al actualizar
     *            perfil
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "perfil_permiso", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "perfil_id"), // FK hacia perfiles
            inverseJoinColumns = @JoinColumn(name = "permiso_id") // FK hacia permisos
    )
    @Builder.Default
    private Set<Permission> permisos = new HashSet<>();

    // ========================================
    // AUDITORÍA
    // ========================================

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Agrega un permiso al perfil
     * 
     * @param permiso El permiso a agregar
     */
    public void agregarPermiso(Permission permiso) {
        this.permisos.add(permiso);
    }

    /**
     * Elimina un permiso del perfil
     * 
     * @param permiso El permiso a eliminar
     */
    public void eliminarPermiso(Permission permiso) {
        this.permisos.remove(permiso);
    }

    /**
     * Verifica si el perfil tiene un permiso específico
     * 
     * @param codigoPermiso El código del permiso a verificar
     * @return true si tiene el permiso, false si no
     */
    public boolean tienePermiso(String codigoPermiso) {
        return this.permisos.stream()
                .anyMatch(permiso -> permiso.getCodigo().equals(codigoPermiso));
    }

    /**
     * Limpia todos los permisos del perfil
     */
    public void limpiarPermisos() {
        this.permisos.clear();
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Qué es @ManyToMany?
 * - Relación muchos a muchos
 * - Un perfil puede tener VARIOS permisos
 * - Un permiso puede estar en VARIOS perfiles
 * 
 * Ejemplo:
 * Perfil "ADMINISTRADOR" tiene: MODULO_PRODUCTOS, MODULO_VENTAS,
 * MODULO_USUARIOS
 * Perfil "TRABAJADOR" tiene: MODULO_PRODUCTOS, MODULO_VENTAS
 * 
 * El permiso MODULO_PRODUCTOS está en AMBOS perfiles
 * 
 * 2. ¿Qué es @JoinTable?
 * - Define la tabla intermedia que conecta las dos tablas
 * - Se crea automáticamente una tabla "perfil_permiso" con:
 * * perfil_id (FK hacia perfiles)
 * * permiso_id (FK hacia permisos)
 * 
 * Ejemplo de tabla perfil_permiso:
 * | perfil_id | permiso_id |
 * |-----------|------------|
 * | 1 | 1 | → Admin tiene permiso 1
 * | 1 | 2 | → Admin tiene permiso 2
 * | 2 | 1 | → Trabajador tiene permiso 1
 * 
 * 3. ¿Por qué usar Set en lugar de List?
 * - Set NO permite duplicados (un permiso no puede estar 2 veces)
 * - List SÍ permite duplicados
 * - Para permisos, Set es más apropiado
 * 
 * 4. ¿Qué es @Builder.Default?
 * - Cuando usas @Builder, los valores por defecto se pierden
 * - @Builder.Default mantiene el valor inicial (new HashSet<>())
 */