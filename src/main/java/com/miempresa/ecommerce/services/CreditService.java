package com.miempresa.ecommerce.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miempresa.ecommerce.models.Credit;
import com.miempresa.ecommerce.models.Installment;
import com.miempresa.ecommerce.models.Payment;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.models.enums.EstadoCredito;
import com.miempresa.ecommerce.models.enums.MetodoPago;
import com.miempresa.ecommerce.repositories.CreditRepository;
import com.miempresa.ecommerce.repositories.InstallmentRepository;
import com.miempresa.ecommerce.repositories.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SERVICE: CRÉDITO
 * 
 * Gestiona créditos y cuotas.
 * Nota: La creación de créditos se hace desde SaleService
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CreditService {

    private final CreditRepository creditRepository;
    private final InstallmentRepository installmentRepository;
    private final PaymentRepository paymentRepository;

    // ========================================
    // CONSULTAS DE CRÉDITOS
    // ========================================

    @Transactional(readOnly = true)
    public Optional<Credit> buscarPorId(Long id) {
        return creditRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Credit> buscarPorVenta(Long ventaId) {
        return creditRepository.findByVentaId(ventaId);
    }

    @Transactional(readOnly = true)
    public List<Credit> obtenerTodos() {
        return creditRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Credit> obtenerActivos() {
        return creditRepository.findByEstadoOrderByFechaCreacionDesc(EstadoCredito.ACTIVO);
    }

    @Transactional(readOnly = true)
    public List<Credit> obtenerPorCliente(Long clienteId) {
        return creditRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Credit> obtenerCreditosConCuotasVencidas() {
        return creditRepository.obtenerCreditosConCuotasVencidas(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Credit> obtenerCreditosConCuotasProximasAVencer(int dias) {
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(dias);

        return creditRepository.obtenerCreditosConCuotasProximasAVencer(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalDeudaPendiente() {
        return creditRepository.sumarMontoPendienteTotal();
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerDeudaCliente(Long clienteId) {
        return creditRepository.sumarMontoPendientePorCliente(clienteId);
    }

    // ========================================
    // CONSULTAS DE CUOTAS
    // ========================================

    @Transactional(readOnly = true)
    public List<Installment> obtenerCuotasDeCredito(Long creditoId) {
        return installmentRepository.findByCreditoIdOrderByNumeroCuotaAsc(creditoId);
    }

    @Transactional(readOnly = true)
    public List<Installment> obtenerCuotasVencidas() {
        return installmentRepository.obtenerCuotasVencidas(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Installment> obtenerCuotasQueVencenHoy() {
        return installmentRepository.obtenerCuotasQueVencenHoy(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Installment> obtenerCuotasProximasAVencer(int dias) {
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(dias);

        return installmentRepository.obtenerCuotasProximasAVencer(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public long contarCuotasVencidas() {
        return installmentRepository.contarCuotasVencidas();
    }

    @Transactional(readOnly = true)
    public BigDecimal sumarMontoPendienteCuotasVencidas() {
        return installmentRepository.sumarMontoPendienteCuotasVencidas();
    }

    // ========================================
    // OPERACIONES
    // ========================================

    /**
     * Actualiza el estado de todas las cuotas vencidas
     */
    public void actualizarEstadoCuotasVencidas() {
        log.info("Actualizando estado de cuotas vencidas");

        List<Installment> cuotasVencidas = obtenerCuotasVencidas();

        for (Installment cuota : cuotasVencidas) {
            cuota.actualizarEstado();
            installmentRepository.save(cuota);
        }

        log.info("Se actualizaron {} cuotas vencidas", cuotasVencidas.size());
    }

    /**
     * Anula un crédito
     */
    public void anularCredito(Long creditoId) {
        log.info("Anulando crédito ID: {}", creditoId);

        Optional<Credit> creditoOpt = buscarPorId(creditoId);

        if (creditoOpt.isEmpty()) {
            throw new RuntimeException("Crédito no encontrado");
        }

        Credit credito = creditoOpt.get();

        // Verificar que no tenga pagos
        if (credito.getMontoTotal().compareTo(credito.getMontoPendiente()) != 0) {
            throw new RuntimeException("No se puede anular un crédito con pagos realizados");
        }

        credito.anular();
        creditRepository.save(credito);

        log.info("Crédito anulado");
    }

    /**
     * Registra un abono a un crédito
     */
    public Payment registrarAbono(Long creditoId, BigDecimal monto,
            MetodoPago metodoPago,
            String referencia, User usuario) {

        log.info("Registrando abono de {} al crédito ID: {}", monto, creditoId);

        Optional<Credit> creditoOpt = buscarPorId(creditoId);

        if (creditoOpt.isEmpty()) {
            throw new RuntimeException("Crédito no encontrado");
        }

        Credit credito = creditoOpt.get();

        // Validar que esté activo
        if (!credito.estaActivo()) {
            throw new RuntimeException("El crédito no está activo");
        }

        // Validar monto
        if (monto.compareTo(credito.getMontoPendiente()) > 0) {
            throw new RuntimeException("El monto supera la deuda pendiente");
        }

        // Crear pago
        Payment pago = Payment.builder()
                .venta(credito.getVenta())
                .credito(credito)
                .monto(monto)
                .metodoPago(metodoPago)
                .referencia(referencia)
                .usuario(usuario)
                .build();

        paymentRepository.save(pago);

        // Aplicar pago al crédito (distribución proporcional)
        credito.aplicarPago(monto);
        creditRepository.save(credito);

        log.info("Abono registrado exitosamente");
        return pago;
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return creditRepository.countByEstado(EstadoCredito.ACTIVO);
    }
}

/**
 * EXPLICACIÓN DEL SISTEMA DE CRÉDITOS:
 * 
 * 1. Creación de Crédito:
 * - Se crea desde SaleService cuando tipo_pago = CREDITO
 * - Se generan N cuotas automáticamente
 * - Cada cuota vence cada 30 días
 * 
 * 2. Estados de Crédito:
 * - ACTIVO: Tiene cuotas pendientes
 * - COMPLETADO: Todas las cuotas pagadas
 * - ANULADO: Crédito cancelado
 * 
 * 3. Estados de Cuota:
 * - PENDIENTE: No pagada, no vencida
 * - PAGADA_PARCIAL: Tiene abonos parciales
 * - PAGADA: Completamente pagada
 * - VENCIDA: Fecha pasó y no está pagada
 * 
 * 4. Alertas importantes:
 * - Cuotas vencidas: para cobro inmediato
 * - Cuotas próximas a vencer: para recordatorios
 * - Total de deuda pendiente: para reportes
 * 
 * 5. Flujo de pago:
 * - Cliente abona X monto
 * - Se distribuye proporcionalmente entre cuotas
 * - Se actualizan estados
 * - Si monto_pendiente = 0 → Crédito COMPLETADO
 */