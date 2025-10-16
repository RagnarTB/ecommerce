package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD: CONFIGURACIÓN
 * 
 * Almacena configuraciones del sistema en formato clave-valor.
 * Permite personalizar la aplicación sin modificar código.
 * 
 * Configuraciones disponibles:
 * - Logo de la empresa
 * - Colores del tema
 * - Información de contacto
 * - Slider/banner principal
 * - Texto de bienvenida
 * - Horarios de atención
 * - Redes sociales
 */

@Entity
@Table(name = "configuraciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuration {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // CLAVE-VALOR
    // ========================================

    /**
     * Clave única de la configuración
     * Ejemplos:
     * - "logo"
     * - "color_primario"
     * - "slider_1_imagen"
     * - "texto_bienvenida"
     * - "telefono"
     */
    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    /**
     * Valor de la configuración
     * Puede ser texto, número, URL, JSON, etc.
     */
    @Column(name = "valor", columnDefinition = "TEXT")
    private String valor;

    /**
     * Tipo de dato del valor
     * Ejemplos: TEXT, NUMBER, IMAGE, COLOR, JSON, BOOLEAN
     */
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo = "TEXT";

    /**
     * Descripción de qué hace esta configuración
     */
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /**
     * Categoría de la configuración
     * Para agrupar en el panel de administración
     * Ejemplos: GENERAL, BRANDING, CONTACTO, SLIDER
     */
    @Column(name = "categoria", length = 50)
    private String categoria;

    // ========================================
    // AUDITORÍA
    // ========================================

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Por qué usar este sistema de configuración?
 * 
 * Sin configuración dinámica:
 * - Cada cambio requiere modificar código
 * - Necesitas recompilar y redesplegar
 * - Los clientes no pueden personalizarlo
 * 
 * Con configuración dinámica:
 * - Cambios desde el panel admin
 * - No requiere tocar código
 * - Personalización fácil para cada cliente
 * 
 * 2. Ejemplos de configuraciones:
 * 
 * BRANDING:
 * | clave | valor | tipo | categoría |
 * |-----------------|------------------------|--------|-----------|
 * | logo | config/logo.png | IMAGE | BRANDING |
 * | color_primario | #007bff | COLOR | BRANDING |
 * | color_secundario| #6c757d | COLOR | BRANDING |
 * 
 * CONTACTO:
 * | clave | valor | tipo | categoría |
 * |-----------------|------------------------|--------|-----------|
 * | telefono | +51 999 888 777 | TEXT | CONTACTO |
 * | email | ventas@miempresa.com | TEXT | CONTACTO |
 * | direccion | Av. Principal 123 | TEXT | CONTACTO |
 * | whatsapp | 51999888777 | TEXT | CONTACTO |
 * | facebook_url | fb.com/miempresa | TEXT | CONTACTO |
 * 
 * SLIDER:
 * | clave | valor | tipo | categoría |
 * |--------------------|---------------------------|--------|-----------|
 * | slider_1_imagen | slider/banner1.jpg | IMAGE | SLIDER |
 * | slider_1_titulo | ¡Ofertas Increíbles! | TEXT | SLIDER |
 * | slider_1_subtitulo | Hasta 50% de descuento | TEXT | SLIDER |
 * | slider_2_imagen | slider/banner2.jpg | IMAGE | SLIDER |
 * | slider_2_titulo | Nuevos Productos | TEXT | SLIDER |
 * 
 * GENERAL:
 * | clave | valor | tipo | categoría |
 * |--------------------|---------------------------|--------|-----------|
 * | nombre_empresa | Mi Empresa SAC | TEXT | GENERAL |
 * | texto_bienvenida | Bienvenido a nuestra... | TEXT | GENERAL |
 * | horario_atencion | Lun-Vie 9am-6pm | TEXT | GENERAL |
 * | igv_habilitado | true | BOOLEAN| GENERAL |
 * 
 * 3. ¿Cómo se usa en el código?
 * 
 * En el Service:
 * ```java
 * // Obtener configuración
 * String logo = configService.obtenerValor("logo");
 * String colorPrimario = configService.obtenerValor("color_primario");
 * 
 * // Guardar configuración
 * configService.guardar("telefono", "+51 999 888 777");
 * ```
 * 
 * En la vista (Thymeleaf):
 * ```html
 * <img th:src="@{'/uploads/' + ${config.logo}}" alt="Logo">
 * <div th:style="'background-color: ' + ${config.color_primario}">
 * ```
 * 
 * 4. ¿Qué es el campo "tipo"?
 * - Ayuda a validar y mostrar correctamente el valor
 * - TEXT: input de texto normal
 * - NUMBER: input numérico
 * - COLOR: color picker
 * - IMAGE: uploader de imágenes
 * - BOOLEAN: checkbox
 * - JSON: para estructuras complejas
 * 
 * 5. Valores por defecto al instalar:
 * Se crean automáticamente al iniciar la aplicación:
 * ```java
 * configuraciones.add("logo", "default-logo.png", "IMAGE");
 * configuraciones.add("color_primario", "#007bff", "COLOR");
 * configuraciones.add("nombre_empresa", "Mi E-commerce", "TEXT");
 * // etc...
 * ```
 */