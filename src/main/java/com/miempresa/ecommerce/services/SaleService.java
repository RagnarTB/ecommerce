package com.miempresa.ecommerce.services;

import java.math.BigDecimal;
import java.time.LocalDate; // Asegúrate de importar LocalDate
import java.time.LocalDateTime;
import java.util.ArrayList; // Asegúrate de importar ArrayList
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

    private final SaleRepository saleRepository;// [cite: 318]
    private final SaleDetailRepository saleDetailRepository;// [cite: 353]
    private final PaymentRepository paymentRepository;// [cite: 340]
    private final CreditRepository creditRepository;// [cite: 312]
    private final ProductRepository productRepository;// [cite: 344]
    private final InventoryMovementRepository inventoryMovementRepository;// [cite: 328]

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
    @Transactional(rollbackFor = Exception.class) //
    public Sale crearVenta(Sale venta, List<SaleDetail> detalles, //
            List<Payment> pagos, User usuario, Integer numCuotas) { //
        log.info("Creando nueva venta"); //

        // ✅ VALIDACIÓN AGREGADA
        if (venta.getTipoPago() == TipoPago.CREDITO) { // [cite: 302]
            if (numCuotas == null || numCuotas < 1 || numCuotas > 24) { //
                throw new RuntimeException("Para ventas a crédito debe especificar entre 1 y 24 cuotas"); //
            }
        }

        // 1. Generar número de venta
        venta.setNumeroVenta(generarNumeroVenta()); //
        venta.setUsuario(usuario); //
        venta.setEstado(EstadoVenta.COMPLETADA); // [cite: 228]

        // 2. Agregar detalles y calcular totales
        for (SaleDetail detalle : detalles) { //
            detalle.establecerDatosProducto(); // [cite: 304]
            detalle.calcularSubtotal(); // [cite: 304]
            venta.agregarDetalle(detalle); // [cite: 300]
        }

        venta.calcularTotal(); // [cite: 300]

        // 3. Validar y descontar stock
        for (SaleDetail detalle : detalles) { //
            Product producto = detalle.getProducto(); //

            if (!producto.disminuirStock(detalle.getCantidad())) { // [cite: 288]
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre()); //
            }

            productRepository.save(producto); //

            // 4. Registrar movimiento de inventario
            registrarMovimientoInventario(producto, detalle.getCantidad(), //
                    TipoMovimiento.SALIDA, MotivoMovimiento.VENTA, //
                    null, usuario); // El ID de venta se actualizará después
        }

        // Guardar venta primero para obtener el ID
        Sale ventaGuardada = saleRepository.save(venta); //

        // 5. Si es crédito, crear cuotas
        if (venta.getTipoPago() == TipoPago.CREDITO) { // [cite: 302]
            crearCredito(ventaGuardada, pagos.isEmpty() ? null : pagos.get(0), numCuotas); // ✅ PARÁMETRO AGREGADO
        }

        // 6. Registrar pagos
        for (Payment pago : pagos) { //
            pago.setVenta(ventaGuardada); //
            pago.setUsuario(usuario); //
            ventaGuardada.agregarPago(pago); // [cite: 300]
        }

        saleRepository.save(ventaGuardada); //

        // 7. Actualizar referencias en movimientos de inventario
        for (SaleDetail detalle : ventaGuardada.getDetalles()) { //
            actualizarReferenciaMovimiento(detalle.getProducto(), ventaGuardada.getId()); //
        }

        log.info("Venta creada: {}", ventaGuardada.getNumeroVenta()); //
        return ventaGuardada;
    }

    /**
     * Crea un crédito con cuotas - ✅ CORREGIDO
     */
    private Credit crearCredito(Sale venta, Payment pagoInicial, int numCuotas) { //
        log.info("Creando crédito para venta: {} con {} cuotas", venta.getNumeroVenta(), numCuotas); //

        Credit credito = Credit.builder() //
                .venta(venta) //
                .cliente(venta.getCliente()) //
                .montoTotal(venta.getTotal()) //
                .montoPendiente(venta.getTotal()) //
                .numCuotas(numCuotas) // ✅ AHORA USA EL PARÁMETRO
                .fechaInicio(LocalDate.now()) //
                .estado(EstadoCredito.ACTIVO) // [cite: 223]
                .build(); //

        // Calcular monto de cada cuota
        credito.calcularMontoCuota(); // [cite: 248]

        // Generar cuotas
        credito.generarCuotas(); // [cite: 247]

        Credit creditoGuardado = creditRepository.save(credito); // Guardar primero para que tenga ID

        // Si hay pago inicial, aplicarlo
        if (pagoInicial != null && pagoInicial.getMonto().compareTo(BigDecimal.ZERO) > 0) { //
            log.info("Aplicando pago inicial de S/ {} al crédito ID {}", pagoInicial.getMonto(),
                    creditoGuardado.getId());
            // Asociar el pago al crédito recién guardado
            pagoInicial.setCredito(creditoGuardado); // Asociar pago al crédito
            paymentRepository.save(pagoInicial); // Guardar el pago con la asociación al crédito

            // Ahora aplicar el pago usando el método del crédito
            creditoGuardado.aplicarPagoConDetalle(pagoInicial); // Usa el pago ya asociado
            creditRepository.save(creditoGuardado); // Guardar crédito con saldo actualizado
        }

        return creditoGuardado;
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
    @Transactional(rollbackFor = Exception.class) //
    public void anularVenta(Long ventaId, User usuario) { //
        log.info("Anulando venta ID: {}", ventaId); //

        Optional<Sale> ventaOpt = buscarPorId(ventaId); //

        if (ventaOpt.isEmpty()) {
            throw new RuntimeException("Venta no encontrada"); //
        }

        Sale venta = ventaOpt.get();

        // Verificar que esté completada
        if (venta.getEstado() != EstadoVenta.COMPLETADA) { // [cite: 228]
            throw new RuntimeException("Solo se pueden anular ventas completadas"); //
        }

        // Si es a crédito y tiene pagos, verificar
        if (venta.getTipoPago() == TipoPago.CREDITO) { // [cite: 302]
            Optional<Credit> creditoOpt = creditRepository.findByVentaId(ventaId); // [cite: 312]

            if (creditoOpt.isPresent()) {
                Credit credito = creditoOpt.get();

                // Si tiene pagos, no se puede anular sin devolución
                if (credito.getMontoTotal().compareTo(credito.getMontoPendiente()) != 0) { //
                    throw new RuntimeException("El crédito tiene abonos. Debe devolver el dinero antes de anular"); //
                }

                // Anular crédito
                credito.anular(); // [cite: 255]
                creditRepository.save(credito); //
            }
        }

        // Devolver stock
        for (SaleDetail detalle : venta.getDetalles()) { //
            Product producto = detalle.getProducto(); //
            producto.aumentarStock(detalle.getCantidad());
            productRepository.save(producto); //

            // Registrar movimiento
            registrarMovimientoInventario(producto, detalle.getCantidad(),
                    TipoMovimiento.ENTRADA, MotivoMovimiento.DEVOLUCION,
                    ventaId, usuario);
        }

        // Anular venta
        venta.anular();
        saleRepository.save(venta); //

        log.info("Venta anulada: {}", venta.getNumeroVenta()); //
    }

    // ========================================
    // ABONOS A CRÉDITO
    // ========================================

    /**
     * Registra un abono a un crédito (Llamado desde CreditService)
     */
    public Payment registrarAbono(Long creditoId, BigDecimal monto, MetodoPago metodoPago, //
            String referencia, User usuario) { //
        log.info("Registrando abono de {} al crédito ID: {}", monto, creditoId); //

        Optional<Credit> creditoOpt = creditRepository.findById(creditoId); //

        if (creditoOpt.isEmpty()) {
            throw new RuntimeException("Crédito no encontrado"); //
        }

        Credit credito = creditoOpt.get();

        // Validar que esté activo
        if (!credito.estaActivo()) {
            throw new RuntimeException("El crédito no está activo"); //
        }

        // ✅ Validar monto positivo
        if (monto.compareTo(BigDecimal.ZERO) <= 0) { //
            throw new RuntimeException("El monto debe ser mayor a cero"); //
        }

        // Validar monto
        if (monto.compareTo(credito.getMontoPendiente()) > 0) { //
            throw new RuntimeException(String.format(
                    "El monto (S/ %.2f) supera la deuda pendiente (S/ %.2f)", //
                    monto, credito.getMontoPendiente())); //
        }

        // Crear pago
        Payment pago = Payment.builder() //
                .venta(credito.getVenta()) //
                .credito(credito) //
                .monto(monto) //
                .metodoPago(metodoPago) //
                .referencia(referencia) //
                .usuario(usuario) //
                .build(); //

        Payment pagoGuardado = paymentRepository.save(pago); // Guardar el pago primero

        // <<--- CORRECCIÓN AQUÍ --->>
        // Aplicar pago al crédito llamando al método correcto en Credit
        credito.aplicarPagoConDetalle(pagoGuardado);
        // Guardar el estado actualizado del crédito (después de aplicar el pago)
        creditRepository.save(credito); //

        log.info("Abono registrado exitosamente. Nuevo saldo: S/ {}", credito.getMontoPendiente()); //
        return pagoGuardado;
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
        String year = String.valueOf(LocalDateTime.now().getYear()); //
        String prefijo = "VEN-" + year + "-"; //

        Long siguiente = saleRepository.generarSiguienteNumero(prefijo + "%");

        return String.format("%s%05d", prefijo, siguiente); //
    }

    /**
     * Registra un movimiento de inventario - ✅ MEJORADO
     */
    private InventoryMovement registrarMovimientoInventario(Product producto, Integer cantidad, //
            TipoMovimiento tipo, MotivoMovimiento motivo, //
            Long referenciaId, User usuario) { //

        Integer stockAnterior = producto.getStockActual(); // Obtener stock ANTES del cambio
        // El stock del producto ya fue actualizado antes de llamar a este método

        InventoryMovement movimiento = InventoryMovement.builder() //
                .producto(producto) //
                .usuario(usuario) //
                .tipo(tipo) //
                .motivo(motivo) //
                .cantidad(cantidad) //
                .stockAnterior(stockAnterior) // Stock antes del movimiento
                .stockNuevo(producto.getStockActual()) // Stock después del movimiento
                .referenciaId(referenciaId) //
                .referenciaTipo("VENTA") //
                .build(); //

        return inventoryMovementRepository.save(movimiento); //
    }

    /**
     * Actualiza la referencia en movimientos de inventario - ✅ NUEVO
     */
    private void actualizarReferenciaMovimiento(Product producto, Long ventaId) { //
        // Buscar el último movimiento de SALIDA por VENTA para este producto SIN
        // referenciaID
        List<InventoryMovement> movimientos = inventoryMovementRepository //
                .findByProductoIdOrderByFechaMovimientoDesc(producto.getId());

        // Actualizar el último movimiento sin referencia y que sea de VENTA
        movimientos.stream()
                .filter(m -> m.getReferenciaId() == null && m.getMotivo() == MotivoMovimiento.VENTA
                        && m.getTipo() == TipoMovimiento.SALIDA)
                .findFirst() //
                .ifPresent(m -> { //
                    m.setReferenciaId(ventaId); //
                    inventoryMovementRepository.save(m); //
                    log.debug("Referencia de venta {} actualizada para movimiento ID {}", ventaId, m.getId()); //
                }); //
    }
}