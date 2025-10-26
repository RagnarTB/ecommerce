package com.miempresa.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonBackReference; // Importar
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "producto_imagenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude
    private Product producto;

    @Column(name = "url", nullable = false, length = 255)
    private String url; // Solo el nombre del archivo: uuid.jpg

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = false;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // Método getUrlCompleta() eliminado o modificado si es necesario
    // Las plantillas Thymeleaf construirán la URL completa
}