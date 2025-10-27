package com.miempresa.ecommerce.models;

import java.time.LocalDateTime;
import java.util.List; // <<--- AÑADIR import

import org.hibernate.annotations.CreationTimestamp; // <<--- AÑADIR import
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType; // Importar List
import jakarta.persistence.GeneratedValue; // Importar JsonIgnore
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "marcas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

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

    @Column(name = "logo", length = 255)
    private String logo;

    @Column(name = "activo", nullable = false)
    @Builder.Default // Asegura valor por defecto con @Builder
    private Boolean activo = true;

    // Relación inversa (opcional, pero útil para saber cuántos productos tiene)
    @JsonIgnore // Evita recursión infinita en JSON al obtener marcas
    @OneToMany(mappedBy = "marca", fetch = FetchType.LAZY) // mappedBy = nombre del campo en Product que referencia a
                                                           // Brand
    @ToString.Exclude // Evita recursión en toString() si Product también tiene referencia a Brand
    private List<Product> productos; // Lista de productos de esta marca

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