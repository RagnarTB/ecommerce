package com.miempresa.ecommerce.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.Product;

/**
 * REPOSITORY: PRODUCTO
 * 
 * Interface para acceder a la tabla 'productos'.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

        /**
         * Busca un producto por su código SKU.
         */
        Optional<Product> findByCodigoSku(String codigoSku);

        /**
         * Busca productos activos.
         */
        List<Product> findByActivoTrue();

        /**
         * Busca productos destacados y activos.
         */
        List<Product> findByEsDestacadoTrueAndActivoTrue();

        /**
         * Busca productos por categoría.
         */
        List<Product> findByCategoriaIdAndActivoTrue(Long categoriaId);

        /**
         * Busca productos por marca.
         */
        List<Product> findByMarcaIdAndActivoTrue(Long marcaId);

        /**
         * Busca productos cuyo nombre contenga un texto.
         */
        List<Product> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

        /**
         * Busca productos con stock bajo mínimo (alerta de reposición).
         */
        @Query("SELECT p FROM Product p WHERE p.stockActual <= p.stockMinimo AND p.activo = true")
        List<Product> obtenerProductosStockBajo();

        /**
         * Busca productos sin stock.
         */
        @Query("SELECT p FROM Product p WHERE p.stockActual = 0 AND p.activo = true")
        List<Product> obtenerProductosSinStock();

        /**
         * Busca productos con stock disponible.
         */
        @Query("SELECT p FROM Product p WHERE p.stockActual > 0 AND p.activo = true")
        List<Product> obtenerProductosConStock();

        /**
         * Busca productos en un rango de precios.
         * Corrige el alcance de 'p.activo = true' para aplicar a ambos casos.
         */
        @Query("""
                        SELECT p FROM Product p
                        WHERE (
                            (p.precioOferta IS NOT NULL AND p.precioOferta BETWEEN :precioMin AND :precioMax)
                            OR
                            (p.precioOferta IS NULL AND p.precioBase BETWEEN :precioMin AND :precioMax)
                        )
                        AND p.activo = true
                        """)
        List<Product> buscarPorRangoPrecio(
                        @Param("precioMin") BigDecimal precioMin,
                        @Param("precioMax") BigDecimal precioMax);

        /**
         * Busca productos con oferta activa.
         */
        @Query("SELECT p FROM Product p WHERE p.precioOferta IS NOT NULL AND p.precioOferta > 0 AND p.precioOferta < p.precioBase AND p.activo = true")
        List<Product> obtenerProductosConOferta();

        /**
         * Busca productos con filtros múltiples.
         */
        @Query("""
                        SELECT p FROM Product p
                        WHERE (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
                        AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
                        AND (:marcaId IS NULL OR p.marca.id = :marcaId)
                        AND (
                            (:precioMin IS NULL OR
                                (p.precioOferta IS NOT NULL AND p.precioOferta >= :precioMin) OR
                                (p.precioOferta IS NULL AND p.precioBase >= :precioMin)
                            )
                        )
                        AND (
                            (:precioMax IS NULL OR
                                (p.precioOferta IS NOT NULL AND p.precioOferta <= :precioMax) OR
                                (p.precioOferta IS NULL AND p.precioBase <= :precioMax)
                            )
                        )
                        AND p.activo = true
                        """)
        List<Product> buscarConFiltros(
                        @Param("nombre") String nombre,
                        @Param("categoriaId") Long categoriaId,
                        @Param("marcaId") Long marcaId,
                        @Param("precioMin") BigDecimal precioMin,
                        @Param("precioMax") BigDecimal precioMax);

        /**
         * Cuenta productos activos.
         */
        long countByActivoTrue();

        /**
         * Verifica si existe un producto con ese SKU.
         */
        boolean existsByCodigoSku(String codigoSku);
}
