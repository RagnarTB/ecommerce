package com.miempresa.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonBackReference; // Importar
import com.miempresa.ecommerce.models.enums.EstadoCuota;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "cuotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonBackReference("credit-installments")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credito_id", nullable = false)
    @ToString.Exclude
    private Credit credito;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "monto_pagado", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Column(name = "monto_pendiente", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPendiente;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCuota estado = EstadoCuota.PENDIENTE;

    // Métodos útiles (sin cambios)...
    // <<--- CÓDIGO DE MÉTODOS ÚTILES OMITIDO POR BREVEDAD --->>
    /**
     * Aplica un pago a la cuota
     * 
     * @param montoAAplicar Monto a abonar a esta cuota
     */
    public void aplicarPago(BigDecimal montoAAplicar) {
        if (montoAAplicar == null || montoAAplicar.compareTo(BigDecimal.ZERO) <= 0 ||
                this.montoPendiente == null || this.montoPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Asegurar que no se aplique más del monto pendiente
        BigDecimal montoRealAplicado = montoAAplicar.min(this.montoPendiente);

        // Actualizar montos
        this.montoPagado = (this.montoPagado == null ? BigDecimal.ZERO : this.montoPagado).add(montoRealAplicado);
        this.montoPendiente = this.montoPendiente.subtract(montoRealAplicado);

        // Redondear para evitar problemas de precisión
        this.montoPagado = this.montoPagado.setScale(2, java.math.RoundingMode.HALF_UP);
        this.montoPendiente = this.montoPendiente.setScale(2, java.math.RoundingMode.HALF_UP);

        // Actualizar estado
        actualizarEstado();
    }

    /**
     * Actualiza el estado de la cuota según su situación
     */
    public void actualizarEstado() {
        if (this.montoPendiente == null) {
            this.montoPendiente = this.monto; // Asegurar que no sea null
        }
        if (this.montoPagado == null) {
            this.montoPagado = BigDecimal.ZERO;
        }

        // Usar compareTo para comparar BigDecimals
        if (this.montoPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            // Totalmente pagada
            this.estado = EstadoCuota.PAGADA;
        } else if (this.montoPagado.compareTo(BigDecimal.ZERO) > 0) {
            // Tiene pagos parciales pero aún debe
            this.estado = EstadoCuota.PAGADA_PARCIAL;
            // Re-evaluar si está vencida aunque sea parcial
            if (this.fechaVencimiento != null && LocalDate.now().isAfter(this.fechaVencimiento)) {
                this.estado = EstadoCuota.VENCIDA; // Si está vencida y parcialmente pagada, sigue vencida
            }
        } else if (this.fechaVencimiento != null && LocalDate.now().isAfter(this.fechaVencimiento)) {
            // Vencida sin pagos
            this.estado = EstadoCuota.VENCIDA;
        } else {
            // Pendiente sin vencer y sin pagos
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
        // Una cuota está vencida si su fecha ya pasó Y AÚN tiene monto pendiente
        return this.fechaVencimiento != null &&
                LocalDate.now().isAfter(this.fechaVencimiento) &&
                this.montoPendiente != null &&
                this.montoPendiente.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Obtiene los días de vencimiento. Positivo si falta, negativo si ya venció.
     */
    public long getDiasVencidos() {
        if (this.fechaVencimiento == null)
            return 0;
        return ChronoUnit.DAYS.between(this.fechaVencimiento, LocalDate.now());
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
        if (this.fechaVencimiento == null)
            return Long.MAX_VALUE; // O algún valor grande
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