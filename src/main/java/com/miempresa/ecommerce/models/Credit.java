package com.miempresa.ecommerce.models;

import com.miempresa.ecommerce.models.enums.EstadoCredito;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD: CRÉDITO
 * 
 * Representa un crédito (venta a cuotas).
 * Se crea cuando una venta es de tipo CREDITO.
 * 
 * El crédito tiene:
 * - Monto total a pagar
 * - Número de cuotas
 * - Lista de cuotas con fechas de vencimiento
 * - Monto pendiente
 */

@Entity
@Table(name = "creditos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credit {

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
     * Venta asociada al crédito
     * Relación uno a uno: una venta tiene un crédito
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    @ToString.Exclude
    private Sale venta;

    /**
     * Cliente que debe el crédito
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Customer cliente;

    /**
     * Lista de cuotas del crédito
     */
    @OneToMany(mappedBy = "credito", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroCuota ASC")
    @Builder.Default
    private List<Installment> cuotas = new ArrayList<>();

    // ========================================
    // INFORMACIÓN DEL CRÉDITO
    // ========================================

    /**
     * Monto total del crédito
     */
    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    /**
     * Monto pendiente por pagar
     * Se actualiza con cada abono
     */
    @Column(name = "monto_pendiente", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPendiente;

    /**
     * Número de cuotas
     * Máximo 24 cuotas
     */
    @Column(name = "num_cuotas", nullable = false)
    private Integer numCuotas;

    /**
     * Monto de cada cuota (todas iguales)
     */
    @Column(name = "monto_cuota", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoCuota;

    /**
     * Fecha de inicio del crédito
     */
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    /**
     * Estado del crédito (ACTIVO, COMPLETADO, ANULADO)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCredito estado = EstadoCredito.ACTIVO;

    // ========================================
    // AUDITORÍA
    // ========================================

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Agrega una cuota al crédito
     */
    public void agregarCuota(Installment cuota) {
        this.cuotas.add(cuota);
        cuota.setCredito(this);
    }

    /**
     * Genera las cuotas del crédito
     * Crea todas las cuotas con fechas de vencimiento cada 30 días
     */
    public void generarCuotas() {
        this.cuotas.clear();

        for (int i = 1; i <= this.numCuotas; i++) {
            Installment cuota = Installment.builder()
                    .credito(this)
                    .numeroCuota(i)
                    .monto(this.montoCuota)
                    .montoPagado(BigDecimal.ZERO)
                    .montoPendiente(this.montoCuota)
                    .fechaVencimiento(this.fechaInicio.plusMonths(i))
                    .build();

            this.agregarCuota(cuota);
        }
    }

    /**
     * Calcula el monto de cada cuota
     */
    public void calcularMontoCuota() {
        if (this.montoTotal != null && this.numCuotas != null && this.numCuotas > 0) {
            this.montoCuota = this.montoTotal
                    .divide(BigDecimal.valueOf(this.numCuotas), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * Aplica un pago al crédito (distribución proporcional)
     * 
     * @param montoPago Monto del pago a distribuir
     */
    public void aplicarPago(BigDecimal montoPago) {
        if (montoPago == null || montoPago.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Distribuir proporcionalmente entre todas las cuotas pendientes
        List<Installment> cuotasPendientes = this.cuotas.stream()
                .filter(c -> c.getMontoPendiente().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (cuotasPendientes.isEmpty()) {
            return;
        }

        // Calcular monto por cuota
        BigDecimal montoPorCuota = montoPago
                .divide(BigDecimal.valueOf(cuotasPendientes.size()), 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal montoRestante = montoPago;

        // Aplicar a cada cuota
        for (Installment cuota : cuotasPendientes) {
            BigDecimal montoAAplicar = montoPorCuota.min(cuota.getMontoPendiente()).min(montoRestante);
            cuota.aplicarPago(montoAAplicar);
            montoRestante = montoRestante.subtract(montoAAplicar);

            if (montoRestante.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }

        // Actualizar monto pendiente del crédito
        this.montoPendiente = this.cuotas.stream()
                .map(Installment::getMontoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Si no hay monto pendiente, marcar como completado
        if (this.montoPendiente.compareTo(BigDecimal.ZERO) == 0) {
            this.estado = EstadoCredito.COMPLETADO;
        }
    }

    /**
     * Obtiene el número de cuotas pagadas
     */
    public long getCuotasPagadas() {
        return this.cuotas.stream()
                .filter(Installment::estaPagada)
                .count();
    }

    /**
     * Obtiene el número de cuotas pendientes
     */
    public long getCuotasPendientes() {
        return this.cuotas.stream()
                .filter(c -> !c.estaPagada())
                .count();
    }

    /**
     * Obtiene el número de cuotas vencidas
     */
    public long getCuotasVencidas() {
        return this.cuotas.stream()
                .filter(Installment::estaVencida)
                .count();
    }

    /**
     * Verifica si el crédito está activo
     */
    public boolean estaActivo() {
        return this.estado == EstadoCredito.ACTIVO;
    }

    /**
     * Verifica si el crédito está completado
     */
    public boolean estaCompletado() {
        return this.estado == EstadoCredito.COMPLETADO;
    }

    /**
     * Anula el crédito
     */
    public void anular() {
        this.estado = EstadoCredito.ANULADO;
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Cómo se crea un crédito?
 * 
 * Ejemplo: Venta de S/ 1200 en 12 cuotas
 * 
 * Paso 1: Crear el crédito
 * ```java
 * Credit credito = Credit.builder()
 * .venta(venta)
 * .cliente(cliente)
 * .montoTotal(new BigDecimal("1200"))
 * .montoPendiente(new BigDecimal("1200"))
 * .numCuotas(12)
 * .fechaInicio(LocalDate.now())
 * .build();
 * 
 * credito.calcularMontoCuota(); // 1200 ÷ 12 = 100
 * credito.generarCuotas(); // Crea 12 cuotas de S/ 100 c/u
 * ```
 * 
 * Paso 2: Se crean automáticamente 12 cuotas:
 * - Cuota 1: S/ 100, vence en 30 días
 * - Cuota 2: S/ 100, vence en 60 días
 * - ...
 * - Cuota 12: S/ 100, vence en 360 días
 * 
 * 2. ¿Cómo funciona el método aplicarPago()?
 * 
 * Ejemplo: Cliente abona S/ 300
 * 
 * Crédito tiene 12 cuotas de S/ 100 cada una (todas pendientes)
 * 
 * Se distribuye proporcionalmente:
 * S/ 300 ÷ 12 = S/ 25 por cuota
 * 
 * Resultado:
 * - Cuota 1: pendiente S/ 75 (pagó S/ 25)
 * - Cuota 2: pendiente S/ 75
 * - ...
 * - Cuota 12: pendiente S/ 75
 * 
 * Monto pendiente del crédito: S/ 900
 * 
 * 3. ¿Cuándo se marca como COMPLETADO?
 * - Cuando montoPendiente = 0
 * - Todas las cuotas están pagadas
 * 
 * 4. Relación con otras tablas:
 * - 1 Venta → 1 Crédito (si es venta a crédito)
 * - 1 Crédito → N Cuotas (12 cuotas, 24 cuotas, etc.)
 * - 1 Crédito → N Pagos (abonos del cliente)
 */