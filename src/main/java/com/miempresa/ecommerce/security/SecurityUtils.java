package com.miempresa.ecommerce.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * SECURITY UTILS
 * 
 * Utilidades para obtener información del usuario autenticado.
 * Se usa en los controllers y services para saber quién está logueado.
 */
public class SecurityUtils {

    /**
     * Obtiene el username del usuario actualmente autenticado
     * 
     * @return Username o null si no hay nadie autenticado
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        return principal.toString();
    }

    /**
     * Verifica si hay un usuario autenticado
     * 
     * @return true si hay usuario autenticado, false si no
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String);
    }

    /**
     * Obtiene el objeto Authentication completo
     * 
     * @return Authentication o null
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Verifica si el usuario tiene un permiso específico
     * 
     * @param authority Código del permiso (ej: "MODULO_PRODUCTOS")
     * @return true si tiene el permiso, false si no
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    /**
     * Verifica si el usuario tiene un rol específico
     * 
     * @param role Nombre del rol (ej: "ADMINISTRADOR")
     * @return true si tiene el rol, false si no
     */
    public static boolean hasRole(String role) {
        return hasAuthority("ROLE_" + role);
    }
}

/**
 * USO EN LOS CONTROLLERS:
 * 
 * // Obtener el username del usuario logueado
 * String username = SecurityUtils.getCurrentUsername();
 * System.out.println("Usuario actual: " + username);
 * 
 * // Verificar si está autenticado
 * if (SecurityUtils.isAuthenticated()) {
 * // Usuario logueado
 * }
 * 
 * // Verificar si tiene un permiso
 * if (SecurityUtils.hasAuthority("MODULO_PRODUCTOS")) {
 * // Puede gestionar productos
 * }
 * 
 * // Verificar si es administrador
 * if (SecurityUtils.hasRole("ADMINISTRADOR")) {
 * // Es admin
 * }
 * 
 * USO EN THYMELEAF:
 * 
 * <!-- Mostrar solo si está autenticado -->
 * <div sec:authorize="isAuthenticated()">
 * Bienvenido <span sec:authentication="name"></span>
 * </div>
 * 
 * <!-- Mostrar solo si tiene el permiso -->
 * <div sec:authorize="hasAuthority('MODULO_PRODUCTOS')">
 * <a href="/admin/productos">Productos</a>
 * </div>
 * 
 * <!-- Mostrar solo si es admin -->
 * <div sec:authorize="hasRole('ADMINISTRADOR')">
 * <a href="/admin/usuarios">Usuarios</a>
 * </div>
 */