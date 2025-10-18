package com.miempresa.ecommerce.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.miempresa.ecommerce.models.Permission;
import com.miempresa.ecommerce.models.User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * USER DETAILS IMPLEMENTATION
 * 
 * Implementación personalizada de UserDetails que envuelve nuestro modelo User.
 * Permite acceder al objeto User completo desde el contexto de seguridad.
 * 
 * IMPORTANTE: Este wrapper es necesario para poder acceder a todos los
 * datos del usuario en los controllers y vistas, no solo username/password.
 */

@RequiredArgsConstructor
@Getter
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Agregar el perfil como rol
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getPerfil().getNombre()));

        // Agregar cada permiso como authority
        for (Permission permission : user.getPerfil().getPermisos()) {
            authorities.add(new SimpleGrantedAuthority(permission.getCodigo()));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getActivo();
    }

    /**
     * Método de conveniencia para obtener el objeto User completo
     */
    public User getUser() {
        return user;
    }
}

/**
 * VENTAJAS DE ESTE WRAPPER:
 * 
 * 1. Acceso al objeto User completo:
 * UserDetailsImpl userDetails = (UserDetailsImpl)
 * authentication.getPrincipal();
 * User user = userDetails.getUser();
 * String nombre = user.getNombre();
 * String email = user.getEmail();
 * 
 * 2. Compatible con Spring Security:
 * Implementa UserDetails, así que Spring Security lo acepta
 * 
 * 3. Acceso a permisos:
 * Set<Permission> permisos = user.getPerfil().getPermisos();
 * 
 * 4. Uso en Thymeleaf:
 * ${currentUser.nombre}
 * ${currentUser.email}
 * ${currentUser.perfil.nombre}
 */