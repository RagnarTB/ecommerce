package com.miempresa.ecommerce.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.User;

/**
 * REPOSITORY: USUARIO
 * 
 * Interface para acceder a la tabla 'usuarios'.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        /**
         * Busca un usuario por su username.
         */
        Optional<User> findByUsername(String username);

        /**
         * Busca un usuario por su email.
         */
        Optional<User> findByEmail(String email);

        /**
         * Busca usuarios activos.
         */
        List<User> findByActivoTrue();

        /**
         * Busca usuarios inactivos.
         */
        List<User> findByActivoFalse();

        /**
         * Busca usuarios por perfil.
         */
        List<User> findByPerfilId(Long perfilId);

        /**
         * Busca usuarios cuyo nombre contenga un texto.
         */
        List<User> findByNombreContainingIgnoreCase(String nombre);

        /**
         * Verifica si existe un usuario con ese username.
         */
        boolean existsByUsername(String username);

        /**
         * Verifica si existe un usuario con ese email.
         */
        boolean existsByEmail(String email);

        /**
         * Busca usuarios por perfil y estado activo.
         */
        List<User> findByPerfilIdAndActivoTrue(Long perfilId);

        /**
         * Busca usuario por username y que esté activo.
         * Útil para el login.
         */
        Optional<User> findByUsernameAndActivoTrue(String username);

        /**
         * Busca usuarios activos por nombre de perfil.
         */
        @Query("""
                        SELECT u FROM User u
                        WHERE u.perfil.nombre = :nombrePerfil
                        AND u.activo = true
                        """)
        List<User> buscarPorNombrePerfil(@Param("nombrePerfil") String nombrePerfil);

        /**
         * Cuenta usuarios activos.
         */
        @Query("SELECT COUNT(u) FROM User u WHERE u.activo = true")
        long contarUsuariosActivos();

        /**
         * Busca usuarios con filtros múltiples.
         */
        @Query("""
                        SELECT u FROM User u
                        WHERE (:nombre IS NULL OR LOWER(TRIM(u.nombre)) LIKE LOWER(CONCAT('%', TRIM(:nombre), '%')))
                        AND (:email IS NULL OR LOWER(TRIM(u.email)) LIKE LOWER(CONCAT('%', TRIM(:email), '%')))
                        AND (:activo IS NULL OR u.activo = :activo)
                        ORDER BY u.nombre ASC
                        """)
        List<User> buscarConFiltros(
                        @Param("nombre") String nombre,
                        @Param("email") String email,
                        @Param("activo") Boolean activo);
}
