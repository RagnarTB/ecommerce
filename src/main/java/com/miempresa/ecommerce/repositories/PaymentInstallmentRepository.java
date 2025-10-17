package com.miempresa.ecommerce.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.PaymentInstallment;

/**
 * REPOSITORY: PAGO-CUOTA (Distribución de pagos)
 * 
 * Interface para acceder a la tabla 'pago_cuota'.
 */
@Repository
public interface PaymentInstallmentRepository extends JpaRepository<PaymentInstallment, Long> {

        /**
         * Busca distribuciones de un pago.
         */
        List<PaymentInstallment> findByPagoId(Long pagoId);

        /**
         * Busca distribuciones hacia una cuota.
         */
        List<PaymentInstallment> findByCuotaId(Long cuotaId);

        /**
         * Elimina todas las distribuciones de un pago.
         */
        void deleteByPagoId(Long pagoId);

        /**
         * Suma total aplicado a una cuota desde todos los pagos.
         */
        @Query("""
                        SELECT COALESCE(SUM(pi.montoAplicado), 0)
                        FROM PaymentInstallment pi
                        WHERE pi.cuota.id = :cuotaId
                        """)
        BigDecimal sumarMontoAplicadoACuota(@Param("cuotaId") Long cuotaId);

        /**
         * Obtiene el historial de pagos de una cuota.
         */
        @Query("""
                        SELECT pi
                        FROM PaymentInstallment pi
                        WHERE pi.cuota.id = :cuotaId
                        ORDER BY pi.pago.fechaPago DESC
                        """)
        List<PaymentInstallment> obtenerHistorialPagosDeCuota(@Param("cuotaId") Long cuotaId);
}
