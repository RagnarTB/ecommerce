package com.miempresa.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Importar
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
import java.util.Objects;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Entity
@Table(name = "creditos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    @ToString.Exclude
    private Sale venta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Customer cliente;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonManagedReference("credit-installments")
    @OneToMany(mappedBy = "credito", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroCuota ASC")
    @Builder.Default
    private List<Installment> cuotas = new ArrayList<>();

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "monto_pendiente", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPendiente;

    @Column(name = "num_cuotas", nullable = false)
    private Integer numCuotas;

    @Column(name = "monto_cuota", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoCuota;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCredito estado = EstadoCredito.ACTIVO;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Métodos útiles (sin cambios)...
    // <<--- CÓDIGO DE MÉTODOS ÚTILES OMITIDO POR BREVEDAD --->>
    /**
     * Agrega una cuota al crédito
     */
    public void agregarCuota(Installment cuota) {
        if (this.cuotas == null)
            this.cuotas = new ArrayList<>();
        this.cuotas.add(cuota);
        cuota.setCredito(this);
    }

    /**
     * Genera las cuotas del crédito
     * Crea todas las cuotas con fechas de vencimiento cada 30 días
     */
    public void generarCuotas() {
        if (this.numCuotas == null || this.numCuotas <= 0 || this.montoCuota == null || this.fechaInicio == null) {
            log.warn("No se pueden generar cuotas: datos incompletos en el crédito.");
            return; // O lanzar excepción
        }
        if (this.cuotas == null)
            this.cuotas = new ArrayList<>();
        this.cuotas.clear();

        for (int i = 1; i <= this.numCuotas; i++) {
            Installment cuota = Installment.builder()
                    .credito(this)
                    .numeroCuota(i)
                    .monto(this.montoCuota)
                    .montoPagado(BigDecimal.ZERO)
                    .montoPendiente(this.montoCuota)
                    .fechaVencimiento(this.fechaInicio.plusMonths(i)) // Vencimiento mensual
                    .estado(com.miempresa.ecommerce.models.enums.EstadoCuota.PENDIENTE) // Estado inicial
                    .build();
            this.agregarCuota(cuota);
        }
        // Asegurarse de que el monto pendiente inicial sea el total
        this.montoPendiente = this.montoTotal;
    }

    /**
     * Calcula el monto de cada cuota
     */
    public void calcularMontoCuota() {
        if (this.montoTotal != null && this.numCuotas != null && this.numCuotas > 0) {
            this.montoCuota = this.montoTotal
                    .divide(BigDecimal.valueOf(this.numCuotas), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.montoCuota = BigDecimal.ZERO;
        }
    }

    /**
     * Aplica un pago al crédito, priorizando cuotas vencidas y luego las más
     * antiguas.
     * Crea los registros PaymentInstallment para trazabilidad.
     *
     * @param pago El objeto Payment que representa el abono.
     * @return Lista de PaymentInstallment creados.
     */
    @Transactional // Asegura atomicidad
    public List<com.miempresa.ecommerce.models.PaymentInstallment> aplicarPagoConDetalle(Payment pago) {
        BigDecimal montoAplicar = pago.getMonto();
        List<com.miempresa.ecommerce.models.PaymentInstallment> distribucion = new ArrayList<>();

        if (montoAplicar == null || montoAplicar.compareTo(BigDecimal.ZERO) <= 0) {
            return distribucion;
        }

        // 1. Obtener cuotas ordenadas: vencidas primero, luego por número de cuota
        List<Installment> cuotasOrdenadas = this.cuotas.stream()
                .filter(c -> c.getMontoPendiente() != null && c.getMontoPendiente().compareTo(BigDecimal.ZERO) > 0)
                .sorted((c1, c2) -> {
                    boolean c1Vencida = c1.estaVencida();
                    boolean c2Vencida = c2.estaVencida();
                    if (c1Vencida && !c2Vencida)
                        return -1; // Vencidas primero
                    if (!c1Vencida && c2Vencida)
                        return 1;
                    // Si ambas son vencidas o ninguna lo es, ordenar por fecha de vencimiento
                    int cmpFecha = c1.getFechaVencimiento().compareTo(c2.getFechaVencimiento());
                    if (cmpFecha != 0)
                        return cmpFecha;
                    // Si vencen el mismo día, ordenar por número de cuota
                    return c1.getNumeroCuota().compareTo(c2.getNumeroCuota());
                })
                .toList();

        // 2. Aplicar el monto a las cuotas en orden
        for (Installment cuota : cuotasOrdenadas) {
            if (montoAplicar.compareTo(BigDecimal.ZERO) <= 0) {
                break; // No hay más monto para aplicar
            }

            BigDecimal montoAAplicarEnCuota = montoAplicar.min(cuota.getMontoPendiente());

            // Aplicar pago a la cuota
            cuota.aplicarPago(montoAAplicarEnCuota);

            // Crear registro de distribución
            com.miempresa.ecommerce.models.PaymentInstallment detallePago = com.miempresa.ecommerce.models.PaymentInstallment
                    .builder()
                    .pago(pago)
                    .cuota(cuota)
                    .montoAplicado(montoAAplicarEnCuota)
                    .build();
            distribucion.add(detallePago);
            // ¡Importante! Añadir al lado inverso de la relación si no se guarda
            // automáticamente
            // pago.agregarDistribucion(detallePago); // <-- Asegúrate de que esto se guarde

            montoAplicar = montoAplicar.subtract(montoAAplicarEnCuota);
        }

        // 3. Recalcular monto pendiente del crédito
        recalcularMontoPendiente();

        // 4. Actualizar estado del crédito si ya se pagó todo
        if (this.montoPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            this.estado = EstadoCredito.COMPLETADO;
            log.info("Crédito ID {} marcado como COMPLETADO", this.id);
        }

        // Persistir cambios en las cuotas (si no se hace por cascada)
        // this.cuotas.forEach(installmentRepository::save); // Si es necesario

        return distribucion; // Devolver los detalles creados
    }

    /**
     * Recalcula el monto pendiente sumando los pendientes de las cuotas.
     */
    public void recalcularMontoPendiente() {
        if (this.cuotas == null || this.cuotas.isEmpty()) {
            this.montoPendiente = this.montoTotal;
            return;
        }
        this.montoPendiente = this.cuotas.stream()
                .map(Installment::getMontoPendiente)
                .filter(Objects::nonNull) // Evitar NPE si alguna cuota no tiene monto pendiente
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP); // Asegurar escala
    }

    /**
     * Obtiene el número de cuotas pagadas
     */
    public long getCuotasPagadas() {
        if (this.cuotas == null)
            return 0;
        return this.cuotas.stream()
                .filter(Installment::estaPagada)
                .count();
    }

    /**
     * Obtiene el número de cuotas pendientes
     */
    public long getCuotasPendientes() {
        if (this.cuotas == null)
            return this.numCuotas != null ? this.numCuotas : 0;
        return this.cuotas.stream()
                .filter(c -> !c.estaPagada())
                .count();
    }

    /**
     * Obtiene el número de cuotas vencidas
     */
    public long getCuotasVencidas() {
        if (this.cuotas == null)
            return 0;
        return this.cuotas.stream()
                .filter(Installment::estaVencida)
                .count();
    }

    /**
     * Verifica si el crédito tiene alguna cuota vencida.
     */
    public boolean getTieneVencidas() {
        return getCuotasVencidas() > 0;
    }

    /**
     * Obtiene la fecha del próximo vencimiento de una cuota pendiente.
     */
    public LocalDate getProximoVencimiento() {
        if (this.cuotas == null)
            return null;
        return this.cuotas.stream()
                .filter(c -> c.getEstado() == com.miempresa.ecommerce.models.enums.EstadoCuota.PENDIENTE ||
                        c.getEstado() == com.miempresa.ecommerce.models.enums.EstadoCuota.PAGADA_PARCIAL ||
                        c.getEstado() == com.miempresa.ecommerce.models.enums.EstadoCuota.VENCIDA) // Incluir vencidas
                                                                                                   // también
                .map(Installment::getFechaVencimiento)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
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

    // Logger para métodos internos si es necesario
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Credit.class);

}