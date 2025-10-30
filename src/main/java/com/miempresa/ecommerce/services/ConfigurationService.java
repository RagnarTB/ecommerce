package com.miempresa.ecommerce.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miempresa.ecommerce.models.Configuration;
import com.miempresa.ecommerce.repositories.ConfigurationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SERVICE: CONFIGURACIÓN - GAMER ZONE
 * 
 * Gestiona las configuraciones del sistema temática gaming
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ConfigurationService {

        private final ConfigurationRepository configurationRepository;

        // ========================================
        // OBTENER CONFIGURACIONES
        // ========================================

        @Transactional(readOnly = true)
        public String obtenerValor(String clave) {
                Optional<Configuration> config = configurationRepository.findByClave(clave);
                return config.map(Configuration::getValor).orElse(null);
        }

        @Transactional(readOnly = true)
        public String obtenerValor(String clave, String valorPorDefecto) {
                String valor = obtenerValor(clave);
                return valor != null ? valor : valorPorDefecto;
        }

        /**
         * Backwards-compatible alias for code that previously used `obtener(String)`.
         */
        @Transactional(readOnly = true)
        public String obtener(String clave) {
                return obtenerValor(clave);
        }

        /**
         * Backwards-compatible alias for code that previously used `obtener(String,
         * String)`.
         */
        @Transactional(readOnly = true)
        public String obtener(String clave, String valorPorDefecto) {
                return obtenerValor(clave, valorPorDefecto);
        }

        @Transactional(readOnly = true)
        public Map<String, String> obtenerTodasComoMapa() {
                List<Configuration> configs = configurationRepository.findAll();
                Map<String, String> mapa = new HashMap<>();

                for (Configuration config : configs) {
                        mapa.put(config.getClave(), config.getValor());
                }

                return mapa;
        }

        @Transactional(readOnly = true)
        public List<Configuration> obtenerPorCategoria(String categoria) {
                return configurationRepository.findByCategoria(categoria);
        }

        // ========================================
        // GUARDAR/ACTUALIZAR
        // ========================================

        public Configuration guardar(String clave, String valor) {
                return guardar(clave, valor, "TEXT", null, null);
        }

        public Configuration guardar(String clave, String valor, String tipo,
                        String descripcion, String categoria) {
                log.info("Guardando configuración: {} = {}", clave, valor);

                Optional<Configuration> existente = configurationRepository.findByClave(clave);

                Configuration config;

                if (existente.isPresent()) {
                        config = existente.get();
                        config.setValor(valor);

                        if (tipo != null)
                                config.setTipo(tipo);
                        if (descripcion != null)
                                config.setDescripcion(descripcion);
                        if (categoria != null)
                                config.setCategoria(categoria);
                } else {
                        config = Configuration.builder()
                                        .clave(clave)
                                        .valor(valor)
                                        .tipo(tipo != null ? tipo : "TEXT")
                                        .descripcion(descripcion)
                                        .categoria(categoria)
                                        .build();
                }

                return configurationRepository.save(config);
        }

        public void guardarMultiples(Map<String, String> configuraciones) {
                for (Map.Entry<String, String> entry : configuraciones.entrySet()) {
                        guardar(entry.getKey(), entry.getValue());
                }
        }

        // ========================================
        // INICIALIZACIÓN - TEMA GAMER
        // ========================================

        public void inicializarConfiguraciones() {
                log.info("Inicializando configuraciones - GAMER ZONE");

                // ========================================
                // GENERAL
                // ========================================
                crearSiNoExiste("nombre_empresa", "GAMER ZONE",
                                "TEXT", "Nombre de la tienda", "GENERAL");

                crearSiNoExiste("texto_bienvenida",
                                "¡Bienvenido a Gamer Zone! Tu tienda favorita de videojuegos, funkos, sillas gaming y más. ¡Nivel up tu experiencia gamer!",
                                "TEXT", "Texto de bienvenida", "GENERAL");

                crearSiNoExiste("horario_atencion",
                                "Lun-Vie 10:00am-8:00pm, Sáb-Dom 10:00am-6:00pm",
                                "TEXT", "Horario de atención", "GENERAL");

                // ========================================
                // CONTACTO
                // ========================================
                crearSiNoExiste("telefono", "+51 999 888 777",
                                "TEXT", "Teléfono de contacto", "CONTACTO");

                crearSiNoExiste("email", "ventas@gamerzone.pe",
                                "TEXT", "Email de contacto", "CONTACTO");

                crearSiNoExiste("direccion", "Av. Larco 1234, Miraflores, Lima",
                                "TEXT", "Dirección física", "CONTACTO");

                crearSiNoExiste("whatsapp", "51999888777",
                                "TEXT", "WhatsApp", "CONTACTO");

                crearSiNoExiste("facebook_url", "https://facebook.com/gamerzone",
                                "TEXT", "URL de Facebook", "CONTACTO");

                crearSiNoExiste("instagram_url", "https://instagram.com/gamerzone",
                                "TEXT", "URL de Instagram", "CONTACTO");

                crearSiNoExiste("tiktok_url", "https://tiktok.com/@gamerzone",
                                "TEXT", "URL de TikTok", "CONTACTO");

                // ========================================
                // BRANDING - TEMA GAMER
                // ========================================
                crearSiNoExiste("logo", "logo-gamerzone.png",
                                "IMAGE", "Logo de la empresa", "BRANDING");

                // Colores neón gaming
                crearSiNoExiste("color_primario", "#FF0080",
                                "COLOR", "Color primario (rosa neón)", "BRANDING");

                crearSiNoExiste("color_secundario", "#00F0FF",
                                "COLOR", "Color secundario (cyan neón)", "BRANDING");

                crearSiNoExiste("color_acento", "#7B2FFF",
                                "COLOR", "Color de acento (púrpura)", "BRANDING");

                crearSiNoExiste("color_fondo", "#0A0E27",
                                "COLOR", "Color de fondo (azul oscuro)", "BRANDING");

                crearSiNoExiste("color_texto", "#FFFFFF",
                                "COLOR", "Color de texto principal", "BRANDING");

                // ========================================
                // SLIDER - BANNERS GAMING
                // ========================================
                crearSiNoExiste("slider_1_imagen", "slider/banner-gaming-1.jpg",
                                "IMAGE", "Banner PS5", "SLIDER");
                crearSiNoExiste("slider_1_titulo", "¡NUEVOS JUEGOS PS5!",
                                "TEXT", "Título slider 1", "SLIDER");
                crearSiNoExiste("slider_1_subtitulo", "Los últimos lanzamientos al mejor precio",
                                "TEXT", "Subtítulo slider 1", "SLIDER");
                crearSiNoExiste("slider_1_url", "/catalogo?categoria=1",
                                "TEXT", "URL slider 1", "SLIDER");

                crearSiNoExiste("slider_2_imagen", "slider/banner-gaming-2.jpg",
                                "IMAGE", "Banner Funkos", "SLIDER");
                crearSiNoExiste("slider_2_titulo", "FUNKOS EXCLUSIVOS",
                                "TEXT", "Título slider 2", "SLIDER");
                crearSiNoExiste("slider_2_subtitulo", "Colecciona tus personajes favoritos",
                                "TEXT", "Subtítulo slider 2", "SLIDER");
                crearSiNoExiste("slider_2_url", "/catalogo?categoria=3",
                                "TEXT", "URL slider 2", "SLIDER");

                crearSiNoExiste("slider_3_imagen", "slider/banner-gaming-3.jpg",
                                "IMAGE", "Banner Sillas Gaming", "SLIDER");
                crearSiNoExiste("slider_3_titulo", "SILLAS GAMING PREMIUM",
                                "TEXT", "Título slider 3", "SLIDER");
                crearSiNoExiste("slider_3_subtitulo", "Juega con el máximo confort",
                                "TEXT", "Subtítulo slider 3", "SLIDER");
                crearSiNoExiste("slider_3_url", "/catalogo?categoria=2",
                                "TEXT", "URL slider 3", "SLIDER");

                // ========================================
                // SECCIONES DESTACADAS
                // ========================================
                crearSiNoExiste("seccion_1_titulo", "🎮 VIDEOJUEGOS",
                                "TEXT", "Título sección 1", "SECCIONES");
                crearSiNoExiste("seccion_1_descripcion", "Los mejores títulos para PS5, Xbox y Switch",
                                "TEXT", "Descripción sección 1", "SECCIONES");
                crearSiNoExiste("seccion_1_icono", "fas fa-gamepad",
                                "TEXT", "Ícono sección 1", "SECCIONES");

                crearSiNoExiste("seccion_2_titulo", "🪑 SILLAS GAMING",
                                "TEXT", "Título sección 2", "SECCIONES");
                crearSiNoExiste("seccion_2_descripcion", "Ergonomía y estilo para tus sesiones épicas",
                                "TEXT", "Descripción sección 2", "SECCIONES");
                crearSiNoExiste("seccion_2_icono", "fas fa-chair",
                                "TEXT", "Ícono sección 2", "SECCIONES");

                crearSiNoExiste("seccion_3_titulo", "🎭 FUNKOS",
                                "TEXT", "Título sección 3", "SECCIONES");
                crearSiNoExiste("seccion_3_descripcion", "Figuras coleccionables de tus personajes favoritos",
                                "TEXT", "Descripción sección 3", "SECCIONES");
                crearSiNoExiste("seccion_3_icono", "fas fa-box-open",
                                "TEXT", "Ícono sección 3", "SECCIONES");

                crearSiNoExiste("seccion_4_titulo", "⌨️ PERIFÉRICOS",
                                "TEXT", "Título sección 4", "SECCIONES");
                crearSiNoExiste("seccion_4_descripcion", "Teclados, mouses y audífonos RGB premium",
                                "TEXT", "Descripción sección 4", "SECCIONES");
                crearSiNoExiste("seccion_4_icono", "fas fa-keyboard",
                                "TEXT", "Ícono sección 4", "SECCIONES");

                // ========================================
                // SEO
                // ========================================
                crearSiNoExiste("meta_title", "Gamer Zone - Tienda Gaming #1 en Perú",
                                "TEXT", "Título SEO", "SEO");

                crearSiNoExiste("meta_description",
                                "Compra videojuegos, funkos, sillas gaming y periféricos RGB. Envíos a todo Perú. Los mejores precios en gaming.",
                                "TEXT", "Descripción SEO", "SEO");

                crearSiNoExiste("meta_keywords",
                                "videojuegos, funkos, sillas gaming, periféricos rgb, ps5, xbox, nintendo switch, gaming peru",
                                "TEXT", "Keywords SEO", "SEO");

                // ========================================
                // REDES SOCIALES
                // ========================================
                crearSiNoExiste("twitter_url", "https://twitter.com/gamerzone",
                                "TEXT", "URL de Twitter", "REDES_SOCIALES");

                crearSiNoExiste("discord_url", "https://discord.gg/gamerzone",
                                "TEXT", "URL de Discord", "REDES_SOCIALES");

                crearSiNoExiste("twitch_url", "https://twitch.tv/gamerzone",
                                "TEXT", "URL de Twitch", "REDES_SOCIALES");

                // ========================================
                // CONFIGURACIÓN DE ENVÍOS
                // ========================================
                crearSiNoExiste("envio_gratis_desde", "200.00",
                                "DECIMAL", "Monto mínimo para envío gratis", "ENVIOS");

                crearSiNoExiste("costo_envio_lima", "15.00",
                                "DECIMAL", "Costo de envío en Lima", "ENVIOS");

                crearSiNoExiste("costo_envio_provincia", "25.00",
                                "DECIMAL", "Costo de envío en provincia", "ENVIOS");

                log.info("✓ Configuraciones GAMER ZONE inicializadas correctamente");
        }

        private void crearSiNoExiste(String clave, String valor, String tipo,
                        String descripcion, String categoria) {
                if (!configurationRepository.existsByClave(clave)) {
                        Configuration config = Configuration.builder()
                                        .clave(clave)
                                        .valor(valor)
                                        .tipo(tipo)
                                        .descripcion(descripcion)
                                        .categoria(categoria)
                                        .build();

                        configurationRepository.save(config);
                        log.debug("✓ Config creada: {}", clave);
                }
        }

        @Transactional(readOnly = true)
        public List<Configuration> obtenerTodas() {
                return configurationRepository.findAll();
        }
}