package com.miempresa.ecommerce.repositories;

import com.miempresa.ecommerce.models.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: PERFIL
 * 
 * Interface para acceder a la tabla 'perfiles' (roles de usuario).
 */

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /**
     * Busca un perfil por su nombre
     * Ejemplo: "ADMINISTRADOR", "TRABAJADOR"
     */
    Optional<Profile> findByNombre(String nombre);

    /**
     * Busca perfiles activos
     */
    List<Profile> findByActivoTrue();

    /**
     * Verifica si existe un perfil con ese nombre
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca perfiles cuyo nombre contenga un texto
     */
    List<Profile> findByNombreContainingIgnoreCase(String nombre);
}