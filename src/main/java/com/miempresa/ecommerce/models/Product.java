package com.miempresa.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Importar
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "codigo_sku", unique = true, length = 50)
    private String codigoSku;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "precio_oferta", precision = 10, scale = 2)
    private BigDecimal precioOferta;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Category categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_id")
    private Brand marca;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonManagedReference
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Lazy
                                                                                                               // puede
                                                                                                               // ayudar
    @OrderBy("orden ASC")
    @Builder.Default
    private List<ProductImage> imagenes = new ArrayList<>();

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "es_destacado", nullable = false)
    private Boolean esDestacado = false;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Métodos útiles (sin cambios)...
    // <<--- CÓDIGO DE MÉTODOS ÚTILES OMITIDO POR BREVEDAD --->>
    /**
     * Obtiene el precio actual (oferta si existe, sino el precio base)
     */
    public BigDecimal getPrecioActual() {
        return (this.precioOferta != null && this.precioOferta.compareTo(BigDecimal.ZERO) > 0)
                ? this.precioOferta
                : this.precioBase;
    }

    /**
     * Verifica si el producto tiene oferta
     */
    public boolean tieneOferta() {
        return this.precioOferta != null
                && this.precioOferta.compareTo(BigDecimal.ZERO) > 0
                && this.precioOferta.compareTo(this.precioBase) < 0;
    }

    /**
     * Calcula el porcentaje de descuento
     */
    public Integer getPorcentajeDescuento() {
        if (!tieneOferta()) {
            return 0;
        }

        BigDecimal descuento = this.precioBase.subtract(this.precioOferta);
        BigDecimal porcentaje = descuento.multiply(BigDecimal.valueOf(100))
                .divide(this.precioBase, 0, BigDecimal.ROUND_HALF_UP);
        return porcentaje.intValue();
    }

    /**
     * Verifica si hay stock disponible
     */
    public boolean hayStock() {
        return this.stockActual != null && this.stockActual > 0;
    }

    /**
     * Verifica si el stock está por debajo del mínimo
     */
    public boolean stockBajoMinimo() {
        return this.stockActual != null
                && this.stockMinimo != null
                && this.stockActual <= this.stockMinimo;
    }

    /**
     * Obtiene la imagen principal del producto
     */
    public ProductImage getImagenPrincipal() {
        // Asegurarse de que las imágenes estén cargadas si son LAZY
        if (!org.hibernate.Hibernate.isInitialized(this.imagenes)) {
            org.hibernate.Hibernate.initialize(this.imagenes);
        }
        return this.imagenes.stream()
                .filter(ProductImage::getEsPrincipal)
                .findFirst()
                .orElse(this.imagenes.isEmpty() ? null : this.imagenes.get(0));
    }

    /**
     * Agrega una imagen al producto
     */
    public void agregarImagen(ProductImage imagen) {
        if (this.imagenes == null) {
            this.imagenes = new ArrayList<>();
        }
        this.imagenes.add(imagen);
        imagen.setProducto(this);
    }

    /**
     * Elimina una imagen del producto
     */
    public void eliminarImagen(ProductImage imagen) {
        if (this.imagenes != null) {
            this.imagenes.remove(imagen);
            imagen.setProducto(null);
        }
    }

    /**
     * Aumenta el stock del producto
     */
    public void aumentarStock(Integer cantidad) {
        if (cantidad != null && cantidad > 0) {
            if (this.stockActual == null)
                this.stockActual = 0;
            this.stockActual += cantidad;
        }
    }

    /**
     * Disminuye el stock del producto
     * 
     * @return true si se pudo disminuir, false si no hay suficiente stock
     */
    public boolean disminuirStock(Integer cantidad) {
        if (cantidad != null && cantidad > 0 && this.stockActual != null && this.stockActual >= cantidad) {
            this.stockActual -= cantidad;
            return true;
        }
        return false;
    }
}