package com.miempresa.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {
    private final CurrentURIInterceptor currentURIInterceptor;
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(currentURIInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", /* ... */ "/uploads/productos/**"); // <-- Excluir
                                                                                                         // nueva ruta
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String effectiveUploadDir = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        String resourceLocation = "file:" + effectiveUploadDir; // <-- Ruta física formateada
        log.info("Mapeo URL: /uploads/productos/** -> Física: {}", resourceLocation);
        registry.addResourceHandler("/uploads/productos/**") // <-- URL pública
                .addResourceLocations(resourceLocation); // <-- Directorio físico
    }
}