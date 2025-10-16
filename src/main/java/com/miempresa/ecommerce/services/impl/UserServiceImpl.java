package com.miempresa.ecommerce.services.impl;

import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.repositories.UserRepository;
import com.miempresa.ecommerce.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE IMPLEMENTATION: USUARIO
 * 
 * Implementa la lógica de negocio para usuarios.
 * Aquí va TODO el código real.
 * 
 * @Service - Le dice a Spring que esto es un servicio
 * @Transactional - Las operaciones son transaccionales (si falla algo, se
 *                revierte todo)
 * @RequiredArgsConstructor - Lombok crea el constructor automáticamente
 * @Slf4j - Lombok habilita logs (para debugging)
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    // ========================================
    // DEPENDENCIAS (inyectadas automáticamente)
    // ========================================

    /**
     * Repository para acceder a la base de datos
     */
    private final UserRepository userRepository;

    /**
     * PasswordEncoder para encriptar contraseñas
     * Spring Security lo proporciona automáticamente
     */
    private final PasswordEncoder passwordEncoder;

    // ========================================
    // IMPLEMENTACIÓN DE MÉTODOS
    // ========================================

    @Override
    public User guardar(User user) {
        log.info("Guardando usuario: {}", user.getUsername());

        // Validación: username único
        if (user.getId() == null && existePorUsername(user.getUsername())) {
            log.error("El username {} ya existe", user.getUsername());
            throw new RuntimeException("El username ya está en uso");
        }

        // Validación: email único
        if (user.getId() == null && existePorEmail(user.getEmail())) {
            log.error("El email {} ya existe", user.getEmail());
            throw new RuntimeException("El email ya está en uso");
        }

        // Guardar en la base de datos
        User usuarioGuardado = userRepository.save(user);
        log.info("Usuario guardado exitosamente con ID: {}", usuarioGuardado.getId());

        return usuarioGuardado;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> buscarPorId(Long id) {
        log.debug("Buscando usuario por ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> obtenerTodos() {
        log.debug("Obteniendo todos los usuarios");
        return userRepository.findAll();
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando usuario con ID: {}", id);

        Optional<User> userOpt = buscarPorId(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Eliminación lógica: solo marcamos como inactivo
            user.setActivo(false);
            userRepository.save(user);
            log.info("Usuario {} marcado como inactivo", user.getUsername());
        } else {
            log.warn("No se encontró usuario con ID: {}", id);
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> buscarPorUsername(String username) {
        log.debug("Buscando usuario por username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> buscarPorEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> obtenerActivos() {
        log.debug("Obteniendo usuarios activos");
        return userRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> obtenerPorPerfil(Long perfilId) {
        log.debug("Obteniendo usuarios del perfil: {}", perfilId);
        return userRepository.findByPerfilId(perfilId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> buscarPorNombre(String nombre) {
        log.debug("Buscando usuarios por nombre: {}", nombre);
        return userRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeIniciarSesion(String username) {
        Optional<User> userOpt = buscarPorUsername(username);
        return userOpt.isPresent() && userOpt.get().puedeIniciarSesion();
    }

    @Override
    public User crearUsuario(User user, String passwordSinEncriptar) {
        log.info("Creando nuevo usuario: {}", user.getUsername());

        // Validaciones
        if (passwordSinEncriptar == null || passwordSinEncriptar.trim().isEmpty()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }

        if (passwordSinEncriptar.length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }

        // Encriptar contraseña
        String passwordEncriptada = passwordEncoder.encode(passwordSinEncriptar);
        user.setPassword(passwordEncriptada);

        // Por defecto, el usuario está activo
        if (user.getActivo() == null) {
            user.setActivo(true);
        }

        // Guardar
        return guardar(user);
    }

    @Override
    public User actualizar(Long id, User userActualizado) {
        log.info("Actualizando usuario con ID: {}", id);

        Optional<User> userExistenteOpt = buscarPorId(id);

        if (userExistenteOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        User userExistente = userExistenteOpt.get();

        // Actualizar campos
        userExistente.setNombre(userActualizado.getNombre());
        userExistente.setApellido(userActualizado.getApellido());
        userExistente.setEmail(userActualizado.getEmail());
        userExistente.setPerfil(userActualizado.getPerfil());

        // Username no se puede cambiar
        // Password no se cambia aquí (usar cambiarPassword())

        return userRepository.save(userExistente);
    }

    @Override
    public boolean cambiarPassword(Long userId, String passwordActual, String passwordNueva) {
        log.info("Cambiando contraseña del usuario ID: {}", userId);

        Optional<User> userOpt = buscarPorId(userId);

        if (userOpt.isEmpty()) {
            log.error("Usuario no encontrado");
            return false;
        }

        User user = userOpt.get();

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(passwordActual, user.getPassword())) {
            log.error("La contraseña actual no es correcta");
            return false;
        }

        // Validar nueva contraseña
        if (passwordNueva == null || passwordNueva.length() < 6) {
            log.error("La nueva contraseña debe tener al menos 6 caracteres");
            return false;
        }

        // Encriptar y guardar nueva contraseña
        user.setPassword(passwordEncoder.encode(passwordNueva));
        userRepository.save(user);

        log.info("Contraseña cambiada exitosamente");
        return true;
    }

    @Override
    public User cambiarEstado(Long id, boolean activo) {
        log.info("Cambiando estado del usuario ID: {} a {}", id, activo ? "ACTIVO" : "INACTIVO");

        Optional<User> userOpt = buscarPorId(id);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        User user = userOpt.get();
        user.setActivo(activo);

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarActivos() {
        return userRepository.contarUsuariosActivos();
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Qué hace @Transactional?
 * - Agrupa varias operaciones en una "transacción"
 * - Si algo falla, TODO se revierte (rollback)
 * - Ejemplo: al crear usuario, si falla el envío de email, no se guarda el
 * usuario
 * 
 * readOnly = true:
 * - Para operaciones de solo lectura (SELECT)
 * - Mejora el rendimiento
 * 
 * 2. ¿Qué hace @RequiredArgsConstructor?
 * - Lombok crea automáticamente el constructor con las dependencias "final"
 * - Es equivalente a:
 * ```java
 * public UserServiceImpl(UserRepository userRepository, PasswordEncoder
 * passwordEncoder) {
 * this.userRepository = userRepository;
 * this.passwordEncoder = passwordEncoder;
 * }
 * ```
 * 
 * 3. ¿Qué hace @Slf4j?
 * - Habilita el logger "log"
 * - Permite escribir mensajes en consola/archivo
 * - log.info() → información normal
 * - log.debug() → detalles técnicos
 * - log.error() → errores
 * - log.warn() → advertencias
 * 
 * 4. ¿Por qué encriptar contraseñas?
 * - NUNCA se guardan contraseñas en texto plano
 * - passwordEncoder.encode() encripta con BCrypt
 * - passwordEncoder.matches() verifica si coincide
 * 
 * Ejemplo:
 * Contraseña: "miPassword123"
 * Encriptada: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
 * 
 * 5. ¿Cuándo lanzar excepciones?
 * - Cuando algo NO se puede hacer
 * - throw new RuntimeException("mensaje")
 * - El Controller capturará la excepción y mostrará error al usuario
 */