
package com.miempresa.ecommerce.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.miempresa.ecommerce.models.Installment;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.security.UserDetailsImpl;
import com.miempresa.ecommerce.services.CreditService;

import lombok.extern.slf4j.Slf4j;

/**
 * CONTROLLER ADVICE: PERMISOS DE USUARIO
 * 
 * Agrega automáticamente los permisos del usuario autenticado
 * a todas las vistas de Thymeleaf.
 * 
 * Esto permite usar en las vistas:
 * th:if="${userPermisos.contains('MODULO_PRODUCTOS')}"
 */

@ControllerAdvice
@Slf4j
public class UserPermissionsControllerAdvice {
    @Autowired
    private CreditService creditService;

    /**
     * Agrega los permisos del usuario al modelo en cada petición
     * Disponible en todas las vistas como: ${userPermisos}
     */
    @ModelAttribute("userPermisos")
    public Set<String> addUserPermissions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Si no hay autenticación o es anónimo, retornar set vacío
            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication.getPrincipal().equals("anonymousUser")) {
                return new HashSet<>();
            }

            // Obtener el UserDetails
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                User user = userDetails.getUser();

                // Extraer los códigos de permisos del perfil del usuario
                Set<String> permisos = user.getPerfil().getPermisos()
                        .stream()
                        .map(permiso -> permiso.getCodigo())
                        .collect(Collectors.toSet());

                log.debug("Permisos cargados para usuario {}: {}", user.getUsername(), permisos);

                return permisos;
            }

            return new HashSet<>();

        } catch (Exception e) {
            log.error("Error al cargar permisos del usuario: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Agrega el usuario autenticado al modelo
     * Disponible en todas las vistas como: ${currentUser}
     */
    @ModelAttribute("currentUser")
    public User addCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication.getPrincipal().equals("anonymousUser")) {
                return null;
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                return userDetails.getUser();
            }

            return null;

        } catch (Exception e) {
            log.error("Error al cargar usuario actual: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Agrega el username del usuario autenticado al modelo
     * Disponible en todas las vistas como: ${username}
     */
    @ModelAttribute("username")
    public String addUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return "Invitado";
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                return userDetails.getUser().getUsername();
            }

            if (principal instanceof String) {
                return (String) principal;
            }

            return "Invitado";

        } catch (Exception e) {
            log.error("Error al cargar username: {}", e.getMessage());
            return "Invitado";
        }
    }

    @ModelAttribute("cuotasVencidas")
    public Long addCuotasVencidas() {
        try {
            // Assuming creditService is an injected instance of CreditService
            List<Installment> cuotas = creditService.obtenerCuotasVencidas();
            return (long) cuotas.size(); // Returns the count as Long
        } catch (Exception e) {
            log.warn("No se pudo cargar el contador de cuotas vencidas: {}", e.getMessage());
            return 0L;
        }
    }
}

/**
 * EXPLICACIÓN:
 * 
 * @ControllerAdvice es una anotación de Spring que permite:
 *                   - Agregar atributos globales a TODAS las vistas
 *                   - Manejar excepciones globalmente
 *                   - Realizar binding de datos
 * 
 *                   @ModelAttribute("userPermisos") crea un atributo llamado
 *                   "userPermisos"
 *                   que estará disponible en TODAS las plantillas Thymeleaf.
 * 
 *                   FLUJO:
 *                   1. Usuario hace login
 *                   2. Spring Security autentica y crea UserDetailsImpl
 *                   3. En cada petición, este ControllerAdvice se ejecuta
 *                   4. Extrae los permisos del usuario autenticado
 *                   5. Los agrega al modelo como "userPermisos"
 *                   6. Thymeleaf puede usar:
 *                   th:if="${userPermisos.contains('MODULO_PRODUCTOS')}"
 * 
 *                   VENTAJAS:
 *                   - No necesitas agregar userPermisos manualmente en cada
 *                   controller
 *                   - Todas las vistas tienen acceso automático
 *                   - Código DRY (Don't Repeat Yourself)
 * 
 *                   USO EN THYMELEAF:
 * 
 *                   <!-- Mostrar menú solo si tiene permiso -->
 *                   <li th:if="${userPermisos.contains('MODULO_PRODUCTOS')}">
 *                   <a href="/admin/productos">Productos</a>
 *                   </li>
 * 
 *                   <!-- Mostrar nombre del usuario -->
 *                   <span th:text="${currentUser.nombre}">Usuario</span>
 * 
 *                   <!-- Mostrar username -->
 *                   <span th:text="${username}">admin</span>
 */