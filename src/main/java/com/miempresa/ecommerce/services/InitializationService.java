package com.miempresa.ecommerce.services;

import com.miempresa.ecommerce.models.Permission;
import com.miempresa.ecommerce.models.Profile;
import com.miempresa.ecommerce.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SERVICE: INICIALIZACIÓN
 * 
 * Se ejecuta automáticamente al iniciar la aplicación.
 * Crea datos iniciales necesarios:
 * - Permisos del sistema
 * - Perfiles (ADMINISTRADOR, TRABAJADOR)
 * - Usuario administrador por defecto
 * - Configuraciones del sistema
 * 
 * CommandLineRunner ejecuta el método run() al arrancar Spring Boot
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InitializationService implements CommandLineRunner {

    private final PermissionService permissionService;
    private final ProfileService profileService;
    private final UserService userService;
    private final ConfigurationService configurationService;

    /**
     * Método que se ejecuta automáticamente al iniciar la aplicación
     */
    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("INICIANDO CONFIGURACIÓN DEL SISTEMA");
        log.info("========================================");

        try {
            // 1. Inicializar permisos
            inicializarPermisos();

            // 2. Inicializar perfiles
            inicializarPerfiles();

            // 3. Inicializar usuario administrador
            inicializarAdministrador();

            // 4. Inicializar configuraciones
            inicializarConfiguraciones();

            log.info("========================================");
            log.info("SISTEMA INICIALIZADO CORRECTAMENTE");
            log.info("========================================");

        } catch (Exception e) {
            log.error("ERROR AL INICIALIZAR EL SISTEMA: {}", e.getMessage(), e);
        }
    }

    /**
     * 1. Inicializa los permisos del sistema
     */
    private void inicializarPermisos() {
        log.info("1. Inicializando permisos...");
        permissionService.inicializarPermisos();
        log.info("✓ Permisos inicializados");
    }

    /**
     * 2. Inicializa los perfiles ADMINISTRADOR y TRABAJADOR
     */
    private void inicializarPerfiles() {
        log.info("2. Inicializando perfiles...");

        // PERFIL: ADMINISTRADOR (todos los permisos)
        if (!profileService.existePorNombre("ADMINISTRADOR")) {
            log.info("Creando perfil ADMINISTRADOR...");

            // Obtener TODOS los permisos
            List<Permission> todosLosPermisos = permissionService.obtenerActivos();
            Set<Permission> permisosAdmin = new HashSet<>(todosLosPermisos);

            Profile admin = Profile.builder()
                    .nombre("ADMINISTRADOR")
                    .descripcion("Acceso completo al sistema")
                    .activo(true)
                    .permisos(permisosAdmin)
                    .build();

            profileService.guardar(admin);
            log.info("✓ Perfil ADMINISTRADOR creado con {} permisos", permisosAdmin.size());
        }

        // PERFIL: TRABAJADOR (permisos limitados)
        if (!profileService.existePorNombre("TRABAJADOR")) {
            log.info("Creando perfil TRABAJADOR...");

            Set<Permission> permisosTrabajador = new HashSet<>();

            // Asignar solo algunos permisos al trabajador
            agregarPermisoSiExiste("MODULO_PRODUCTOS", permisosTrabajador);
            agregarPermisoSiExiste("MODULO_VENTAS", permisosTrabajador);
            agregarPermisoSiExiste("MODULO_CLIENTES", permisosTrabajador);
            agregarPermisoSiExiste("MODULO_REPORTES", permisosTrabajador);

            Profile trabajador = Profile.builder()
                    .nombre("TRABAJADOR")
                    .descripcion("Acceso limitado: productos, ventas, clientes y reportes")
                    .activo(true)
                    .permisos(permisosTrabajador)
                    .build();

            profileService.guardar(trabajador);
            log.info("✓ Perfil TRABAJADOR creado con {} permisos", permisosTrabajador.size());
        }

        log.info("✓ Perfiles inicializados");
    }

    /**
     * 3. Crea el usuario administrador por defecto
     */
    private void inicializarAdministrador() {
        log.info("3. Inicializando usuario administrador...");

        // Verificar si ya existe un admin
        if (userService.existePorUsername("admin")) {
            log.info("Usuario 'admin' ya existe");
            return;
        }

        // Obtener perfil ADMINISTRADOR
        Profile perfilAdmin = profileService.buscarPorNombre("ADMINISTRADOR")
                .orElseThrow(() -> new RuntimeException("Perfil ADMINISTRADOR no encontrado"));

        // Crear usuario administrador
        User admin = User.builder()
                .nombre("Administrador")
                .apellido("Sistema")
                .email("admin@miempresa.com")
                .username("admin")
                .perfil(perfilAdmin)
                .activo(true)
                .build();

        // Contraseña por defecto: admin123
        userService.crearUsuario(admin, "admin123");

        log.info("✓ Usuario administrador creado");
        log.info("  Username: admin");
        log.info("  Password: admin123");
        log.info("  ⚠️  IMPORTANTE: Cambia esta contraseña en producción");
    }

    /**
     * 4. Inicializa las configuraciones del sistema
     */
    private void inicializarConfiguraciones() {
        log.info("4. Inicializando configuraciones del sistema...");
        configurationService.inicializarConfiguraciones();
        log.info("✓ Configuraciones inicializadas");
    }

    /**
     * Método auxiliar para agregar permisos al perfil
     */
    private void agregarPermisoSiExiste(String codigoPermiso, Set<Permission> conjuntoPermisos) {
        permissionService.buscarPorCodigo(codigoPermiso).ifPresent(conjuntoPermisos::add);
    }
}

/**
 * EXPLICACIÓN COMPLETA:
 * 
 * 1. ¿Qué es CommandLineRunner?
 * - Interface de Spring Boot
 * - El método run() se ejecuta AUTOMÁTICAMENTE al iniciar
 * - Perfecto para inicializar datos
 * 
 * 2. Orden de inicialización:
 * Paso 1: Permisos (base de todo)
 * Paso 2: Perfiles (necesitan permisos)
 * Paso 3: Usuarios (necesitan perfiles)
 * Paso 4: Configuraciones (independientes)
 * 
 * 3. Perfiles creados:
 * 
 * ADMINISTRADOR:
 * - TODOS los permisos
 * - Puede acceder a TODO
 * - Gestiona usuarios, proveedores, configuración
 * 
 * TRABAJADOR:
 * - Solo 4 permisos:
 * * MODULO_PRODUCTOS
 * * MODULO_VENTAS
 * * MODULO_CLIENTES
 * * MODULO_REPORTES
 * - NO puede gestionar usuarios
 * - NO puede cambiar configuración
 * - NO puede gestionar proveedores
 * 
 * 4. Usuario por defecto:
 * Username: admin
 * Password: admin123
 * 
 * IMPORTANTE: En producción, cambiar inmediatamente esta contraseña
 * 
 * 5. ¿Cuándo se ejecuta?
 * - Al iniciar la aplicación por primera vez
 * - Cada vez que se reinicia (pero verifica si ya existen)
 * - NO duplica datos (verifica antes de crear)
 * 
 * 6. Logs en consola:
 * Al iniciar verás:
 * ========================================
 * INICIANDO CONFIGURACIÓN DEL SISTEMA
 * ========================================
 * 1. Inicializando permisos...
 * ✓ Permisos inicializados
 * 2. Inicializando permisos...
 * ...
 * ✓ Usuario administrador creado
 * Username: admin
 * Password: admin123
 * ========================================
 * SISTEMA INICIALIZADO CORRECTAMENTE
 * ========================================
 */