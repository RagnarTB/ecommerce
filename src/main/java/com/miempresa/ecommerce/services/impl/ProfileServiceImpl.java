package com.miempresa.ecommerce.services.impl;

import com.miempresa.ecommerce.models.Permission;
import com.miempresa.ecommerce.models.Profile;
import com.miempresa.ecommerce.repositories.PermissionRepository;
import com.miempresa.ecommerce.repositories.ProfileRepository;
import com.miempresa.ecommerce.services.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * SERVICE IMPLEMENTATION: PERFIL
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public Profile guardar(Profile profile) {
        log.info("Guardando perfil: {}", profile.getNombre());

        // Validar nombre único
        if (profile.getId() == null && existePorNombre(profile.getNombre())) {
            throw new RuntimeException("Ya existe un perfil con ese nombre");
        }

        return profileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Profile> buscarPorId(Long id) {
        return profileRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profile> obtenerTodos() {
        return profileRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Profile> buscarPorNombre(String nombre) {
        return profileRepository.findByNombre(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profile> obtenerActivos() {
        return profileRepository.findByActivoTrue();
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando perfil ID: {}", id);

        Optional<Profile> profileOpt = buscarPorId(id);

        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            profile.setActivo(false);
            profileRepository.save(profile);
        } else {
            throw new RuntimeException("Perfil no encontrado");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return profileRepository.existsByNombre(nombre);
    }

    @Override
    public Profile asignarPermisos(Long perfilId, Set<Long> permisosIds) {
        log.info("Asignando {} permisos al perfil ID: {}", permisosIds.size(), perfilId);

        Optional<Profile> profileOpt = buscarPorId(perfilId);

        if (profileOpt.isEmpty()) {
            throw new RuntimeException("Perfil no encontrado");
        }

        Profile profile = profileOpt.get();

        // Limpiar permisos actuales
        profile.limpiarPermisos();

        // Asignar nuevos permisos
        for (Long permisoId : permisosIds) {
            Optional<Permission> permisoOpt = permissionRepository.findById(permisoId);
            permisoOpt.ifPresent(profile::agregarPermiso);
        }

        return profileRepository.save(profile);
    }

    @Override
    public Profile actualizar(Long id, Profile profileActualizado) {
        log.info("Actualizando perfil ID: {}", id);

        Optional<Profile> profileOpt = buscarPorId(id);

        if (profileOpt.isEmpty()) {
            throw new RuntimeException("Perfil no encontrado");
        }

        Profile profile = profileOpt.get();
        profile.setNombre(profileActualizado.getNombre());
        profile.setDescripcion(profileActualizado.getDescripcion());

        return profileRepository.save(profile);
    }

    @Override
    public Profile cambiarEstado(Long id, boolean activo) {
        Optional<Profile> profileOpt = buscarPorId(id);

        if (profileOpt.isEmpty()) {
            throw new RuntimeException("Perfil no encontrado");
        }

        Profile profile = profileOpt.get();
        profile.setActivo(activo);

        return profileRepository.save(profile);
    }
}