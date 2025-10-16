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
 * SERVICE: CONFIGURACIÓN
 * 
 * Gestiona las configuraciones del sistema (logo, colores, etc).
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

    /**
     * Obtiene el valor de una configuración por clave
     */
    @Transactional(readOnly = true)
    public String obtenerValor(String clave) {
        Optional<Configuration> config = configurationRepository.findByClave(clave);
        return config.map(Configuration::getValor).orElse(null);
    }

    /**
     * Obtiene el valor con un default si no existe
     */
    @Transactional(readOnly = true)
    public String obtenerValor(String clave, String valorPorDefecto) {
        String valor = obtenerValor(clave);
        return valor != null ? valor : valorPorDefecto;
    }

    /**
     * Obtiene todas las configuraciones como Map
     * Útil para pasar a las vistas
     */
    @Transactional(readOnly = true)
    public Map<String, String> obtenerTodasComoMapa() {
        List<Configuration> configs = configurationRepository.findAll();
        Map<String, String> mapa = new HashMap<>();

        for (Configuration config : configs) {
            mapa.put(config.getClave(), config.getValor());
        }

        return mapa;
    }

    /**
     * Obtiene configuraciones por categoría
     */
    @Transactional(readOnly = true)
    public List<Configuration> obtenerPorCategoria(String categoria) {
        return configurationRepository.findByCategoria(categoria);
    }

    // ========================================
    // GUARDAR/ACTUALIZAR CONFIGURACIONES
    // ========================================

    /**
     * Guarda o actualiza una configuración
     */
    public Configuration guardar(String clave, String valor) {
        return guardar(clave, valor, "TEXT", null, null);
    }

    /**
     * Guarda o actualiza una configuración completa
     */
    public Configuration guardar(String clave, String valor, String tipo,
            String descripcion, String categoria) {
        log.info("Guardando configuración: {} = {}", clave, valor);

        Optional<Configuration> existente = configurationRepository.findByClave(clave);

        Configuration config;

        if (existente.isPresent()) {
            // Actualizar
            config = existente.get();
            config.setValor(valor);

            if (tipo != null)
                config.setTipo(tipo);
            if (descripcion != null)
                config.setDescripcion(descripcion);
            if (categoria != null)
                config.setCategoria(categoria);
        } else {
            // Crear nueva
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

    /**
     * Guarda múltiples configuraciones
     */
    public void guardarMultiples(Map<String, String> configuraciones) {
        for (Map.Entry<String, String> entry : configuraciones.entrySet()) {
            guardar(entry.getKey(), entry.getValue());
        }
    }

    // ========================================
    // INICIALIZACIÓN
    // ========================================

    /**
     * Crea configuraciones por defecto si no existen
     */
    public void inicializarConfiguraciones() {
        log.info("Inicializando configuraciones por defecto");

        // BRANDING
        crearSiNoExiste("logo", "default-logo.png", "IMAGE", "Logo de la empresa", "BRANDING");
        crearSiNoExiste("color_primario", "#007bff", "COLOR", "Color primario del tema", "BRANDING");
        crearSiNoExiste("color_secundario", "#6c757d", "COLOR", "Color secundario del tema", "BRANDING");

        // CONTACTO
        crearSiNoExiste("nombre_empresa", "Mi E-commerce", "TEXT", "Nombre de la empresa", "GENERAL");
        crearSiNoExiste("telefono", "+51 999 888 777", "TEXT", "Teléfono de contacto", "CONTACTO");
        crearSiNoExiste("email", "ventas@miempresa.com", "TEXT", "Email de contacto", "CONTACTO");
        crearSiNoExiste("direccion", "Av. Principal 123", "TEXT", "Dirección física", "CONTACTO");
        crearSiNoExiste("whatsapp", "51999888777", "TEXT", "WhatsApp", "CONTACTO");
        crearSiNoExiste("facebook_url", "", "TEXT", "URL de Facebook", "CONTACTO");
        crearSiNoExiste("instagram_url", "", "TEXT", "URL de Instagram", "CONTACTO");

        // SLIDER
        crearSiNoExiste("slider_1_imagen", "slider/banner1.jpg", "IMAGE", "Imagen slider 1", "SLIDER");
        crearSiNoExiste("slider_1_titulo", "Bienvenido", "TEXT", "Título slider 1", "SLIDER");
        crearSiNoExiste("slider_1_subtitulo", "Las mejores ofertas", "TEXT", "Subtítulo slider 1", "SLIDER");

        crearSiNoExiste("slider_2_imagen", "slider/banner2.jpg", "IMAGE", "Imagen slider 2", "SLIDER");
        crearSiNoExiste("slider_2_titulo", "Nuevos productos", "TEXT", "Título slider 2", "SLIDER");
        crearSiNoExiste("slider_2_subtitulo", "Descubre nuestra colección", "TEXT", "Subtítulo slider 2", "SLIDER");

        // GENERAL
        crearSiNoExiste("texto_bienvenida", "Bienvenido a nuestra tienda online", "TEXT", "Texto de bienvenida",
                "GENERAL");
        crearSiNoExiste("horario_atencion", "Lun-Vie 9am-6pm", "TEXT", "Horario de atención", "GENERAL");

        log.info("Configuraciones inicializadas");
    }

    /**
     * Crea una configuración si no existe
     */
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
        }
    }

    @Transactional(readOnly = true)
    public List<Configuration> obtenerTodas() {
        return configurationRepository.findAll();
    }
}

/**
 * USO EN LOS CONTROLLERS:
 * 
 * // Obtener una configuración
 * String logo = configService.obtenerValor("logo");
 * 
 * // Pasar todas las configuraciones a la vista
 * Map<String, String> config = configService.obtenerTodasComoMapa();
 * model.addAttribute("config", config);
 * 
 * // En Thymeleaf
 * <img th:src="@{'/uploads/' + ${config.logo}}" />
 * <div th:style="'background-color: ' + ${config.color_primario}">
 */