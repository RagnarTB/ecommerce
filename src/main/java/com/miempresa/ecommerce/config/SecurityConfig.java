package com.miempresa.ecommerce.config;

import com.miempresa.ecommerce.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order; // Importar Order
import org.springframework.http.HttpMethod; // Importar HttpMethod
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Importar AbstractHttpConfigurer
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final UserDetailsServiceImpl userDetailsService;

        // PasswordEncoder (sin cambios)
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // Authentication Provider (sin cambios)
        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        // Authentication Manager (sin cambios)
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        // --- NUEVA CONFIGURACIÓN PARA API ---
        @Bean
        @Order(1) // Prioridad 1 para la API (se evalúa primero)
        public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/api/**") // Aplicar esta configuración SOLO a rutas /api/**
                                .authorizeHttpRequests(auth -> auth
                                                // Permitir POST a obtener-o-crear SIN autenticación
                                                .requestMatchers(HttpMethod.POST, "/api/clientes/obtener-o-crear")
                                                .permitAll()
                                                // Permitir GETs públicos a la API si los hubiera (ej. buscar productos)
                                                .requestMatchers(HttpMethod.GET, "/api/productos/**",
                                                                "/api/categorias/**", "/api/marcas/**")
                                                .permitAll()
                                                // Cualquier otra ruta API requiere autenticación (si es necesario)
                                                .anyRequest().authenticated())
                                .csrf(AbstractHttpConfigurer::disable) // Deshabilitar CSRF SOLO para /api/**
                                .httpBasic(AbstractHttpConfigurer::disable) // Deshabilitar login básico si no lo usas
                                                                            // para API
                                .formLogin(AbstractHttpConfigurer::disable); // Deshabilitar form login para API

                return http.build();
        }
        // --- FIN NUEVA CONFIGURACIÓN PARA API ---

        // --- CONFIGURACIÓN PRINCIPAL (Admin Panel y Web) ---
        @Bean
        @Order(2) // Prioridad 2 (se evalúa después de la API)
        public SecurityFilterChain mainFilterChain(HttpSecurity http) throws Exception {
                http
                                // Aplicar a todas las rutas EXCEPTO /api/** (ya manejadas arriba)
                                .securityMatcher("/**")
                                .authorizeHttpRequests(auth -> auth
                                                // Rutas públicas (Web y estáticos)
                                                .requestMatchers(
                                                                "/", "/home", "/catalogo/**", "/producto/**",
                                                                "/carrito/**", "/checkout/**", // Web pública
                                                                "/css/**", "/js/**", "/img/**", "/uploads/**",
                                                                "/webjars/**", "/adminlte/**", // Estáticos
                                                                "/login", "/logout", "/error" // Auth y error
                                                ).permitAll()

                                                // Rutas Admin (protegidas por permisos)
                                                .requestMatchers("/admin/dashboard").authenticated()
                                                .requestMatchers("/admin/productos/**", "/admin/categorias/**",
                                                                "/admin/marcas/**")
                                                .hasAuthority("MODULO_PRODUCTOS")
                                                .requestMatchers("/admin/ventas/**", "/admin/pedidos/**",
                                                                "/admin/pos/**", "/admin/creditos/**")
                                                .hasAuthority("MODULO_VENTAS") // Incluye /admin/ventas/pos/registrar
                                                .requestMatchers("/admin/clientes/**").hasAuthority("MODULO_CLIENTES")
                                                .requestMatchers("/admin/reportes/**").hasAuthority("MODULO_REPORTES")
                                                .requestMatchers("/admin/inventario/**")
                                                .hasAuthority("MODULO_INVENTARIO")
                                                .requestMatchers("/admin/usuarios/**", "/admin/perfiles/**")
                                                .hasAuthority("MODULO_USUARIOS")
                                                .requestMatchers("/admin/proveedores/**")
                                                .hasAuthority("MODULO_PROVEEDORES")
                                                .requestMatchers("/admin/configuracion/**")
                                                .hasAuthority("MODULO_CONFIGURACION")

                                                // Cualquier otra ruta (no API, no pública) requiere autenticación
                                                .anyRequest().authenticated())
                                .formLogin(form -> form // Configuración de login (sin cambios)
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/admin/dashboard", true)
                                                .failureUrl("/login?error=true")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .permitAll())
                                .logout(logout -> logout // Configuración de logout (sin cambios)
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                                .logoutSuccessUrl("/login?logout=true")
                                                .deleteCookies("JSESSIONID")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .permitAll())
                                .exceptionHandling(exception -> exception // Acceso denegado (sin cambios)
                                                .accessDeniedPage("/error/403"))
                                // CSRF HABILITADO para la app principal (Thymeleaf lo maneja)
                                // No es necesario deshabilitarlo aquí si usas formularios Thymeleaf
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")); // Ignorar CSRF para API, pero
                                                                                        // mantenerlo para el resto

                return http.build();
        }
}