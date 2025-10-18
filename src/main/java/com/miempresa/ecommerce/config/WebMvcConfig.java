package com.miempresa.ecommerce.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentURIInterceptor currentURIInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(currentURIInterceptor)
                .addPathPatterns("/**") // aplica a todas las rutas
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/vendor/**"); // excluye estáticos
    }
}
