package com.miempresa.ecommerce.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp; // <<--- AÑADIR import
import org.hibernate.annotations.UpdateTimestamp; // <<--- AÑADIR import

import jakarta.persistence.Column; // <<--- AÑADIR import
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Añadir validaciones
    @NotBlank(message = "El nombre es obligatorio") // <<--- AÑADIR
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres") // <<--- AÑADIR
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres") // <<--- AÑADIR
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "imagen", length = 255)
    private String imagen;

    @Column(name = "activo", nullable = false)
    @Builder.Default // Asegura valor por defecto con @Builder
    private Boolean activo = true;

    // Añadir validación
    @Min(value = 0, message = "El orden debe ser 0 o mayor") // <<--- AÑADIR
    @Column(name = "orden")
    @Builder.Default // Asegura valor por defecto con @Builder
    private Integer orden = 0;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public boolean estaActiva() {
        return this.activo != null && this.activo;
    }
}