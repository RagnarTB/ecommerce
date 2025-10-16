package com.miempresa.ecommerce.security;

import com.miempresa.ecommerce.models.Permission;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * USER DETAILS SERVICE
 * 
 * Servicio que Spring Security usa para cargar los datos del usuario.
 * Se ejecuta automáticamente cuando alguien intenta hacer login.
 * 
 * UserDetailsService es una interface de Spring Security que DEBES implementar.
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
     * 4. Retornamos un UserDetails con los datos del usuario
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

        // Convertir nuestro User a UserDetails de Spring Security
        return buildUserDetails(user);
    }

    /**
     * Convierte nuestro modelo User a UserDetails de Spring Security
     */
    private UserDetails buildUserDetails(User user) {
        // Obtener los permisos del usuario
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);

        // Crear el objeto UserDetails que Spring Security entiende
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // Ya está encriptada
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getActivo())
                .build();
    }

    /**
     * Obtiene los permisos del usuario y los convierte a authorities de Spring
     * Security
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Agregar el perfil como authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getPerfil().getNombre()));

        // Agregar cada permiso como authority
        for (Permission permission : user.getPerfil().getPermisos()) {
            authorities.add(new SimpleGrantedAuthority(permission.getCodigo()));
        }

        log.debug("Usuario {} tiene {} authorities", user.getUsername(), authorities.size());

        return authorities;
    }
}

/**
 * EXPLICACIÓN DETALLADA:
 * 
 * 1. ¿Qué es UserDetails?
 * - Es una interface de Spring Security
 * - Representa los datos de un usuario autenticado
 * - Incluye: username, password, authorities, estado de la cuenta
 * 
 * 2. ¿Qué son las Authorities?
 * - Son los permisos/roles del usuario
 * - Spring Security los usa para controlar acceso
 * - Formato: "ROLE_ADMIN", "MODULO_PRODUCTOS", etc.
 * 
 * 3. Flujo completo del login:
 * 
 * Paso 1: Usuario ingresa credenciales
 * Username: admin
 * Password: admin123
 * 
 * Paso 2: Spring Security llama loadUserByUsername("admin")
 * 
 * Paso 3: Buscamos en BD
 * SELECT * FROM usuarios WHERE username = 'admin'
 * 
 * Paso 4: Usuario encontrado
 * - Username: admin
 * - Password: $2a$10$... (encriptada)
 * - Perfil: ADMINISTRADOR
 * - Permisos: MODULO_PRODUCTOS, MODULO_VENTAS, etc.
 * 
 * Paso 5: Convertimos a UserDetails
 * UserDetails {
 * username: "admin",
 * password: "$2a$10$...",
 * authorities: [
 * "ROLE_ADMINISTRADOR",
 * "MODULO_PRODUCTOS",
 * "MODULO_VENTAS",
 * ...
 * ]
 * }
 * 
 * Paso 6: Spring Security compara contraseñas
 * passwordEncoder.matches("admin123", "$2a$10$...")
 * 
 * Paso 7: Si coincide → Login exitoso
 * Usuario autenticado y sesión creada
 * 
 * 4. ¿Por qué "ROLE_" en el perfil?
 * - Spring Security espera que los roles empiecen con "ROLE_"
 * - ROLE_ADMINISTRADOR, ROLE_TRABAJADOR
 * - Los permisos NO necesitan el prefijo
 * 
 * 5. Ejemplo de authorities generadas:
 * 
 * Usuario con perfil ADMINISTRADOR:
 * [
 * "ROLE_ADMINISTRADOR",
 * "MODULO_PRODUCTOS",
 * "MODULO_VENTAS",
 * "MODULO_CLIENTES",
 * "MODULO_REPORTES",
 * "MODULO_USUARIOS",
 * "MODULO_PROVEEDORES",
 * "MODULO_CONFIGURACION",
 * "MODULO_INVENTARIO"
 * ]
 * 
 * Usuario con perfil TRABAJADOR:
 * [
 * "ROLE_TRABAJADOR",
 * "MODULO_PRODUCTOS",
 * "MODULO_VENTAS",
 * "MODULO_CLIENTES",
 * "MODULO_REPORTES"
 * ]
 */