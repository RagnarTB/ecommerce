package com.miempresa.ecommerce.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * USER DETAILS SERVICE
 * 
 * Servicio que Spring Security usa para cargar los datos del usuario.
 * Se ejecuta automáticamente cuando alguien intenta hacer login.
 * 
 * CAMBIO IMPORTANTE: Ahora retorna UserDetailsImpl personalizado
 * en lugar del UserDetails estándar de Spring Security.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Método principal que Spring Security llama al hacer login
     * 
     * Flujo:
     * 1. Usuario ingresa username y password
     * 2. Spring Security llama a este método con el username
     * 3. Buscamos el usuario en la base de datos
     * 4. Retornamos un UserDetailsImpl con el User completo
     * 5. Spring Security compara las contraseñas
     * 6. Si coinciden, login exitoso
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Intentando cargar usuario: {}", username);

        // Buscar usuario en la base de datos
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        // Verificar que el usuario esté activo
        if (!user.getActivo()) {
            log.error("Usuario inactivo: {}", username);
            throw new UsernameNotFoundException("Usuario inactivo");
        }

        log.info("Usuario cargado exitosamente: {}", username);

        // ✅ CAMBIO: Retornar UserDetailsImpl personalizado
        // Esto permite acceder al objeto User completo desde cualquier parte
        return new UserDetailsImpl(user);
    }
}

/**
 * EXPLICACIÓN DEL CAMBIO:
 * 
 * ANTES:
 * return org.springframework.security.core.userdetails.User.builder()
 * .username(user.getUsername())
 * .password(user.getPassword())
 * .authorities(authorities)
 * .build();
 * 
 * Problema: Solo tienes acceso a username, password y authorities
 * No puedes acceder a nombre, email, perfil, etc.
 * 
 * DESPUÉS:
 * return new UserDetailsImpl(user);
 * 
 * Ventaja: Tienes acceso al objeto User COMPLETO
 * Puedes obtener: nombre, email, perfil, permisos, etc.
 * 
 * USO:
 * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 * UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
 * User user = userDetails.getUser();
 * String nombre = user.getNombre();
 * String email = user.getEmail();
 */