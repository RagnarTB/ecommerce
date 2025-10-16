package com.miempresa.ecommerce.repositories;

import com.miempresa.ecommerce.models.SaleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REPOSITORY: DETALLE DE VENTA
 * 
 * Interface para acceder a la tabla 'venta_detalles'.
 */

@Repository
public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {

        /**
         * Busca los detalles de una venta
         */
        List<SaleDetail> findByVentaId(Long ventaId);

        /**
         * Busca detalles de un producto específico
         */
        List<SaleDetail> findByProductoId(Long productoId);

        /**
         * Elimina todos los detalles de una venta
         */
        void deleteByVentaId(Long ventaId);

        /**
         * Cuenta cuántos detalles tiene una venta
         */
        long countByVentaId(Long ventaId);

        /**
         * Obtiene productos más vendidos
         */
        @Query("SELECT d.producto.id, d.producto.nombre, SUM(d.cantidad) as totalVendido, " +
                        "SUM(d.subtotal) as totalIngresos " +
                        "FROM SaleDetail d " +
                        "WHERE d.venta.estado = 'COMPLETADA' " +
                        "GROUP BY d.producto.id, d.producto.nombre " +
                        "ORDER BY totalVendido DESC")
        List<Object[]> obtenerProductosMasVendidos();

        /**
         * Obtiene productos más vendidos en un rango de fechas
         */
        @Query("SELECT d.producto.id, d.producto.nombre, SUM(d.cantidad) as totalVendido, " +
                        "SUM(d.subtotal) as totalIngresos " +
                        "FROM SaleDetail d " +
                        "WHERE d.venta.estado = 'COMPLETADA' " +
                        "AND d.venta.fechaVenta BETWEEN :fechaInicio AND :fechaFin " +
                        "GROUP BY d.producto.id, d.producto.nombre " +
                        "ORDER BY totalVendido DESC")
        List<Object[]> obtenerProductosMasVendidosPorFecha(
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        /**
         * Suma total de unidades vendidas de un producto
         */
        @Query("SELECT COALESCE(SUM(d.cantidad), 0) FROM SaleDetail d " +
                        "WHERE d.producto.id = :productoId AND d.venta.estado = 'COMPLETADA'")
        Long sumarCantidadVendida(@Param("productoId") Long productoId);
}