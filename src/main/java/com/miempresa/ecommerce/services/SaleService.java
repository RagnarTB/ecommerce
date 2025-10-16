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
import com.miempresa.ecommerce.repositories.UserRepository;

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
    private final UserRepository userRepository;

    // ========================================
    // CREAR VENTA
    // ========================================

    /**
     * Crea una nueva venta
     * 
     * Pasos:
     * 1. Genera número de venta
     * 2. Calcula totales
     * 3. Descuenta stock
     * 4. Registra movimientos de inventario
     * 5. Si es crédito, crea cuotas
     * 6. Registra pagos
     */
    public Sale crearVenta(Sale venta, List<SaleDetail> detalles,
            List<Payment> pagos, User usuario) {
        log.info("Creando nueva venta");

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
                    venta.getId(), usuario);
        }

        // Guardar venta
        Sale ventaGuardada = saleRepository.save(venta);

        // 5. Si es crédito, crear cuotas
        if (venta.getTipoPago() == TipoPago.CREDITO) {
            crearCredito(ventaGuardada, pagos.isEmpty() ? null : pagos.get(0));
        }

        // 6. Registrar pagos
        for (Payment pago : pagos) {
            pago.setVenta(ventaGuardada);
            pago.setUsuario(usuario);
            ventaGuardada.agregarPago(pago);
        }

        saleRepository.save(ventaGuardada);

        log.info("Venta creada: {}", ventaGuardada.getNumeroVenta());
        return ventaGuardada;
    }

    /**
     * Crea un crédito con cuotas
     */
    private Credit crearCredito(Sale venta, Payment pagoInicial) {
        log.info("Creando crédito para venta: {}", venta.getNumeroVenta());

        // Determinar número de cuotas (default 12 si no está especificado)
        int numCuotas = 12; // Esto debería venir como parámetro

        Credit credito = Credit.builder()
                .venta(venta)
                .cliente(venta.getCliente())
                .montoTotal(venta.getTotal())
                .montoPendiente(venta.getTotal())
                .numCuotas(numCuotas)
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
    // ANULAR VENTA
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
    // UTILIDADES PRIVADAS
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
     * Registra un movimiento de inventario
     */
    private void registrarMovimientoInventario(Product producto, Integer cantidad,
            TipoMovimiento tipo, MotivoMovimiento motivo,
            Long referenciaId, User usuario) {

        Integer stockAnterior = producto.getStockActual();
        Integer stockNuevo = tipo == TipoMovimiento.ENTRADA
                ? stockAnterior + cantidad
                : stockAnterior - cantidad;

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
}

/**
 * EXPLICACIÓN DEL PROCESO DE VENTA:
 * 
 * 1. Flujo de Venta al CONTADO:
 * - Cliente compra productos
 * - Se descuenta stock inmediatamente
 * - Se registran pagos (puede ser multipago)
 * - Se genera PDF de boleta
 * 
 * 2. Flujo de Venta a CRÉDITO:
 * - Cliente compra productos
 * - Se descuenta stock inmediatamente
 * - Se crea crédito con N cuotas
 * - Cuotas con vencimiento cada 30 días
 * - Cliente puede abonar cuando quiera
 * - Abonos se distribuyen proporcionalmente
 * 
 * 3. Anulación de Venta:
 * - Solo si no tiene pagos (crédito sin abonos)
 * - Devuelve stock automáticamente
 * - Registra movimientos de inventario
 * - Marca venta como ANULADA
 * 
 * 4. Movimientos de Inventario:
 * - VENTA → SALIDA
 * - DEVOLUCIÓN → ENTRADA
 * - Guarda stock anterior y nuevo
 * - Trazabilidad total
 */