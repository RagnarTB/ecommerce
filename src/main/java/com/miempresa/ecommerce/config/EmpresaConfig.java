package com.miempresa.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * CONFIGURACIÓN: DATOS DE LA EMPRESA
 * 
 * Centraliza los datos de la empresa para usar en PDFs y documentos.
 * 
 * Configurar en application.properties o application.yml:
 * 
 * empresa.nombre=MI EMPRESA SAC
 * empresa.ruc=20123456789
 * empresa.direccion=Av. Principal 123, Lima
 * empresa.telefono=01-1234567
 * empresa.email=ventas@miempresa.com
 */

@Configuration
@ConfigurationProperties(prefix = "empresa")
@Data
public class EmpresaConfig {

    private String nombre;
    private String ruc;
    private String direccion;
    private String telefono;
    private String email;
}