package com.miempresa.ecommerce.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.ProductImage;

/**
 * REPOSITORY: IMAGEN DE PRODUCTO
 * 
 * Interface para acceder a la tabla 'producto_imagenes'.
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * Busca todas las imágenes de un producto ordenadas por el campo 'orden'.
     */
    List<ProductImage> findByProductoIdOrderByOrdenAsc(Long productoId);

    /**
     * Busca la imagen principal de un producto.
     */
    Optional<ProductImage> findByProductoIdAndEsPrincipalTrue(Long productoId);

    /**
     * Elimina todas las imágenes de un producto.
     */
    void deleteByProductoId(Long productoId);

    /**
     * Cuenta cuántas imágenes tiene un producto.
     */
    long countByProductoId(Long productoId);
}
