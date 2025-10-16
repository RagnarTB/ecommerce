package com.miempresa.ecommerce.services;

import com.miempresa.ecommerce.models.Permission;
import com.miempresa.ecommerce.repositories.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE: PERMISO
 * 
 * Gestiona los permisos del sistema.
 * Se inicializan al arrancar la aplicación.
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    // ========================================
    // OPERACIONES CRUD
    // ========================================

    public Permission guardar(Permission permission) {
        log.info("Guardando permiso: {}", permission.getCodigo());

        if (permission.getId() == null && permissionRepository.existsByCodigo(permission.getCodigo())) {
            throw new RuntimeException("Ya existe un permiso con ese código");
        }

        return permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    public Optional<Permission> buscarPorId(Long id) {
        return permissionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Permission> buscarPorCodigo(String codigo) {
        return permissionRepository.findByCodigo(codigo);
    }

    @Transactional(readOnly = true)
    public List<Permission> obtenerTodos() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Permission> obtenerActivos() {
        return permissionRepository.findByActivoTrueOrderByOrdenAsc();
    }

    @Transactional(readOnly = true)
    public List<Permission> obtenerActivosOrdenados() {
        return permissionRepository.findByActivoTrueOrderByOrdenAsc();
    }

    public Permission actualizar(Long id, Permission permissionActualizado) {
        Optional<Permission> permissionOpt = buscarPorId(id);

        if (permissionOpt.isEmpty()) {
            throw new RuntimeException("Permiso no encontrado");
        }

        Permission permission = permissionOpt.get();
        permission.setNombre(permissionActualizado.getNombre());
        permission.setDescripcion(permissionActualizado.getDescripcion());
        permission.setIcono(permissionActualizado.getIcono());
        permission.setOrden(permissionActualizado.getOrden());

        return permissionRepository.save(permission);
    }

    public Permission cambiarEstado(Long id, boolean activo) {
        Optional<Permission> permissionOpt = buscarPorId(id);

        if (permissionOpt.isEmpty()) {
            throw new RuntimeException("Permiso no encontrado");
        }

        Permission permission = permissionOpt.get();
        permission.setActivo(activo);

        return permissionRepository.save(permission);
    }

    // ========================================
    // INICIALIZACIÓN DE PERMISOS
    // ========================================

    /**
     * Crea los permisos del sistema si no existen
     * Se ejecuta al iniciar la aplicación
     */
    public void inicializarPermisos() {
        log.info("Inicializando permisos del sistema");

        // MÓDULO PRODUCTOS
        crearPermisoSiNoExiste(
                "MODULO_PRODUCTOS",
                "Gestión de Productos",
                "Permite acceder al módulo de productos, categorías y marcas",
                "fas fa-boxes",
                1);

        // MÓDULO VENTAS
        crearPermisoSiNoExiste(
                "MODULO_VENTAS",
                "Gestión de Ventas",
                "Permite acceder al módulo de ventas, pedidos y POS",
                "fas fa-cash-register",
                2);

        // MÓDULO CLIENTES
        crearPermisoSiNoExiste(
                "MODULO_CLIENTES",
                "Gestión de Clientes",
                "Permite acceder al módulo de clientes",
                "fas fa-users",
                3);

        // MÓDULO REPORTES
        crearPermisoSiNoExiste(
                "MODULO_REPORTES",
                "Reportes y Estadísticas",
                "Permite acceder a reportes y dashboards",
                "fas fa-chart-bar",
                4);

        // MÓDULO INVENTARIO
        crearPermisoSiNoExiste(
                "MODULO_INVENTARIO",
                "Gestión de Inventario",
                "Permite acceder al módulo de inventario y movimientos",
                "fas fa-warehouse",
                5);

        // MÓDULO USUARIOS (SOLO ADMIN)
        crearPermisoSiNoExiste(
                "MODULO_USUARIOS",
                "Gestión de Usuarios",
                "Permite acceder al módulo de usuarios y perfiles",
                "fas fa-user-shield",
                6);

        // MÓDULO PROVEEDORES (SOLO ADMIN)
        crearPermisoSiNoExiste(
                "MODULO_PROVEEDORES",
                "Gestión de Proveedores",
                "Permite acceder al módulo de proveedores",
                "fas fa-truck",
                7);

        // MÓDULO CONFIGURACIÓN (SOLO ADMIN)
        crearPermisoSiNoExiste(
                "MODULO_CONFIGURACION",
                "Configuración del Sistema",
                "Permite acceder a la configuración del sistema",
                "fas fa-cog",
                8);

        log.info("Permisos inicializados correctamente");
    }

    /**
     * Crea un permiso si no existe
     */
    private void crearPermisoSiNoExiste(String codigo, String nombre, String descripcion,
            String icono, int orden) {
        if (!permissionRepository.existsByCodigo(codigo)) {
            Permission permission = Permission.builder()
                    .codigo(codigo)
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .icono(icono)
                    .orden(orden)
                    .activo(true)
                    .build();

            permissionRepository.save(permission);
            log.info("Permiso creado: {}", codigo);
        }
    }
}

/**
 * EXPLICACIÓN DE LOS PERMISOS:
 * 
 * 1. MODULO_PRODUCTOS:
 * - Crear, editar, eliminar productos
 * - Gestionar categorías y marcas
 * - Subir imágenes
 * 
 * 2. MODULO_VENTAS:
 * - Registrar ventas en POS
 * - Ver ventas realizadas
 * - Gestionar créditos y pagos
 * - Convertir pedidos a ventas
 * 
 * 3. MODULO_CLIENTES:
 * - Ver y editar clientes
 * - Consultar historial de compras
 * 
 * 4. MODULO_REPORTES:
 * - Ver dashboards
 * - Generar reportes
 * - Exportar datos
 * 
 * 5. MODULO_INVENTARIO:
 * - Ver movimientos de inventario
 * - Registrar entradas/salidas
 * - Ver alertas de stock
 * 
 * 6. MODULO_USUARIOS (SOLO ADMIN):
 * - Crear/editar usuarios
 * - Asignar perfiles
 * - Gestionar permisos
 * 
 * 7. MODULO_PROVEEDORES (SOLO ADMIN):
 * - Gestionar proveedores
 * - Registrar compras
 * 
 * 8. MODULO_CONFIGURACION (SOLO ADMIN):
 * - Cambiar logo
 * - Modificar colores
 * - Configurar slider
 * - Editar información de contacto
 */