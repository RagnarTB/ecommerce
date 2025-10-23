package com.miempresa.ecommerce.config;

import com.miempresa.ecommerce.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * SECURITY CONFIG
 * 
 * Configuración principal de Spring Security.
 * Define:
 * - Qué rutas son públicas y cuáles requieren login
 * - Cómo se encriptan las contraseñas
 * - Cómo funciona el login y logout
 * - Control de acceso por permisos
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final UserDetailsServiceImpl userDetailsService;

        // ========================================
        // CONFIGURACIÓN DE SEGURIDAD
        // ========================================

        /**
         * Configuración principal de seguridad
         * Define qué rutas están protegidas y cuáles son públicas
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Configuración de autorización de peticiones
                                .authorizeHttpRequests(auth -> auth
                                                // ========================================
                                                // RUTAS PÚBLICAS (sin login)
                                                // ========================================

                                                // Página principal y catálogo público
                                                .requestMatchers(
                                                                "/",
                                                                "/home",
                                                                "/catalogo/**",
                                                                "/producto/**",
                                                                "/carrito/**",
                                                                "/checkout/**")
                                                .permitAll()

                                                // Recursos estáticos (CSS, JS, imágenes)
                                                .requestMatchers(
                                                                "/css/**",
                                                                "/js/**",
                                                                "/img/**",
                                                                "/uploads/**",
                                                                "/webjars/**",
                                                                "/adminlte/**")
                                                .permitAll()

                                                // Login y registro
                                                .requestMatchers(
                                                                "/login",
                                                                "/logout",
                                                                "/error")
                                                .permitAll()

                                                // ========================================
                                                // RUTAS DEL ADMIN PANEL (requieren login)
                                                // ========================================

                                                // Dashboard principal
                                                .requestMatchers("/admin/dashboard").authenticated()

                                                // Módulo de productos (requiere permiso MODULO_PRODUCTOS)
                                                .requestMatchers("/admin/productos/**").hasAuthority("MODULO_PRODUCTOS")
                                                .requestMatchers("/admin/categorias/**")
                                                .hasAuthority("MODULO_PRODUCTOS")
                                                .requestMatchers("/admin/marcas/**").hasAuthority("MODULO_PRODUCTOS")

                                                // Módulo de ventas (requiere permiso MODULO_VENTAS)
                                                .requestMatchers("/admin/ventas/**").hasAuthority("MODULO_VENTAS")
                                                .requestMatchers("/admin/pedidos/**").hasAuthority("MODULO_VENTAS")
                                                .requestMatchers("/admin/pos/**").hasAuthority("MODULO_VENTAS")
                                                .requestMatchers("/admin/creditos/**").hasAuthority("MODULO_VENTAS")

                                                // Módulo de clientes (requiere permiso MODULO_CLIENTES)
                                                .requestMatchers("/admin/clientes/**").hasAuthority("MODULO_CLIENTES")

                                                // Módulo de reportes (requiere permiso MODULO_REPORTES)
                                                .requestMatchers("/admin/reportes/**").hasAuthority("MODULO_REPORTES")

                                                // Módulo de inventario (requiere permiso MODULO_INVENTARIO)
                                                .requestMatchers("/admin/inventario/**")
                                                .hasAuthority("MODULO_INVENTARIO")

                                                // Módulo de usuarios (SOLO ADMIN)
                                                .requestMatchers("/admin/usuarios/**").hasAuthority("MODULO_USUARIOS")
                                                .requestMatchers("/admin/perfiles/**").hasAuthority("MODULO_USUARIOS")

                                                // Módulo de proveedores (SOLO ADMIN)
                                                .requestMatchers("/admin/proveedores/**")
                                                .hasAuthority("MODULO_PROVEEDORES")

                                                // Módulo de configuración (SOLO ADMIN)
                                                .requestMatchers("/admin/configuracion/**")
                                                .hasAuthority("MODULO_CONFIGURACION")

                                                // Cualquier otra ruta requiere autenticación
                                                .anyRequest().authenticated())

                                // ========================================
                                // CONFIGURACIÓN DE LOGIN
                                // ========================================
                                .formLogin(form -> form
                                                .loginPage("/login") // Página personalizada de login
                                                .loginProcessingUrl("/login") // URL que procesa el login
                                                .defaultSuccessUrl("/admin/dashboard", true) // Redirige aquí tras login
                                                                                             // exitoso
                                                .failureUrl("/login?error=true") // Redirige aquí si falla el login
                                                .usernameParameter("username") // Nombre del campo username en el form
                                                .passwordParameter("password") // Nombre del campo password en el form
                                                .permitAll())

                                // ========================================
                                // CONFIGURACIÓN DE LOGOUT
                                // ========================================
                                .logout(logout -> logout
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                                .logoutSuccessUrl("/login?logout=true") // Redirige aquí tras logout
                                                .deleteCookies("JSESSIONID") // Elimina la cookie de sesión
                                                .invalidateHttpSession(true) // Invalida la sesión
                                                .clearAuthentication(true) // Limpia la autenticación
                                                .permitAll())

                                // ========================================
                                // CONFIGURACIÓN ADICIONAL
                                // ========================================

                                // Página de acceso denegado personalizada
                                .exceptionHandling(exception -> exception
                                                .accessDeniedPage("/error/403"))

                                // Configuración de sesiones
                                .sessionManagement(session -> session
                                                .maximumSessions(1) // Solo una sesión por usuario
                                                .maxSessionsPreventsLogin(false) // La nueva sesión cierra la anterior
                                );

                return http.build();
        }

        // ========================================
        // BEANS DE SEGURIDAD
        // ========================================

        /**
         * PasswordEncoder para encriptar contraseñas con BCrypt
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * Authentication Provider
         * Conecta el UserDetailsService con el PasswordEncoder
         */
        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        /**
         * Authentication Manager
         * Gestiona el proceso de autenticación
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
                        throws Exception {
                return authConfig.getAuthenticationManager();
        }
}

/**
 * EXPLICACIÓN DETALLADA:
 * 
 * 1. ¿Qué hace @Configuration?
 * - Le dice a Spring que esta clase tiene configuraciones
 * - Los métodos @Bean se ejecutan al iniciar
 * 
 * 2. ¿Qué hace @EnableWebSecurity?
 * - Activa Spring Security en la aplicación
 * - Sin esto, Security no funciona
 * 
 * 3. ¿Qué hace @EnableMethodSecurity?
 * - Permite usar anotaciones de seguridad en métodos
 * - Ejemplo: @PreAuthorize("hasAuthority('MODULO_PRODUCTOS')")
 * 
 * 4. Rutas públicas vs protegidas:
 * 
 * PÚBLICAS (cualquiera puede acceder):
 * - / (home)
 * - /catalogo/** (catálogo de productos)
 * - /producto/** (detalle de producto)
 * - /css/**, /js/**, /img/** (recursos estáticos)
 * - /login (página de login)
 * 
 * PROTEGIDAS (requieren login):
 * - /admin/** (todo el panel admin)
 * 
 * CON PERMISOS ESPECÍFICOS:
 * - /admin/productos/** → requiere MODULO_PRODUCTOS
 * - /admin/ventas/** → requiere MODULO_VENTAS
 * - /admin/usuarios/** → requiere MODULO_USUARIOS (solo admin)
 * 
 * 5. ¿Cómo funciona hasAuthority()?
 * 
 * Usuario ADMINISTRADOR intenta acceder a /admin/productos:
 * - Spring Security verifica: ¿Tiene "MODULO_PRODUCTOS"?
 * - Authorities del admin: [ROLE_ADMINISTRADOR, MODULO_PRODUCTOS, ...]
 * - SÍ tiene → Acceso permitido ✅
 * 
 * Usuario TRABAJADOR intenta acceder a /admin/usuarios:
 * - Spring Security verifica: ¿Tiene "MODULO_USUARIOS"?
 * - Authorities del trabajador: [ROLE_TRABAJADOR, MODULO_PRODUCTOS,
 * MODULO_VENTAS, ...]
 * - NO tiene → Acceso denegado ❌ → Redirige a /error/403
 * 
 * 6. Flujo completo del login:
 * 
 * 1. Usuario visita /admin/dashboard (protegido)
 * 2. No está autenticado → Redirige a /login
 * 3. Usuario completa formulario y envía
 * 4. POST a /login con username y password
 * 5. Spring Security llama UserDetailsServiceImpl.loadUserByUsername()
 * 6. Se verifica la contraseña con passwordEncoder.matches()
 * 7. Si coincide → Crea sesión y redirige a /admin/dashboard
 * 8. Si NO coincide → Redirige a /login?error=true
 * 
 * 7. ¿Qué es BCryptPasswordEncoder?
 * - Algoritmo de encriptación muy seguro
 * - Usa "salt" para mayor seguridad
 * - Misma contraseña genera hashes diferentes
 * 
 * Ejemplo:
 * Contraseña: "admin123"
 * Hash 1: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
 * Hash 2: $2a$10$xEEZKfAH6nMx8Cjiq5mLN.jk8EF9YsF8FHUkSQJfKLJn9EjX6hbqu
 * (¡Son diferentes pero ambos válidos!)
 * 
 * 8. Control de sesiones:
 * - maximumSessions(1): Solo una sesión activa por usuario
 * - Si el usuario hace login desde otra PC, cierra la sesión anterior
 * - Evita que compartan cuentas
 */