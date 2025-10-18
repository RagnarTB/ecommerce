package com.miempresa.ecommerce.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.miempresa.ecommerce.models.User;

/**
 * SECURITY UTILS
 * 
 * Utilidades para obtener información del usuario autenticado.
 * Se usa en los controllers y services para saber quién está logueado.
 * 
 * ✅ MEJORADO: Ahora incluye métodos para obtener el objeto User completo
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

        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getUsername();
        }

        return principal.toString();
    }

    /**
     * ✅ NUEVO: Obtiene el objeto User completo del usuario autenticado
     * 
     * @return User o null si no hay nadie autenticado
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getUser();
        }

        return null;
    }

    /**
     * ✅ NUEVO: Obtiene el ID del usuario autenticado
     * 
     * @return ID del usuario o null
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * ✅ NUEVO: Obtiene el nombre completo del usuario autenticado
     * 
     * @return Nombre completo o null
     */
    public static String getCurrentUserFullName() {
        User user = getCurrentUser();
        if (user != null) {
            return user.getNombre() + " " + user.getApellido();
        }
        return null;
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

    /**
     * ✅ NUEVO: Verifica si el usuario es administrador
     * 
     * @return true si es administrador
     */
    public static boolean isAdmin() {
        return hasRole("ADMINISTRADOR");
    }

    /**
     * ✅ NUEVO: Verifica si el usuario puede acceder a un módulo
     * 
     * @param modulo Nombre del módulo (ej: "PRODUCTOS")
     * @return true si tiene acceso
     */
    public static boolean canAccessModule(String modulo) {
        return hasAuthority("MODULO_" + modulo);
    }
}

/**
 * ✅ NUEVOS MÉTODOS - USO EN LOS CONTROLLERS:
 * 
 * // Obtener el objeto User completo
 * User currentUser = SecurityUtils.getCurrentUser();
 * String nombre = currentUser.getNombre();
 * String email = currentUser.getEmail();
 * 
 * // Obtener ID del usuario
 * Long userId = SecurityUtils.getCurrentUserId();
 * 
 * // Obtener nombre completo
 * String fullName = SecurityUtils.getCurrentUserFullName();
 * 
 * // Verificar si es admin
 * if (SecurityUtils.isAdmin()) {
 * // Código solo para administradores
 * }
 * 
 * // Verificar acceso a módulo
 * if (SecurityUtils.canAccessModule("PRODUCTOS")) {
 * // Puede gestionar productos
 * }
 * 
 * EJEMPLO EN UN CONTROLLER:
 * 
 * @PostMapping("/productos/guardar")
 * public String guardar(@Valid Product producto) {
 * // Obtener usuario que está guardando el producto
 * User currentUser = SecurityUtils.getCurrentUser();
 * 
 * producto.setCreadoPor(currentUser);
 * productService.guardar(producto);
 * 
 * return "redirect:/admin/productos";
 * }
 */