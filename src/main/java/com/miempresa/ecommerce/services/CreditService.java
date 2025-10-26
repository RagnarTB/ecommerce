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

    private final CreditRepository creditRepository; // [cite: 312]
    private final InstallmentRepository installmentRepository; // [cite: 321]
    private final PaymentRepository paymentRepository; // [cite: 339]

    // ========================================
    // CONSULTAS DE CRÉDITOS
    // ========================================

    @Transactional(readOnly = true)
    public Optional<Credit> buscarPorId(Long id) {
        return creditRepository.findById(id); // [cite: 312]
    }

    @Transactional(readOnly = true)
    public Optional<Credit> buscarPorVenta(Long ventaId) {
        return creditRepository.findByVentaId(ventaId); // [cite: 312]
    }

    @Transactional(readOnly = true)
    public List<Credit> obtenerTodos() {
        return creditRepository.findAll(); // [cite: 312]
    }

    @Transactional(readOnly = true)
    public List<Credit> obtenerActivos() {
        return creditRepository.findByEstadoOrderByFechaCreacionDesc(EstadoCredito.ACTIVO); // [cite: 312]
    }

    @Transactional(readOnly = true)
    public List<Credit> obtenerPorCliente(Long clienteId) {
        return creditRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId); // [cite: 312]
    }

    @Transactional(readOnly = true)
    public List<Credit> obtenerCreditosConCuotasVencidas() {
        return creditRepository.obtenerCreditosConCuotasVencidas(LocalDate.now()); // [cite: 314]
    }

    @Transactional(readOnly = true)
    public List<Credit> obtenerCreditosConCuotasProximasAVencer(int dias) {
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(dias);

        return creditRepository.obtenerCreditosConCuotasProximasAVencer(fechaInicio, fechaFin); // [cite: 315]
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalDeudaPendiente() {
        return creditRepository.sumarMontoPendienteTotal(); // [cite: 315]
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerDeudaCliente(Long clienteId) {
        return creditRepository.sumarMontoPendientePorCliente(clienteId); // [cite: 316]
    }

    // ========================================
    // CONSULTAS DE CUOTAS
    // ========================================

    @Transactional(readOnly = true)
    public List<Installment> obtenerCuotasDeCredito(Long creditoId) {
        return installmentRepository.findByCreditoIdOrderByNumeroCuotaAsc(creditoId); // [cite: 322]
    }

    @Transactional(readOnly = true)
    public List<Installment> obtenerCuotasVencidas() {
        return installmentRepository.obtenerCuotasVencidas(LocalDate.now()); // [cite: 323]
    }

    @Transactional(readOnly = true)
    public List<Installment> obtenerCuotasQueVencenHoy() {
        return installmentRepository.obtenerCuotasQueVencenHoy(LocalDate.now()); // [cite: 324]
    }

    @Transactional(readOnly = true)
    public List<Installment> obtenerCuotasProximasAVencer(int dias) {
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(dias);

        return installmentRepository.obtenerCuotasProximasAVencer(fechaInicio, fechaFin); // [cite: 325]
    }

    @Transactional(readOnly = true)
    public long contarCuotasVencidas() {
        return installmentRepository.contarCuotasVencidas(); // [cite: 327]
    }

    @Transactional(readOnly = true)
    public BigDecimal sumarMontoPendienteCuotasVencidas() {
        return installmentRepository.sumarMontoPendienteCuotasVencidas(); // [cite: 328]
    }

    // ========================================
    // OPERACIONES
    // ========================================

    /**
     * Actualiza el estado de todas las cuotas vencidas
     */
    public void actualizarEstadoCuotasVencidas() {
        log.info("Actualizando estado de cuotas vencidas");

        List<Installment> cuotasVencidas = obtenerCuotasVencidas(); // [cite: 323]

        for (Installment cuota : cuotasVencidas) {
            cuota.actualizarEstado(); // [cite: 265]
            installmentRepository.save(cuota); // [cite: 321]
        }

        log.info("Se actualizaron {} cuotas vencidas", cuotasVencidas.size());
    }

    /**
     * Anula un crédito
     */
    public void anularCredito(Long creditoId) {
        log.info("Anulando crédito ID: {}", creditoId);

        Optional<Credit> creditoOpt = buscarPorId(creditoId); // [cite: 312]

        if (creditoOpt.isEmpty()) {
            throw new RuntimeException("Crédito no encontrado");
        }

        Credit credito = creditoOpt.get();

        // Verificar que no tenga pagos
        if (credito.getMontoTotal().compareTo(credito.getMontoPendiente()) != 0) { // [cite: 246]
            throw new RuntimeException("No se puede anular un crédito con pagos realizados");
        }

        credito.anular(); // [cite: 256]
        creditRepository.save(credito); // [cite: 312]

        log.info("Crédito anulado");
    }

    /**
     * Registra un abono a un crédito
     */
    @Transactional(rollbackFor = Exception.class) // [cite: 416]
    public Payment registrarAbono(Long creditoId, BigDecimal monto, // [cite: 417]
            MetodoPago metodoPago, // [cite: 417]
            String referencia, User usuario) { // [cite: 417]

        log.info("Registrando abono de {} al crédito ID: {}", monto, creditoId);

        Optional<Credit> creditoOpt = buscarPorId(creditoId); // [cite: 312]

        if (creditoOpt.isEmpty()) {
            throw new RuntimeException("Crédito no encontrado");
        }

        Credit credito = creditoOpt.get();

        // Validar que esté activo
        if (!credito.estaActivo()) { // [cite: 256]
            throw new RuntimeException("El crédito no está activo");
        }

        // ✅ Validar monto positivo
        if (monto.compareTo(BigDecimal.ZERO) <= 0) { // [cite: 417]
            throw new RuntimeException("El monto debe ser mayor a cero");
        }

        // Validar monto
        if (monto.compareTo(credito.getMontoPendiente()) > 0) { // [cite: 417]
            throw new RuntimeException(String.format(
                    "El monto (S/ %.2f) supera la deuda pendiente (S/ %.2f)", // [cite: 418]
                    monto, credito.getMontoPendiente())); // [cite: 418]
        }

        // Crear pago
        Payment pago = Payment.builder() // [cite: 278]
                .venta(credito.getVenta()) // [cite: 278]
                .credito(credito) // [cite: 278]
                .monto(monto) // [cite: 278]
                .metodoPago(metodoPago) // [cite: 278]
                .referencia(referencia) // [cite: 278]
                .usuario(usuario) // [cite: 278]
                .build(); // [cite: 278]

        paymentRepository.save(pago); // [cite: 339]

        // <<--- CORRECCIÓN AQUÍ --->>
        // Aplicar pago al crédito llamando al método correcto en Credit
        credito.aplicarPagoConDetalle(pago); // <-- SE LLAMA A aplicarPagoConDetalle CON EL OBJETO Payment // [cite:
                                             // 249, 250, 251, 252]
        // Ya no se necesita creditRepository.save(credito) aquí porque
        // aplicarPagoConDetalle
        // modifica las cuotas y el crédito, y al ser @Transactional, se guardará al
        // final.
        // Si quieres forzar el guardado inmediato (aunque no es usualmente necesario):
        // creditRepository.save(credito);

        log.info("Abono registrado exitosamente. Nuevo saldo: S/ {}", credito.getMontoPendiente());
        return pago;
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return creditRepository.countByEstado(EstadoCredito.ACTIVO); // [cite: 317]
    }
}