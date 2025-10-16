package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD: PRODUCTO
 * 
 * Representa los productos que se venden en la tienda.
 * Cada producto tiene:
 * - Información básica (nombre, descripción, precios)
 * - Stock y control de inventario
 * - Relación con categoría y marca
 * - Múltiples imágenes (hasta 5)
 */

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // INFORMACIÓN BÁSICA
    // ========================================

    /**
     * Nombre del producto
     */
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    /**
     * Descripción detallada del producto
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Código SKU (Stock Keeping Unit)
     * Código único de identificación del producto
     */
    @Column(name = "codigo_sku", unique = true, length = 50)
    private String codigoSku;

    // ========================================
    // PRECIOS
    // ========================================

    /**
     * Precio base del producto
     * BigDecimal es mejor para dinero (evita errores de redondeo)
     */
    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    /**
     * Precio de oferta (si hay descuento)
     * Si es NULL, no hay oferta
     */
    @Column(name = "precio_oferta", precision = 10, scale = 2)
    private BigDecimal precioOferta;

    // ========================================
    // INVENTARIO
    // ========================================

    /**
     * Stock actual disponible
     */
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    /**
     * Stock mínimo (alerta cuando esté por debajo)
     */
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;

    // ========================================
    // RELACIONES CON OTRAS TABLAS
    // ========================================

    /**
     * RELACIÓN: Muchos productos pertenecen a UNA categoría
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Category categoria;

    /**
     * RELACIÓN: Muchos productos pertenecen a UNA marca
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_id")
    private Brand marca;

    /**
     * RELACIÓN: Un producto tiene MUCHAS imágenes
     * 
     * @OneToMany - Uno a Muchos
     *            mappedBy = "producto" - El atributo "producto" en ProductImage es
     *            el dueño de la relación
     *            cascade = CascadeType.ALL - Si eliminas el producto, se eliminan
     *            sus imágenes
     *            orphanRemoval = true - Si quitas una imagen de la lista, se
     *            elimina de la BD
     */
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<ProductImage> imagenes = new ArrayList<>();

    // ========================================
    // ESTADO Y CARACTERÍSTICAS
    // ========================================

    /**
     * Estado del producto (activo o inactivo)
     * Si está inactivo, no se muestra en el catálogo público
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * Marca el producto como destacado
     * Los productos destacados aparecen en la página principal
     */
    @Column(name = "es_destacado", nullable = false)
    private Boolean esDestacado = false;

    // ========================================
    // AUDITORÍA
    // ========================================

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

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
        return this.imagenes.stream()
                .filter(ProductImage::getEsPrincipal)
                .findFirst()
                .orElse(this.imagenes.isEmpty() ? null : this.imagenes.get(0));
    }

    /**
     * Agrega una imagen al producto
     */
    public void agregarImagen(ProductImage imagen) {
        this.imagenes.add(imagen);
        imagen.setProducto(this);
    }

    /**
     * Elimina una imagen del producto
     */
    public void eliminarImagen(ProductImage imagen) {
        this.imagenes.remove(imagen);
        imagen.setProducto(null);
    }

    /**
     * Aumenta el stock del producto
     */
    public void aumentarStock(Integer cantidad) {
        if (cantidad > 0) {
            this.stockActual += cantidad;
        }
    }

    /**
     * Disminuye el stock del producto
     * 
     * @return true si se pudo disminuir, false si no hay suficiente stock
     */
    public boolean disminuirStock(Integer cantidad) {
        if (cantidad > 0 && this.stockActual >= cantidad) {
            this.stockActual -= cantidad;
            return true;
        }
        return false;
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Por qué usar BigDecimal para precios?
 * 
 * MAL (con double):
 * double precio = 10.10;
 * double total = precio * 3;
 * System.out.println(total); // Resultado: 30.299999999999997 ❌
 * 
 * BIEN (con BigDecimal):
 * BigDecimal precio = new BigDecimal("10.10");
 * BigDecimal total = precio.multiply(new BigDecimal("3"));
 * System.out.println(total); // Resultado: 30.30 ✅
 * 
 * 2. ¿Qué es @OneToMany?
 * - Un producto tiene MUCHAS imágenes
 * - mappedBy = "producto" indica que ProductImage tiene el campo "producto"
 * - cascade = CascadeType.ALL: operaciones en cascada
 * * Si guardas producto → guarda imágenes automáticamente
 * * Si eliminas producto → elimina imágenes automáticamente
 * 
 * 3. ¿Qué es orphanRemoval = true?
 * - Si quitas una imagen de la lista, se elimina de la BD
 * 
 * Ejemplo:
 * producto.getImagenes().remove(imagen); // Quita de la lista
 * productoRepository.save(producto); // La imagen se elimina de la BD
 * 
 * 4. ¿Para qué sirven los métodos auxiliares?
 * - getPrecioActual(): devuelve el precio con oferta o el normal
 * - tieneOferta(): verifica si hay descuento
 * - hayStock(): verifica si hay productos disponibles
 * - getImagenPrincipal(): obtiene la imagen principal para mostrar
 * 
 * Se usan en las vistas:
 * <span>${producto.precioActual}</span>
 * <img src="${producto.imagenPrincipal.url}">
 * 
 * 5. ¿Qué es @OrderBy?
 * - Ordena automáticamente las imágenes por el campo "orden"
 * - La imagen con orden=1 aparece primero, luego orden=2, etc.
 */