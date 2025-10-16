package com.miempresa.ecommerce.services;

import com.miempresa.ecommerce.models.Permission;
import com.miempresa.ecommerce.models.Profile;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * SERVICE INTERFACE: PERFIL (ROL)
 * 
 * Define operaciones para gestionar perfiles de usuario.
 */
public interface ProfileService {

    /**
     * Guarda un perfil
     */
    Profile guardar(Profile profile);

    /**
     * Busca un perfil por ID
     */
    Optional<Profile> buscarPorId(Long id);

    /**
     * Obtiene todos los perfiles
     */
    List<Profile> obtenerTodos();

    /**
     * Busca un perfil por nombre
     */
    Optional<Profile> buscarPorNombre(String nombre);

    /**
     * Obtiene perfiles activos
     */
    List<Profile> obtenerActivos();

    /**
     * Elimina un perfil (lógicamente)
     */
    void eliminar(Long id);

    /**
     * Verifica si existe un perfil con ese nombre
     */
    boolean existePorNombre(String nombre);

    /**
     * Asigna permisos a un perfil
     */
    Profile asignarPermisos(Long perfilId, Set<Long> permisosIds);

    /**
     * Actualiza un perfil
     */
    Profile actualizar(Long id, Profile profileActualizado);

    /**
     * Cambia el estado de un perfil
     */
    Profile cambiarEstado(Long id, boolean activo);
}