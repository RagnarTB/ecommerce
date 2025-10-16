package com.miempresa.ecommerce.repositories;

import com.miempresa.ecommerce.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: USUARIO
 * 
 * Interface para acceder a la tabla 'usuarios' en la base de datos.
 * 
 * ¿Qué es JpaRepository?
 * - Es una interface de Spring que ya tiene métodos predefinidos
 * - No necesitas implementar nada, Spring lo hace automáticamente
 * - Ya incluye: save(), findById(), findAll(), delete(), etc.
 * 
 * Solo debes declarar los métodos adicionales que necesites.
 */

@Repository // Le dice a Spring que esto es un Repository
public interface UserRepository extends JpaRepository<User, Long> {
        // JpaRepository<User, Long>
        // User = La entidad con la que trabaja
        // Long = El tipo de dato del ID

        // ========================================
        // MÉTODOS QUE YA VIENEN CON JpaRepository
        // (No necesitas escribirlos, ya existen)
        // ========================================

        // save(user) - Guarda o actualiza un usuario
        // findById(id) - Busca por ID
        // findAll() - Obtiene todos los usuarios
        // deleteById(id) - Elimina por ID
        // count() - Cuenta cuántos usuarios hay
        // existsById(id) - Verifica si existe un ID

        // ========================================
        // MÉTODOS PERSONALIZADOS
        // ========================================

        /**
         * Busca un usuario por su username
         * 
         * Spring genera automáticamente la consulta:
         * SELECT * FROM usuarios WHERE username = ?
         * 
         * @param username Nombre de usuario
         * @return Optional con el usuario si existe, vacío si no
         */
        Optional<User> findByUsername(String username);

        /**
         * Busca un usuario por su email
         * 
         * SQL generado:
         * SELECT * FROM usuarios WHERE email = ?
         */
        Optional<User> findByEmail(String email);

        /**
         * Busca usuarios activos
         * 
         * SQL generado:
         * SELECT * FROM usuarios WHERE activo = true
         */
        List<User> findByActivoTrue();

        /**
         * Busca usuarios inactivos
         */
        List<User> findByActivoFalse();

        /**
         * Busca usuarios por perfil
         * 
         * SQL generado:
         * SELECT * FROM usuarios WHERE perfil_id = ?
         */
        List<User> findByPerfilId(Long perfilId);

        /**
         * Busca usuarios cuyo nombre contenga un texto
         * (búsqueda parcial, case-insensitive)
         * 
         * SQL generado:
         * SELECT * FROM usuarios WHERE LOWER(nombre) LIKE LOWER(?)
         */
        List<User> findByNombreContainingIgnoreCase(String nombre);

        /**
         * Verifica si existe un usuario con ese username
         * 
         * SQL generado:
         * SELECT COUNT(*) > 0 FROM usuarios WHERE username = ?
         */
        boolean existsByUsername(String username);

        /**
         * Verifica si existe un usuario con ese email
         */
        boolean existsByEmail(String email);

        /**
         * Busca usuarios por perfil y estado activo
         * 
         * SQL generado:
         * SELECT * FROM usuarios WHERE perfil_id = ? AND activo = true
         */
        List<User> findByPerfilIdAndActivoTrue(Long perfilId);

        /**
         * Busca usuario por username y que esté activo
         * Útil para el login
         */
        Optional<User> findByUsernameAndActivoTrue(String username);

        // ========================================
        // CONSULTAS PERSONALIZADAS CON @Query
        // ========================================

        /**
         * Busca usuarios con consulta JPQL personalizada
         * 
         * JPQL es similar a SQL pero usa nombres de clases y atributos
         * en lugar de tablas y columnas
         */
        @Query("SELECT u FROM User u WHERE u.perfil.nombre = :nombrePerfil AND u.activo = true")
        List<User> buscarPorNombrePerfil(@Param("nombrePerfil") String nombrePerfil);

        /**
         * Cuenta usuarios activos
         */
        @Query("SELECT COUNT(u) FROM User u WHERE u.activo = true")
        long contarUsuariosActivos();

        /**
         * Busca usuarios por múltiples criterios
         */
        @Query("SELECT u FROM User u WHERE " +
                        "(:nombre IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
                        "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
                        "(:activo IS NULL OR u.activo = :activo)")
        List<User> buscarConFiltros(
                        @Param("nombre") String nombre,
                        @Param("email") String email,
                        @Param("activo") Boolean activo);
}

/**
 * EXPLICACIÓN ADICIONAL PARA PRINCIPIANTES:
 * 
 * 1. ¿Por qué es una INTERFACE y no una CLASE?
 * - No necesitas implementarla
 * - Spring crea automáticamente la implementación en tiempo de ejecución
 * - Es "magia" de Spring Framework
 * 
 * 2. ¿Cómo funcionan los nombres de métodos?
 * Spring lee el nombre del método y genera el SQL automáticamente:
 * 
 * findBy + NombreAtributo + Operación
 * 
 * Ejemplos:
 * - findByUsername → WHERE username = ?
 * - findByActivoTrue → WHERE activo = true
 * - findByNombreContaining → WHERE nombre LIKE %?%
 * - findByEmailAndActivoTrue → WHERE email = ? AND activo = true
 * 
 * 3. ¿Qué es Optional?
 * - Es un contenedor que puede tener un valor o estar vacío
 * - Evita NullPointerException
 * 
 * Uso:
 * ```java
 * Optional<User> resultado = userRepository.findByUsername("admin");
 * 
 * if (resultado.isPresent()) {
 * User user = resultado.get();
 * System.out.println(user.getNombre());
 * } else {
 * System.out.println("Usuario no encontrado");
 * }
 * 
 * // O de forma corta:
 * User user = resultado.orElse(null);
 * User user = resultado.orElseThrow(() -> new Exception("No encontrado"));
 * ```
 * 
 * 4. ¿Qué es @Query?
 * - Permite escribir consultas personalizadas en JPQL
 * - JPQL es como SQL pero orientado a objetos
 * - Usas nombres de clases (User) en lugar de tablas (usuarios)
 * - Usas nombres de atributos (nombre) en lugar de columnas (nombre)
 * 
 * 5. ¿Cómo se usa un Repository en el código?
 * ```java
 * 
 * @Service
 *          public class UserService {
 * @Autowired
 *            private UserRepository userRepository;
 * 
 *            public User buscarPorId(Long id) {
 *            return userRepository.findById(id).orElse(null);
 *            }
 * 
 *            public List<User> buscarTodos() {
 *            return userRepository.findAll();
 *            }
 * 
 *            public User guardar(User user) {
 *            return userRepository.save(user);
 *            }
 *            }
 *            ```
 * 
 *            6. Métodos más comunes de JpaRepository:
 *            - save(entity) → INSERT o UPDATE
 *            - findById(id) → SELECT por ID
 *            - findAll() → SELECT todo
 *            - deleteById(id) → DELETE por ID
 *            - count() → COUNT(*)
 *            - existsById(id) → Verifica existencia
 */