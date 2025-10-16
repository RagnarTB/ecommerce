package com.miempresa.ecommerce.models;

import com.miempresa.ecommerce.models.enums.EstadoCuota;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ENTIDAD: CUOTA
 * 
 * Representa una cuota individual de un crédito.
 * Cada crédito tiene N cuotas (hasta 24).
 * 
 * Ejemplo: Crédito de S/ 1200 en 12 cuotas
 * - Cuota 1: S/ 100, vence 2025-11-16
 * - Cuota 2: S/ 100, vence 2025-12-16
 * - ...
 * - Cuota 12: S/ 100, vence 2026-10-16
 */

@Entity
@Table(name = "cuotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Installment {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // RELACIONES
    // ========================================

    /**
     * Crédito al que pertenece esta cuota
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credito_id", nullable = false)
    @ToString.Exclude
    private Credit credito;

    // ========================================
    // INFORMACIÓN DE LA CUOTA
    // ========================================

    /**
     * Número de cuota (1, 2, 3, ...)
     */
    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    /**
     * Monto original de la cuota
     */
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    /**
     * Monto pagado hasta el momento
     */
    @Column(name = "monto_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    /**
     * Monto pendiente por pagar
     */
    @Column(name = "monto_pendiente", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPendiente;

    /**
     * Fecha de vencimiento de la cuota
     */
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    /**
     * Estado de la cuota (PENDIENTE, PAGADA_PARCIAL, PAGADA, VENCIDA)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCuota estado = EstadoCuota.PENDIENTE;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Aplica un pago a la cuota
     * 
     * @param montoPago Monto a abonar a esta cuota
     */
    public void aplicarPago(BigDecimal montoPago) {
        if (montoPago == null || montoPago.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // No se puede pagar más del monto pendiente
        BigDecimal montoAAplicar = montoPago.min(this.montoPendiente);

        // Actualizar montos
        this.montoPagado = this.montoPagado.add(montoAAplicar);
        this.montoPendiente = this.montoPendiente.subtract(montoAAplicar);

        // Actualizar estado
        actualizarEstado();
    }

    /**
     * Actualiza el estado de la cuota según su situación
     */
    public void actualizarEstado() {
        if (this.montoPendiente.compareTo(BigDecimal.ZERO) == 0) {
            // Totalmente pagada
            this.estado = EstadoCuota.PAGADA;
        } else if (this.montoPagado.compareTo(BigDecimal.ZERO) > 0) {
            // Tiene pagos parciales
            this.estado = EstadoCuota.PAGADA_PARCIAL;
        } else if (LocalDate.now().isAfter(this.fechaVencimiento)) {
            // Vencida sin pagos
            this.estado = EstadoCuota.VENCIDA;
        } else {
            // Pendiente sin vencer
            this.estado = EstadoCuota.PENDIENTE;
        }
    }

    /**
     * Verifica si la cuota está pagada completamente
     */
    public boolean estaPagada() {
        return this.estado == EstadoCuota.PAGADA;
    }

    /**
     * Verifica si la cuota está vencida
     */
    public boolean estaVencida() {
        return this.estado == EstadoCuota.VENCIDA ||
                (LocalDate.now().isAfter(this.fechaVencimiento) &&
                        this.montoPendiente.compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Verifica si la cuota está pendiente
     */
    public boolean estaPendiente() {
        return this.estado == EstadoCuota.PENDIENTE;
    }

    /**
     * Obtiene los días hasta el vencimiento
     * Número positivo = días que faltan
     * Número negativo = días de atraso
     */
    public long getDiasParaVencimiento() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.fechaVencimiento);
    }

    /**
     * Verifica si la cuota está próxima a vencer (menos de 7 días)
     */
    public boolean estaProximaAVencer() {
        long dias = getDiasParaVencimiento();
        return dias >= 0 && dias <= 7;
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. Estados de una cuota:
 * 
 * PENDIENTE:
 * - No ha sido pagada
 * - Aún no ha vencido
 * - monto_pagado = 0
 * - fecha_vencimiento >= hoy
 * 
 * PAGADA_PARCIAL:
 * - Tiene abonos pero no está completa
 * - monto_pagado > 0
 * - monto_pendiente > 0
 * 
 * PAGADA:
 * - Completamente pagada
 * - monto_pendiente = 0
 * 
 * VENCIDA:
 * - Fecha de vencimiento pasó
 * - Aún tiene saldo pendiente
 * - fecha_vencimiento < hoy
 * - monto_pendiente > 0
 * 
 * 2. Ejemplo de ciclo de vida de una cuota:
 * 
 * Día 0 (creación):
 * - numeroCuota: 1
 * - monto: S/ 100
 * - monto_pagado: S/ 0
 * - monto_pendiente: S/ 100
 * - fecha_vencimiento: 2025-11-16
 * - estado: PENDIENTE
 * 
 * Día 15 (abono parcial):
 * Cliente abona S/ 25
 * - monto_pagado: S/ 25
 * - monto_pendiente: S/ 75
 * - estado: PAGADA_PARCIAL
 * 
 * Día 28 (completa el pago):
 * Cliente abona S/ 75
 * - monto_pagado: S/ 100
 * - monto_pendiente: S/ 0
 * - estado: PAGADA
 * 
 * 3. ¿Qué pasa si no paga?
 * 
 * Día 31 (después del vencimiento):
 * - fecha_vencimiento: 2025-11-16 (ya pasó)
 * - monto_pendiente: S/ 75 (aún debe)
 * - estado: VENCIDA
 * 
 * 4. Tabla en base de datos:
 * 
 * | id | credito_id | numero_cuota | monto | monto_pagado | monto_pendiente |
 * fecha_vencimiento | estado |
 * |----|------------|--------------|--------|--------------|-----------------|-------------------|----------------|
 * | 1 | 5 | 1 | 100.00 | 25.00 | 75.00 | 2025-11-16 | PAGADA_PARCIAL |
 * | 2 | 5 | 2 | 100.00 | 0.00 | 100.00 | 2025-12-16 | PENDIENTE |
 * | 3 | 5 | 3 | 100.00 | 100.00 | 0.00 | 2026-01-16 | PAGADA |
 * 
 * 5. Métodos útiles:
 * - aplicarPago(): Registra un abono a la cuota
 * - actualizarEstado(): Recalcula el estado según montos y fechas
 * - getDiasParaVencimiento(): Saber cuántos días faltan/pasaron
 * - estaProximaAVencer(): Alerta si vence pronto (menos de 7 días)
 */