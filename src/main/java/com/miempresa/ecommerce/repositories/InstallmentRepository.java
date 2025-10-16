package com.miempresa.ecommerce.repositories;

import com.miempresa.ecommerce.models.Installment;
import com.miempresa.ecommerce.models.enums.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REPOSITORY: CUOTA
 * 
 * Interface para acceder a la tabla 'cuotas'.
 */

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long> {

        /**
         * Busca cuotas de un crédito ordenadas
         */
        List<Installment> findByCreditoIdOrderByNumeroCuotaAsc(Long creditoId);

        /**
         * Busca cuotas por estado
         */
        List<Installment> findByEstadoOrderByFechaVencimientoAsc(EstadoCuota estado);

        /**
         * Busca cuotas pendientes de un crédito
         */
        List<Installment> findByCreditoIdAndEstadoOrderByNumeroCuotaAsc(
                        Long creditoId,
                        EstadoCuota estado);

        /**
         * Busca cuotas vencidas
         */
        @Query("SELECT i FROM Installment i WHERE " +
                        "i.fechaVencimiento < :fecha AND " +
                        "i.montoPendiente > 0 AND " +
                        "i.credito.estado = 'ACTIVO' " +
                        "ORDER BY i.fechaVencimiento ASC")
        List<Installment> obtenerCuotasVencidas(@Param("fecha") LocalDate fecha);

        /**
         * Busca cuotas que vencen hoy
         */
        @Query("SELECT i FROM Installment i WHERE " +
                        "i.fechaVencimiento = :fecha AND " +
                        "i.montoPendiente > 0 AND " +
                        "i.credito.estado = 'ACTIVO' " +
                        "ORDER BY i.credito.id, i.numeroCuota")
        List<Installment> obtenerCuotasQueVencenHoy(@Param("fecha") LocalDate fecha);

        /**
         * Busca cuotas próximas a vencer (en los próximos N días)
         */
        @Query("SELECT i FROM Installment i WHERE " +
                        "i.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin AND " +
                        "i.montoPendiente > 0 AND " +
                        "i.credito.estado = 'ACTIVO' " +
                        "ORDER BY i.fechaVencimiento ASC")
        List<Installment> obtenerCuotasProximasAVencer(
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin);

        /**
         * Busca cuotas vencidas de un cliente específico
         */
        @Query("SELECT i FROM Installment i WHERE " +
                        "i.credito.cliente.id = :clienteId AND " +
                        "i.fechaVencimiento < :fecha AND " +
                        "i.montoPendiente > 0 AND " +
                        "i.credito.estado = 'ACTIVO' " +
                        "ORDER BY i.fechaVencimiento ASC")
        List<Installment> obtenerCuotasVencidasPorCliente(
                        @Param("clienteId") Long clienteId,
                        @Param("fecha") LocalDate fecha);

        /**
         * Cuenta cuotas por estado
         */
        long countByEstado(EstadoCuota estado);

        /**
         * Cuenta cuotas vencidas totales
         */
        @Query("SELECT COUNT(i) FROM Installment i WHERE " +
                        "i.fechaVencimiento < CURRENT_DATE AND " +
                        "i.montoPendiente > 0 AND " +
                        "i.credito.estado = 'ACTIVO'")
        long contarCuotasVencidas();

        /**
         * Suma monto total pendiente de cuotas vencidas
         */
        @Query("SELECT COALESCE(SUM(i.montoPendiente), 0) FROM Installment i WHERE " +
                        "i.fechaVencimiento < CURRENT_DATE AND " +
                        "i.montoPendiente > 0 AND " +
                        "i.credito.estado = 'ACTIVO'")
        BigDecimal sumarMontoPendienteCuotasVencidas();

        /**
         * Obtiene la primera cuota pendiente de un crédito
         */
        @Query("SELECT i FROM Installment i WHERE " +
                        "i.credito.id = :creditoId AND " +
                        "i.montoPendiente > 0 " +
                        "ORDER BY i.numeroCuota ASC " +
                        "LIMIT 1")
        Installment obtenerPrimeraCuotaPendiente(@Param("creditoId") Long creditoId);
}