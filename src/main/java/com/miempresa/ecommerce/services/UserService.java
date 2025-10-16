package com.miempresa.ecommerce.services;

import com.miempresa.ecommerce.models.User;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE INTERFACE: USUARIO
 * 
 * Define los métodos (operaciones) que se pueden hacer con usuarios.
 * La implementación real está en UserServiceImpl.
 * 
 * ¿Por qué usar interfaces?
 * - Define un "contrato" de lo que debe hacer el servicio
 * - Facilita cambiar la implementación sin romper nada
 * - Permite hacer testing más fácilmente
 */
public interface UserService {

    // ========================================
    // OPERACIONES CRUD BÁSICAS
    // ========================================

    /**
     * Guarda un nuevo usuario o actualiza uno existente
     * 
     * Validaciones:
     * - Username único
     * - Email único
     * - Contraseña encriptada
     * - Perfil válido
     * 
     * @param user Usuario a guardar
     * @return Usuario guardado
     */
    User guardar(User user);

    /**
     * Busca un usuario por su ID
     * 
     * @param id ID del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> buscarPorId(Long id);

    /**
     * Obtiene todos los usuarios
     * 
     * @return Lista de todos los usuarios
     */
    List<User> obtenerTodos();

    /**
     * Elimina un usuario por su ID
     * 
     * Nota: En realidad solo lo marca como inactivo (eliminación lógica)
     * 
     * @param id ID del usuario a eliminar
     */
    void eliminar(Long id);

    // ========================================
    // BÚSQUEDAS ESPECÍFICAS
    // ========================================

    /**
     * Busca un usuario por su username
     * Usado principalmente para el login
     * 
     * @param username Nombre de usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> buscarPorUsername(String username);

    /**
     * Busca un usuario por su email
     * 
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> buscarPorEmail(String email);

    /**
     * Obtiene solo usuarios activos
     * 
     * @return Lista de usuarios activos
     */
    List<User> obtenerActivos();

    /**
     * Obtiene usuarios de un perfil específico
     * 
     * @param perfilId ID del perfil
     * @return Lista de usuarios con ese perfil
     */
    List<User> obtenerPorPerfil(Long perfilId);

    /**
     * Busca usuarios cuyo nombre contenga un texto
     * 
     * @param nombre Texto a buscar
     * @return Lista de usuarios encontrados
     */
    List<User> buscarPorNombre(String nombre);

    // ========================================
    // VALIDACIONES
    // ========================================

    /**
     * Verifica si existe un usuario con ese username
     * 
     * @param username Username a verificar
     * @return true si existe, false si no
     */
    boolean existePorUsername(String username);

    /**
     * Verifica si existe un usuario con ese email
     * 
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existePorEmail(String email);

    /**
     * Valida si un usuario puede iniciar sesión
     * 
     * @param username Username del usuario
     * @return true si puede iniciar sesión, false si no
     */
    boolean puedeIniciarSesion(String username);

    // ========================================
    // OPERACIONES ESPECIALES
    // ========================================

    /**
     * Crea un nuevo usuario con contraseña encriptada
     * 
     * @param user                 Usuario a crear
     * @param passwordSinEncriptar Contraseña en texto plano
     * @return Usuario creado
     */
    User crearUsuario(User user, String passwordSinEncriptar);

    /**
     * Actualiza los datos de un usuario
     * 
     * @param id              ID del usuario
     * @param userActualizado Datos actualizados
     * @return Usuario actualizado
     */
    User actualizar(Long id, User userActualizado);

    /**
     * Cambia la contraseña de un usuario
     * 
     * @param userId         ID del usuario
     * @param passwordActual Contraseña actual
     * @param passwordNueva  Nueva contraseña
     * @return true si se cambió correctamente, false si no
     */
    boolean cambiarPassword(Long userId, String passwordActual, String passwordNueva);

    /**
     * Activa o desactiva un usuario
     * 
     * @param id     ID del usuario
     * @param activo true para activar, false para desactivar
     * @return Usuario actualizado
     */
    User cambiarEstado(Long id, boolean activo);

    /**
     * Cuenta el total de usuarios activos
     * 
     * @return Cantidad de usuarios activos
     */
    long contarActivos();
}

/**
 * EXPLICACIÓN ADICIONAL PARA PRINCIPIANTES:
 * 
 * 1. ¿Qué es una interface?
 * - Es un "contrato" que define QUÉ se puede hacer
 * - NO define CÓMO se hace (eso va en la implementación)
 * - Es como un menú de restaurante: lista los platos, pero no la receta
 * 
 * 2. ¿Por qué Optional?
 * - Porque un usuario puede existir o no
 * - Es más seguro que devolver null
 * 
 * Uso:
 * ```java
 * Optional<User> resultado = userService.buscarPorId(1L);
 * 
 * if (resultado.isPresent()) {
 * User user = resultado.get();
 * // Hacer algo con el usuario
 * } else {
 * // El usuario no existe
 * }
 * ```
 * 
 * 3. ¿Qué métodos son más importantes?
 * - guardar() - Crear y actualizar usuarios
 * - buscarPorUsername() - Para el login
 * - crearUsuario() - Crea usuario con contraseña encriptada
 * - cambiarPassword() - Cambiar contraseña de forma segura
 * 
 * 4. ¿Dónde va la lógica?
 * - Aquí NO hay lógica, solo declaraciones
 * - La lógica va en UserServiceImpl
 */