package com.miempresa.ecommerce.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CONTROLLER: AUTENTICACIÓN
 * 
 * Maneja las páginas de login y logout.
 * 
 * Anotaciones importantes:
 * 
 * @Controller - Le dice a Spring que esto es un controlador
 * @GetMapping - Maneja peticiones GET
 * @PostMapping - Maneja peticiones POST
 */

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    /**
     * Página de login
     * 
     * URL: GET /login
     * Vista: templates/login.html
     */
    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        log.debug("Accediendo a página de login");

        // Si hay error de login
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            log.warn("Intento de login fallido");
        }

        // Si viene de logout
        if (logout != null) {
            model.addAttribute("mensaje", "Sesión cerrada correctamente");
            log.info("Usuario cerró sesión");
        }

        // Retorna el nombre de la vista (login.html)
        return "login";
    }
}

/**
 * EXPLICACIÓN DETALLADA:
 * 
 * 1. ¿Qué hace @Controller?
 * - Marca la clase como controlador
 * - Spring la detecta automáticamente
 * - Puede manejar peticiones HTTP
 * 
 * 2. ¿Qué hace @GetMapping("/login")?
 * - Cuando alguien visita http://localhost:8080/login
 * - Se ejecuta el método mostrarLogin()
 * - GET = obtener/ver (no modifica datos)
 * 
 * 3. ¿Qué es Model?
 * - Objeto que pasa datos a la vista
 * - model.addAttribute("nombre", valor)
 * - En la vista accedes con ${nombre}
 * 
 * 4. ¿Qué es @RequestParam?
 * - Lee parámetros de la URL
 * - /login?error=true → error = "true"
 * - required = false → el parámetro es opcional
 * 
 * 5. ¿Qué retorna el método?
 * - Retorna un String con el nombre de la vista
 * - "login" → busca templates/login.html
 * - Spring + Thymeleaf renderizan el HTML
 * 
 * 6. Flujo completo:
 * 
 * Usuario visita: http://localhost:8080/login
 * ↓
 * Spring ejecuta: AuthController.mostrarLogin()
 * ↓
 * Método retorna: "login"
 * ↓
 * Thymeleaf busca: src/main/resources/templates/login.html
 * ↓
 * Renderiza HTML con los datos del model
 * ↓
 * Browser muestra la página
 * 
 * 7. Ejemplo con error:
 * 
 * URL: /login?error=true
 * ↓
 * 
 * @RequestParam error = "true"
 *               ↓
 *               model.addAttribute("error", "Usuario o contraseña incorrectos")
 *               ↓
 *               En login.html:
 *               <div th:if="${error}" th:text="${error}"></div>
 *               ↓
 *               Se muestra: "Usuario o contraseña incorrectos"
 */