package com.miempresa.ecommerce.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.Sale;
import com.miempresa.ecommerce.models.enums.EstadoVenta;
import com.miempresa.ecommerce.models.enums.TipoPago;

/**
 * REPOSITORY: VENTA
 * 
 * Interface para acceder a la tabla 'ventas'.
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

        /**
         * Busca una venta por su número.
         */
        Optional<Sale> findByNumeroVenta(String numeroVenta);

        /**
         * Busca ventas por estado.
         */
        List<Sale> findByEstadoOrderByFechaVentaDesc(EstadoVenta estado);

        /**
         * Busca ventas completadas.
         */
        @Query("""
                        SELECT s FROM Sale s
                        WHERE s.estado = 'COMPLETADA'
                        ORDER BY s.fechaVenta DESC
                        """)
        List<Sale> findVentasCompletadasOrderByFechaVentaDesc();

        /**
         * Busca ventas de un cliente.
         */
        List<Sale> findByClienteIdOrderByFechaVentaDesc(Long clienteId);

        /**
         * Busca ventas de un usuario (vendedor).
         */
        List<Sale> findByUsuarioIdOrderByFechaVentaDesc(Long usuarioId);

        /**
         * Busca ventas en un rango de fechas.
         */
        List<Sale> findByFechaVentaBetweenOrderByFechaVentaDesc(
                        LocalDateTime fechaInicio,
                        LocalDateTime fechaFin);

        /**
         * Busca ventas por tipo de pago.
         */
        List<Sale> findByTipoPagoOrderByFechaVentaDesc(TipoPago tipoPago);

        /**
         * Busca ventas a crédito.
         */
        List<Sale> findByTipoPagoAndEstadoOrderByFechaVentaDesc(TipoPago tipoPago, EstadoVenta estado);

        /**
         * Cuenta ventas por estado.
         */
        long countByEstado(EstadoVenta estado);

        /**
         * Obtiene las últimas ventas.
         */
        List<Sale> findTop20ByOrderByFechaVentaDesc();

        /**
         * Busca ventas con filtros múltiples.
         */
        @Query("""
                        SELECT s FROM Sale s
                        WHERE (:clienteId IS NULL OR s.cliente.id = :clienteId)
                        AND (:usuarioId IS NULL OR s.usuario.id = :usuarioId)
                        AND (:estado IS NULL OR s.estado = :estado)
                        AND (:tipoPago IS NULL OR s.tipoPago = :tipoPago)
                        AND (:fechaInicio IS NULL OR s.fechaVenta >= :fechaInicio)
                        AND (:fechaFin IS NULL OR s.fechaVenta <= :fechaFin)
                        ORDER BY s.fechaVenta DESC
                        """)
        List<Sale> buscarConFiltros(
                        @Param("clienteId") Long clienteId,
                        @Param("usuarioId") Long usuarioId,
                        @Param("estado") EstadoVenta estado,
                        @Param("tipoPago") TipoPago tipoPago,
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        /**
         * Suma total de ventas en un rango de fechas.
         */
        @Query("""
                        SELECT COALESCE(SUM(s.total), 0)
                        FROM Sale s
                        WHERE s.estado = 'COMPLETADA'
                        AND s.fechaVenta BETWEEN :fechaInicio AND :fechaFin
                        """)
        BigDecimal sumarVentasPorFecha(
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        /**
         * Genera el siguiente número de venta.
         */
        @Query("""
                        SELECT COALESCE(MAX(CAST(SUBSTRING(s.numeroVenta, 10) AS long)), 0) + 1
                        FROM Sale s
                        WHERE s.numeroVenta LIKE :prefijo
                        """)
        Long generarSiguienteNumero(@Param("prefijo") String prefijo);

        /**
         * Obtiene ventas del día actual.
         */
        @Query("""
                        SELECT s FROM Sale s
                        WHERE FUNCTION('DATE', s.fechaVenta) = CURRENT_DATE
                        AND s.estado = 'COMPLETADA'
                        ORDER BY s.fechaVenta DESC
                        """)
        List<Sale> obtenerVentasDelDia();

        /**
         * Obtiene ventas del mes actual.
         */
        @Query("""
                        SELECT s FROM Sale s
                        WHERE FUNCTION('YEAR', s.fechaVenta) = FUNCTION('YEAR', CURRENT_DATE)
                        AND FUNCTION('MONTH', s.fechaVenta) = FUNCTION('MONTH', CURRENT_DATE)
                        AND s.estado = 'COMPLETADA'
                        ORDER BY s.fechaVenta DESC
                        """)
        List<Sale> obtenerVentasDelMes();

        /**
         * Obtiene estadísticas de ventas por día.
         */
        @Query("""
                        SELECT FUNCTION('DATE', s.fechaVenta) AS fecha,
                               COUNT(s) AS cantidad,
                               SUM(s.total) AS total
                        FROM Sale s
                        WHERE s.fechaVenta BETWEEN :fechaInicio AND :fechaFin
                        AND s.estado = 'COMPLETADA'
                        GROUP BY FUNCTION('DATE', s.fechaVenta)
                        ORDER BY fecha DESC
                        """)
        List<Object[]> obtenerEstadisticasPorDia(
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);
}
