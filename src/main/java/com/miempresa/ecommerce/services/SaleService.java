package com.miempresa.ecommerce.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miempresa.ecommerce.models.Credit;
import com.miempresa.ecommerce.models.InventoryMovement;
import com.miempresa.ecommerce.models.Payment;
import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.models.Sale;
import com.miempresa.ecommerce.models.SaleDetail;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.models.enums.EstadoCredito;
import com.miempresa.ecommerce.models.enums.EstadoVenta;
import com.miempresa.ecommerce.models.enums.MetodoPago;
import com.miempresa.ecommerce.models.enums.MotivoMovimiento;
import com.miempresa.ecommerce.models.enums.TipoMovimiento;
import com.miempresa.ecommerce.models.enums.TipoPago;
import com.miempresa.ecommerce.repositories.CreditRepository;
import com.miempresa.ecommerce.repositories.InventoryMovementRepository;
import com.miempresa.ecommerce.repositories.PaymentRepository;
import com.miempresa.ecommerce.repositories.ProductRepository;
import com.miempresa.ecommerce.repositories.SaleDetailRepository;
import com.miempresa.ecommerce.repositories.SaleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SERVICE: VENTA
 * 
 * Gestiona ventas, pagos y créditos.
 * El servicio más complejo del sistema.
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final PaymentRepository paymentRepository;
    private final CreditRepository creditRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    // ========================================
    // CREAR VENTA - ✅ CORREGIDO
    // ========================================

    /**
     * Crea una nueva venta
     * 
     * @param venta     Venta a crear
     * @param detalles  Lista de productos vendidos
     * @param pagos     Lista de pagos realizados
     * @param usuario   Usuario que registra la venta
     * @param numCuotas Número de cuotas (obligatorio si es a crédito)
     * @return Venta creada
     */
    @Transactional(rollbackFor = Exception.class) // ✅ AGREGADO
    public Sale crearVenta(Sale venta, List<SaleDetail> detalles,
            List<Payment> pagos, User usuario, Integer numCuotas) { // ✅ PARÁMETRO AGREGADO
        log.info("Creando nueva venta");

        // ✅ VALIDACIÓN AGREGADA
        if (venta.getTipoPago() == TipoPago.CREDITO) {
            if (numCuotas == null || numCuotas < 1 || numCuotas > 24) {
                throw new RuntimeException("Para ventas a crédito debe especificar entre 1 y 24 cuotas");
            }
        }

        // 1. Generar número de venta
        venta.setNumeroVenta(generarNumeroVenta());
        venta.setUsuario(usuario);
        venta.setEstado(EstadoVenta.COMPLETADA);

        // 2. Agregar detalles y calcular totales
        for (SaleDetail detalle : detalles) {
            detalle.establecerDatosProducto();
            detalle.calcularSubtotal();
            venta.agregarDetalle(detalle);
        }

        venta.calcularTotal();

        // 3. Validar y descontar stock
        for (SaleDetail detalle : detalles) {
            Product producto = detalle.getProducto();

            if (!producto.disminuirStock(detalle.getCantidad())) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            productRepository.save(producto);

            // 4. Registrar movimiento de inventario
            registrarMovimientoInventario(producto, detalle.getCantidad(),
                    TipoMovimiento.SALIDA, MotivoMovimiento.VENTA,
                    null, usuario); // El ID de venta se actualizará después
        }

        // Guardar venta primero para obtener el ID
        Sale ventaGuardada = saleRepository.save(venta);

        // 5. Si es crédito, crear cuotas
        if (venta.getTipoPago() == TipoPago.CREDITO) {
            crearCredito(ventaGuardada, pagos.isEmpty() ? null : pagos.get(0), numCuotas); // ✅ PARÁMETRO AGREGADO
        }

        // 6. Registrar pagos
        for (Payment pago : pagos) {
            pago.setVenta(ventaGuardada);
            pago.setUsuario(usuario);
            ventaGuardada.agregarPago(pago);
        }

        saleRepository.save(ventaGuardada);

        // 7. Actualizar referencias en movimientos de inventario
        for (SaleDetail detalle : ventaGuardada.getDetalles()) {
            actualizarReferenciaMovimiento(detalle.getProducto(), ventaGuardada.getId());
        }

        log.info("Venta creada: {}", ventaGuardada.getNumeroVenta());
        return ventaGuardada;
    }

    /**
     * Crea un crédito con cuotas - ✅ CORREGIDO
     */
    private Credit crearCredito(Sale venta, Payment pagoInicial, int numCuotas) { // ✅ PARÁMETRO AGREGADO
        log.info("Creando crédito para venta: {} con {} cuotas", venta.getNumeroVenta(), numCuotas);

        Credit credito = Credit.builder()
                .venta(venta)
                .cliente(venta.getCliente())
                .montoTotal(venta.getTotal())
                .montoPendiente(venta.getTotal())
                .numCuotas(numCuotas) // ✅ AHORA USA EL PARÁMETRO
                .fechaInicio(LocalDate.now())
                .estado(EstadoCredito.ACTIVO)
                .build();

        // Calcular monto de cada cuota
        credito.calcularMontoCuota();

        // Generar cuotas
        credito.generarCuotas();

        // Si hay pago inicial, aplicarlo
        if (pagoInicial != null) {
            credito.aplicarPago(pagoInicial.getMonto());
        }

        return creditRepository.save(credito);
    }

    // ========================================
    // ANULAR VENTA - ✅ MEJORADO
    // ========================================

    /**
     * Anula una venta
     * 
     * Pasos:
     * 1. Cambiar estado a ANULADA
     * 2. Devolver stock
     * 3. Registrar movimientos de inventario
     * 4. Si es crédito, anular crédito
     */
    @Transactional(rollbackFor = Exception.class) // ✅ AGREGADO
    public void anularVenta(Long ventaId, User usuario) {
        log.info("Anulando venta ID: {}", ventaId);

        Optional<Sale> ventaOpt = buscarPorId(ventaId);

        if (ventaOpt.isEmpty()) {
            throw new RuntimeException("Venta no encontrada");
        }

        Sale venta = ventaOpt.get();

        // Verificar que esté completada
        if (venta.getEstado() != EstadoVenta.COMPLETADA) {
            throw new RuntimeException("Solo se pueden anular ventas completadas");
        }

        // Si es a crédito y tiene pagos, verificar
        if (venta.getTipoPago() == TipoPago.CREDITO) {
            Optional<Credit> creditoOpt = creditRepository.findByVentaId(ventaId);

            if (creditoOpt.isPresent()) {
                Credit credito = creditoOpt.get();

                // Si tiene pagos, no se puede anular sin devolución
                if (credito.getMontoTotal().compareTo(credito.getMontoPendiente()) != 0) {
                    throw new RuntimeException("El crédito tiene abonos. Debe devolver el dinero antes de anular");
                }

                // Anular crédito
                credito.anular();
                creditRepository.save(credito);
            }
        }

        // Devolver stock
        for (SaleDetail detalle : venta.getDetalles()) {
            Product producto = detalle.getProducto();
            producto.aumentarStock(detalle.getCantidad());
            productRepository.save(producto);

            // Registrar movimiento
            registrarMovimientoInventario(producto, detalle.getCantidad(),
                    TipoMovimiento.ENTRADA, MotivoMovimiento.DEVOLUCION,
                    ventaId, usuario);
        }

        // Anular venta
        venta.anular();
        saleRepository.save(venta);

        log.info("Venta anulada: {}", venta.getNumeroVenta());
    }

    // ========================================
    // ABONOS A CRÉDITO
    // ========================================

    /**
     * Registra un abono a un crédito
     */
    public Payment registrarAbono(Long creditoId, BigDecimal monto, MetodoPago metodoPago,
            String referencia, User usuario) {
        log.info("Registrando abono de {} al crédito ID: {}", monto, creditoId);

        Optional<Credit> creditoOpt = creditRepository.findById(creditoId);

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

    // ========================================
    // BÚSQUEDAS
    // ========================================

    @Transactional(readOnly = true)
    public Optional<Sale> buscarPorId(Long id) {
        return saleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Sale> buscarPorNumero(String numeroVenta) {
        return saleRepository.findByNumeroVenta(numeroVenta);
    }

    @Transactional(readOnly = true)
    public List<Sale> obtenerTodas() {
        return saleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Sale> obtenerDelDia() {
        return saleRepository.obtenerVentasDelDia();
    }

    @Transactional(readOnly = true)
    public List<Sale> obtenerDelMes() {
        return saleRepository.obtenerVentasDelMes();
    }

    @Transactional(readOnly = true)
    public List<Sale> buscarPorCliente(Long clienteId) {
        return saleRepository.findByClienteIdOrderByFechaVentaDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Sale> buscarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return saleRepository.findByFechaVentaBetweenOrderByFechaVentaDesc(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalVentasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return saleRepository.sumarVentasPorFecha(fechaInicio, fechaFin);
    }

    // ========================================
    // UTILIDADES PRIVADAS - ✅ MEJORADAS
    // ========================================

    /**
     * Genera un número único de venta
     * Formato: VEN-2025-00001
     */
    private String generarNumeroVenta() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String prefijo = "VEN-" + year + "-";

        Long siguiente = saleRepository.generarSiguienteNumero(prefijo + "%");

        return String.format("%s%05d", prefijo, siguiente);
    }

    /**
     * Registra un movimiento de inventario - ✅ MEJORADO
     */
    private void registrarMovimientoInventario(Product producto, Integer cantidad,
            TipoMovimiento tipo, MotivoMovimiento motivo,
            Long referenciaId, User usuario) {

        Integer stockAnterior = tipo == TipoMovimiento.ENTRADA
                ? producto.getStockActual() - cantidad
                : producto.getStockActual() + cantidad;

        Integer stockNuevo = producto.getStockActual();

        InventoryMovement movimiento = InventoryMovement.builder()
                .producto(producto)
                .usuario(usuario)
                .tipo(tipo)
                .motivo(motivo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .referenciaId(referenciaId)
                .referenciaTipo("VENTA")
                .build();

        inventoryMovementRepository.save(movimiento);
    }

    /**
     * Actualiza la referencia en movimientos de inventario - ✅ NUEVO
     */
    private void actualizarReferenciaMovimiento(Product producto, Long ventaId) {
        List<InventoryMovement> movimientos = inventoryMovementRepository
                .findByProductoIdOrderByFechaMovimientoDesc(producto.getId());

        // Actualizar el último movimiento sin referencia
        movimientos.stream()
                .filter(m -> m.getReferenciaId() == null && m.getMotivo() == MotivoMovimiento.VENTA)
                .findFirst()
                .ifPresent(m -> {
                    m.setReferenciaId(ventaId);
                    inventoryMovementRepository.save(m);
                });
    }
}