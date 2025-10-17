package com.miempresa.ecommerce.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.Payment;
import com.miempresa.ecommerce.models.enums.MetodoPago;

/**
 * REPOSITORY: PAGO
 * 
 * Interface para acceder a la tabla 'pagos'.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

        /**
         * Busca pagos de una venta.
         */
        List<Payment> findByVentaIdOrderByFechaPagoDesc(Long ventaId);

        /**
         * Busca pagos de un crédito (abonos).
         */
        List<Payment> findByCreditoIdOrderByFechaPagoDesc(Long creditoId);

        /**
         * Busca pagos por método de pago.
         */
        List<Payment> findByMetodoPagoOrderByFechaPagoDesc(MetodoPago metodoPago);

        /**
         * Busca pagos registrados por un usuario.
         */
        List<Payment> findByUsuarioIdOrderByFechaPagoDesc(Long usuarioId);

        /**
         * Busca pagos en un rango de fechas.
         */
        List<Payment> findByFechaPagoBetweenOrderByFechaPagoDesc(
                        LocalDateTime fechaInicio,
                        LocalDateTime fechaFin);

        /**
         * Suma total de pagos de una venta.
         */
        @Query("""
                        SELECT COALESCE(SUM(p.monto), 0)
                        FROM Payment p
                        WHERE p.venta.id = :ventaId
                        """)
        BigDecimal sumarPagosPorVenta(@Param("ventaId") Long ventaId);

        /**
         * Suma total de abonos de un crédito.
         */
        @Query("""
                        SELECT COALESCE(SUM(p.monto), 0)
                        FROM Payment p
                        WHERE p.credito.id = :creditoId
                        """)
        BigDecimal sumarAbonosPorCredito(@Param("creditoId") Long creditoId);

        /**
         * Obtiene estadísticas de pagos por método.
         */
        @Query("""
                        SELECT p.metodoPago, COUNT(p), SUM(p.monto)
                        FROM Payment p
                        WHERE p.fechaPago BETWEEN :fechaInicio AND :fechaFin
                        GROUP BY p.metodoPago
                        """)
        List<Object[]> obtenerEstadisticasPorMetodo(
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        /**
         * Obtiene los últimos pagos.
         */
        List<Payment> findTop20ByOrderByFechaPagoDesc();
}
