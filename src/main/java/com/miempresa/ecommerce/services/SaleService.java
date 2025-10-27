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
     * Crea una nueva venta (incluyendo descuento y costo de envío)
     */
    // --- INICIO DE LA INTEGRACIÓN ---
    // Firma corregida
    @Transactional(rollbackFor = Exception.class)
    public Sale crearVenta(Sale venta, List<SaleDetail> detalles,
            List<Payment> pagos, User usuario, Integer numCuotas,
            BigDecimal descuento, BigDecimal costoEnvio) { // <<--- PARÁMETROS AÑADIDOS
        log.info("Creando nueva venta...");

        // Validación crédito/cuotas
        if (venta.getTipoPago() == TipoPago.CREDITO) {
            if (numCuotas == null || numCuotas < 1 || numCuotas > 24) {
                throw new RuntimeException("Para ventas a crédito debe especificar entre 1 y 24 cuotas");
            }
        }

        // 1. Datos básicos
        venta.setNumeroVenta(generarNumeroVenta());
        venta.setUsuario(usuario);
        venta.setEstado(EstadoVenta.COMPLETADA);

        // 2. Detalles
        for (SaleDetail detalle : detalles) {
            detalle.establecerDatosProducto();
            detalle.calcularSubtotal();
            venta.agregarDetalle(detalle);
        }

        // 3. Asignar Descuento y Envío ANTES de calcular total
        venta.setDescuento(descuento != null ? descuento : BigDecimal.ZERO); // <<--- USAR PARÁMETRO
        venta.setCostoEnvio(costoEnvio != null ? costoEnvio : BigDecimal.ZERO); // <<--- USAR PARÁMETRO

        // 4. Calcular Total
        venta.calcularTotal(); // Ahora usará descuento y envío
        log.info("Total calculado (con desc: {}, envío: {}): S/ {}", venta.getDescuento(), venta.getCostoEnvio(),
                venta.getTotal());

        // 5. Descontar Stock y Registrar Movimiento
        for (SaleDetail detalle : detalles) {
            Product producto = detalle.getProducto();
            if (producto == null) { // Añadir validación por si acaso
                log.error("Error crítico: Detalle ID {} no tiene producto asociado.", detalle.getId());
                throw new RuntimeException("Error interno: Detalle de venta inválido.");
            }
            if (!producto.disminuirStock(detalle.getCantidad())) {
                log.error("Stock insuficiente para {} (ID {}) al crear venta.", producto.getNombre(), producto.getId());
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
            productRepository.save(producto);

            // 6. Registrar movimiento de inventario (el ID de venta se actualizará después)
            registrarMovimientoInventario(producto, detalle.getCantidad(),
                    TipoMovimiento.SALIDA, MotivoMovimiento.VENTA,
                    null, usuario);
        }

        // 7. Guardar Venta
        Sale ventaGuardada = saleRepository.save(venta);
        log.info("Venta guardada (ID: {}), procesando crédito y pagos...", ventaGuardada.getId());

        // 8. Crear Crédito si aplica
        Credit creditoGuardado = null;
        if (ventaGuardada.getTipoPago() == TipoPago.CREDITO) {
            creditoGuardado = crearCredito(ventaGuardada, pagos.isEmpty() ? null : pagos.get(0), numCuotas);
        }

        // 9. Registrar Pagos
        List<Payment> pagosGuardados = new ArrayList<>();
        for (Payment pago : pagos) {
            pago.setVenta(ventaGuardada);
            pago.setUsuario(usuario);
            // Si es un abono inicial a crédito, asociarlo al crédito
            if (creditoGuardado != null && pago.getMonto().compareTo(BigDecimal.ZERO) > 0) {
                pago.setCredito(creditoGuardado);
            }
            pagosGuardados.add(paymentRepository.save(pago)); // Guardar cada pago
        }
        // Opcional: Si no usas cascade para pagos o quieres asegurar la relación
        // bidireccional
        // ventaGuardada.setPagos(pagosGuardados);
        // saleRepository.save(ventaGuardada); // Guardar venta de nuevo con pagos
        // asociados

        // 10. Actualizar referencias en movimientos de inventario
        log.info("Actualizando referencias de movimiento para venta ID: {}", ventaGuardada.getId());
        // Asegurarse que los detalles tengan el producto cargado
        // Si la relación es LAZY, podrías necesitar recargar la venta o los detalles
        // Sale ventaConDetalles =
        // saleRepository.findById(ventaGuardada.getId()).orElse(ventaGuardada); //
        // Ejemplo
        for (SaleDetail detalle : ventaGuardada.getDetalles()) {
            if (detalle.getProducto() != null) {
                actualizarReferenciaMovimiento(detalle.getProducto(), ventaGuardada.getId());
            } else {
                log.warn("Detalle ID {} no tiene producto asociado al actualizar referencia de movimiento.",
                        detalle.getId());
                // Considera recargar si es necesario
            }
        }

        log.info("Venta {} creada exitosamente.", ventaGuardada.getNumeroVenta());
        return ventaGuardada;
    }
    // --- FIN DE LA INTEGRACIÓN ---

    /**
     * Crea un crédito con cuotas - ✅ CORREGIDO
     */
    private Credit crearCredito(Sale venta, Payment pagoInicial, int numCuotas) {
        log.info("Creando crédito para venta: {} con {} cuotas", venta.getNumeroVenta(), numCuotas);

        Credit credito = Credit.builder()
                .venta(venta)
                .cliente(venta.getCliente())
                .montoTotal(venta.getTotal())
                .montoPendiente(venta.getTotal())
                .numCuotas(numCuotas) // Usa el parámetro
                .fechaInicio(LocalDate.now())
                .estado(EstadoCredito.ACTIVO)
                .build();

        credito.calcularMontoCuota();
        credito.generarCuotas();

        Credit creditoGuardado = creditRepository.save(credito); // Guardar primero

        // Aplicar pago inicial si existe
        if (pagoInicial != null && pagoInicial.getMonto().compareTo(BigDecimal.ZERO) > 0) {
            log.info("Aplicando pago inicial de S/ {} al crédito ID {}", pagoInicial.getMonto(),
                    creditoGuardado.getId());
            // El pago ya debe tener la asociación al crédito desde crearVenta
            if (pagoInicial.getCredito() == null) { // Doble check por si acaso
                pagoInicial.setCredito(creditoGuardado);
                paymentRepository.save(pagoInicial); // Guardar asociación si faltaba
            }
            creditoGuardado.aplicarPagoConDetalle(pagoInicial); // Aplicar lógica de pago
            creditRepository.save(creditoGuardado); // Guardar estado actualizado del crédito
        }

        return creditoGuardado;
    }

    // ========================================
    // ANULAR VENTA - ✅ MEJORADO / INTEGRADO
    // ========================================

    @Transactional(rollbackFor = Exception.class)
    public void anularVenta(Long ventaId, User usuario) {
        log.info("Iniciando anulación de venta ID: {}", ventaId);

        Sale venta = saleRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + ventaId));

        // 1. Verificar si ya está anulada
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            log.warn("Intento de anular venta ID {} que ya está ANULADA.", ventaId);
            throw new RuntimeException("La venta ya se encuentra anulada.");
        }

        // 2. Verificar si está completada (solo se anulan completadas)
        if (venta.getEstado() != EstadoVenta.COMPLETADA) {
            log.warn("Intento de anular venta ID {} que no está COMPLETADA (Estado: {})", ventaId, venta.getEstado());
            throw new RuntimeException("Solo se pueden anular ventas que estén completadas.");
        }

        log.info("Venta {} encontrada. Estado: COMPLETADA. Tipo Pago: {}", venta.getNumeroVenta(),
                venta.getTipoPago());

        // 3. Anular crédito asociado (si existe y aplica)
        if (venta.getTipoPago() == TipoPago.CREDITO) {
            Optional<Credit> creditoOpt = creditRepository.findByVentaId(ventaId);
            if (creditoOpt.isPresent()) {
                Credit credito = creditoOpt.get();
                log.info("Crédito asociado ID {} encontrado. Estado actual: {}. Monto Pendiente: S/ {}",
                        credito.getId(), credito.getEstado(), credito.getMontoPendiente());

                // Validar si se puede anular (ej: si tiene pagos)
                BigDecimal montoPagado = credito.getMontoTotal().subtract(credito.getMontoPendiente());
                // DESCOMENTA si quieres impedir anulación con pagos
                /*
                 * if (montoPagado.compareTo(BigDecimal.ZERO) > 0) {
                 * log.error(
                 * "No se puede anular la venta {} porque el crédito asociado ID {} tiene pagos registrados (S/ {})."
                 * ,
                 * venta.getNumeroVenta(), credito.getId(), montoPagado);
                 * throw new RuntimeException(
                 * "No se puede anular una venta a crédito que ya tiene pagos registrados. Realice una devolución manual del dinero primero."
                 * );
                 * }
                 */

                log.info("Anulando crédito ID {}", credito.getId());
                credito.setEstado(EstadoCredito.ANULADO); // Cambiar estado a ANULADO
                // Opcional: Podrías querer anular las cuotas también
                // credito.getCuotas().forEach(c -> c.setEstado(EstadoCuota.ANULADA)); // Si
                // tienes ese estado
                creditRepository.save(credito);
            } else {
                log.warn("Venta a crédito {} no tiene un crédito asociado registrado. No se puede anular el crédito.",
                        venta.getNumeroVenta());
                // Considerar si esto debe ser un error fatal o solo una advertencia
            }
        }

        // 4. Devolver stock y registrar movimientos
        log.info("Devolviendo stock para venta {}...", venta.getNumeroVenta());
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            log.warn("La venta {} no tiene detalles para devolver stock.", venta.getNumeroVenta());
        } else {
            for (SaleDetail detalle : venta.getDetalles()) {
                try {
                    // Cargar explícitamente el producto si es LAZY y no está cargado
                    Product producto = detalle.getProducto();
                    if (producto == null) {
                        // Intentar recargar el detalle o buscar el producto por ID si es necesario
                        Optional<SaleDetail> detalleCompletoOpt = saleDetailRepository.findById(detalle.getId());
                        if (detalleCompletoOpt.isPresent()) {
                            producto = detalleCompletoOpt.get().getProducto();
                        }

                        if (producto == null) {
                            log.error(
                                    "El detalle ID {} de la venta {} no tiene producto asociado o no se pudo cargar. No se puede devolver stock.",
                                    detalle.getId(), venta.getNumeroVenta());
                            continue; // Saltar este detalle
                        }
                    }

                    Integer cantidadDevuelta = detalle.getCantidad();
                    if (cantidadDevuelta == null || cantidadDevuelta <= 0) {
                        log.warn("Cantidad inválida ({}) en detalle ID {} para producto ID {}. No se devuelve stock.",
                                cantidadDevuelta, detalle.getId(), producto.getId());
                        continue;
                    }

                    Integer stockAntesDev = producto.getStockActual() != null ? producto.getStockActual() : 0;

                    producto.aumentarStock(cantidadDevuelta);
                    productRepository.save(producto);
                    log.info("Stock de producto '{}' (ID {}) aumentado en {}. Stock: {} -> {}",
                            producto.getNombre(), producto.getId(), cantidadDevuelta, stockAntesDev,
                            producto.getStockActual());

                    // Registrar movimiento de devolución (ENTRADA)
                    registrarMovimientoInventario(producto, cantidadDevuelta,
                            TipoMovimiento.ENTRADA, MotivoMovimiento.DEVOLUCION, // Usar DEVOLUCION para anulación
                            ventaId, usuario); // Pasar ventaId como referencia
                } catch (Exception e) {
                    log.error(
                            "Error al devolver stock o registrar movimiento para detalle ID {} (Producto ID {}) en venta {}: {}",
                            detalle.getId(), detalle.getProducto() != null ? detalle.getProducto().getId() : "N/A",
                            venta.getNumeroVenta(), e.getMessage(), e);
                    throw new RuntimeException("Error al procesar devolución de stock para "
                            + (detalle.getProducto() != null ? detalle.getProducto().getNombre() : "ID desconocido"),
                            e);
                }
            }
        }

        // 5. Anular la venta
        venta.setEstado(EstadoVenta.ANULADA);
        venta.setFechaAnulacion(LocalDateTime.now()); // Guardar fecha de anulación
        saleRepository.save(venta);

        log.info("Venta {} anulada correctamente por usuario {}", venta.getNumeroVenta(), usuario.getUsername());
    }

    // ========================================
    // ABONOS A CRÉDITO
    // ========================================

    /**
     * Registra un abono a un crédito (Llamado desde CreditService)
     */
    public Payment registrarAbono(Long creditoId, BigDecimal monto, MetodoPago metodoPago,
            String referencia, User usuario) {
        log.info("Registrando abono de {} al crédito ID: {}", monto, creditoId);

        Credit credito = creditRepository.findById(creditoId)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));

        if (!credito.estaActivo()) {
            throw new RuntimeException("El crédito no está activo");
        }
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto debe ser mayor a cero");
        }
        if (monto.compareTo(credito.getMontoPendiente()) > 0) {
            throw new RuntimeException(String.format(
                    "El monto (S/ %.2f) supera la deuda pendiente (S/ %.2f)",
                    monto, credito.getMontoPendiente()));
        }

        Payment pago = Payment.builder()
                .venta(credito.getVenta())
                .credito(credito)
                .monto(monto)
                .metodoPago(metodoPago)
                .referencia(referencia)
                .usuario(usuario)
                .build();

        Payment pagoGuardado = paymentRepository.save(pago);
        credito.aplicarPagoConDetalle(pagoGuardado);
        creditRepository.save(credito);

        log.info("Abono registrado exitosamente. Nuevo saldo: S/ {}", credito.getMontoPendiente());
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
        BigDecimal total = saleRepository.sumarVentasPorFecha(fechaInicio, fechaFin);
        return total != null ? total : BigDecimal.ZERO;
    }

    // ========================================
    // UTILIDADES PRIVADAS - ✅ MEJORADAS
    // ========================================

    private String generarNumeroVenta() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String prefijo = "VEN-" + year + "-";
        Long siguiente = saleRepository.generarSiguienteNumero(prefijo + "%");
        // Asegúrate que generarSiguienteNumero maneje el caso inicial devolviendo 1L o
        // usa:
        // Long num = Optional.ofNullable(saleRepository.generarSiguienteNumero(prefijo
        // + "%")).orElse(0L);
        // Long siguiente = num + 1L;
        return String.format("%s%05d", prefijo, siguiente);
    }

    private InventoryMovement registrarMovimientoInventario(Product producto, Integer cantidad,
            TipoMovimiento tipo, MotivoMovimiento motivo,
            Long referenciaId, User usuario) {

        Integer stockNuevo = producto.getStockActual() != null ? producto.getStockActual() : 0;
        Integer stockAnterior;
        if (tipo == TipoMovimiento.SALIDA) {
            stockAnterior = stockNuevo + cantidad;
        } else { // ENTRADA
            stockAnterior = stockNuevo - cantidad;
        }
        if (stockAnterior < 0 && tipo == TipoMovimiento.ENTRADA) {
            stockAnterior = 0; // Evitar stock anterior negativo en devoluciones si stock era 0
        }

        InventoryMovement movimiento = InventoryMovement.builder()
                .producto(producto)
                .usuario(usuario)
                .tipo(tipo)
                .motivo(motivo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .referenciaId(referenciaId)
                .referenciaTipo(
                        motivo == MotivoMovimiento.VENTA || motivo == MotivoMovimiento.DEVOLUCION ? "VENTA" : "OTRO")
                .build();

        return inventoryMovementRepository.save(movimiento);
    }

    private void actualizarReferenciaMovimiento(Product producto, Long ventaId) {
        if (producto == null || ventaId == null) {
            log.warn("Producto o Venta ID nulos al intentar actualizar referencia de movimiento.");
            return;
        }

        List<InventoryMovement> movimientos = inventoryMovementRepository
                .findByProductoIdAndMotivoAndTipoAndReferenciaIdIsNullOrderByFechaMovimientoDesc(
                        producto.getId(),
                        MotivoMovimiento.VENTA,
                        TipoMovimiento.SALIDA);

        movimientos.stream()
                .findFirst()
                .ifPresent(m -> {
                    m.setReferenciaId(ventaId);
                    inventoryMovementRepository.save(m);
                    log.debug("Referencia de venta {} actualizada para movimiento ID {}", ventaId, m.getId());
                });

        if (movimientos.isEmpty()) {
            log.warn(
                    "No se encontró un movimiento de inventario de SALIDA por VENTA sin referencia para el producto ID {} asociado a la venta ID {}",
                    producto.getId(), ventaId);
        }
    }
}
