package com.miempresa.ecommerce.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.Credit;
import com.miempresa.ecommerce.models.enums.EstadoCredito;

/**
 * REPOSITORY: CRÉDITO
 * 
 * Interface para acceder a la tabla 'creditos'.
 */
@Repository
public interface CreditRepository extends JpaRepository<Credit, Long> {

        /**
         * Busca un crédito por su venta
         */
        Optional<Credit> findByVentaId(Long ventaId);

        /**
         * Busca créditos de un cliente
         */
        List<Credit> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);

        /**
         * Busca créditos por estado
         */
        List<Credit> findByEstadoOrderByFechaCreacionDesc(EstadoCredito estado);

        /**
         * Busca créditos activos
         */
        @Query("SELECT c FROM Credit c WHERE c.estado = 'ACTIVO' ORDER BY c.fechaCreacion DESC")
        List<Credit> findActivosOrderByFechaCreacionDesc();

        /**
         * Busca créditos de un cliente por estado
         */
        List<Credit> findByClienteIdAndEstadoOrderByFechaCreacionDesc(
                        Long clienteId,
                        EstadoCredito estado);

        /**
         * Busca créditos con cuotas vencidas
         */
        @Query("""
                        SELECT DISTINCT c FROM Credit c
                        JOIN c.cuotas cu
                        WHERE c.estado = 'ACTIVO'
                        AND cu.fechaVencimiento < :fecha
                        AND cu.montoPendiente > 0
                        """)
        List<Credit> obtenerCreditosConCuotasVencidas(@Param("fecha") LocalDate fecha);

        /**
         * Busca créditos con cuotas próximas a vencer
         */
        @Query("""
                        SELECT DISTINCT c FROM Credit c
                        JOIN c.cuotas cu
                        WHERE c.estado = 'ACTIVO'
                        AND cu.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin
                        AND cu.montoPendiente > 0
                        """)
        List<Credit> obtenerCreditosConCuotasProximasAVencer(
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin);

        /**
         * Suma total de monto pendiente de todos los créditos activos
         */
        @Query("SELECT COALESCE(SUM(c.montoPendiente), 0) FROM Credit c WHERE c.estado = 'ACTIVO'")
        BigDecimal sumarMontoPendienteTotal();

        /**
         * Suma monto pendiente de créditos de un cliente
         */
        @Query("""
                        SELECT COALESCE(SUM(c.montoPendiente), 0)
                        FROM Credit c
                        WHERE c.cliente.id = :clienteId
                        AND c.estado = 'ACTIVO'
                        """)
        BigDecimal sumarMontoPendientePorCliente(@Param("clienteId") Long clienteId);

        /**
         * Cuenta créditos por estado
         */
        long countByEstado(EstadoCredito estado);

        /**
         * Cuenta créditos activos
         */
        @Query("SELECT COUNT(c) FROM Credit c WHERE c.estado = 'ACTIVO'")
        long countActivos();

        /**
         * Obtiene clientes con más deuda
         */
        @Query("""
                        SELECT c.cliente.id,
                               c.cliente.numeroDocumento,
                               CASE WHEN c.cliente.tipoDocumento = 'DNI'
                                    THEN CONCAT(c.cliente.apellidoPaterno, ' ', c.cliente.apellidoMaterno, ' ', c.cliente.nombres)
                                    ELSE c.cliente.razonSocial
                               END as nombreCompleto,
                               SUM(c.montoPendiente) as totalDeuda
                        FROM Credit c
                        WHERE c.estado = 'ACTIVO'
                        GROUP BY c.cliente.id, c.cliente.numeroDocumento, nombreCompleto
                        ORDER BY totalDeuda DESC
                        """)
        List<Object[]> obtenerClientesConMasDeuda();

        /**
         * Obtiene los últimos créditos creados
         */
        List<Credit> findTop20ByOrderByFechaCreacionDesc();
}
