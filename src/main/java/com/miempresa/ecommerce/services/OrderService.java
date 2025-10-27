package com.miempresa.ecommerce.services;

import java.math.BigDecimal; // <<--- AÑADIDO import
import java.time.LocalDateTime;
import java.util.ArrayList; // Added import for ArrayList
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // <<--- AÑADIDO import

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miempresa.ecommerce.models.Order;
import com.miempresa.ecommerce.models.OrderDetail;
import com.miempresa.ecommerce.models.Payment; // <<--- AÑADIDO import
import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.models.Sale;
import com.miempresa.ecommerce.models.SaleDetail;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.models.enums.EstadoPedido;
import com.miempresa.ecommerce.models.enums.TipoPago;
import com.miempresa.ecommerce.repositories.OrderRepository;
import com.miempresa.ecommerce.repositories.ProductRepository;
import com.miempresa.ecommerce.repositories.SaleDetailRepository; // Added import for SaleDetailRepository

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SERVICE: PEDIDO
 *
 * Gestiona los pedidos creados desde la web.
 * Los pedidos NO descuentan stock hasta que se convierten en venta.
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SaleService saleService;
    private final SaleDetailRepository saleDetailRepository; // Added repository

    // ========================================
    // CREAR PEDIDO
    // ========================================

    /**
     * Crea un nuevo pedido desde la web
     */
    @Transactional(rollbackFor = Exception.class) // ✅ AGREGADO
    public Order crearPedido(Order pedido, List<OrderDetail> detalles) {
        // Assume pedido.getCliente() is not null due to controller validation
        log.info("Creando nuevo pedido para cliente: {}",
                pedido.getCliente() != null ? pedido.getCliente().getNombreCompleto() : "Desconocido");

        // Generar número de pedido
        pedido.setNumeroPedido(generarNumeroPedido());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // Agregar detalles y calcular totales
        for (OrderDetail detalle : detalles) {
            // Validar stock disponible
            Product producto = detalle.getProducto();
            if (producto == null) { // Added null check for product
                log.error("Error crítico: Detalle de pedido no tiene producto asociado.");
                throw new RuntimeException("Error interno: Detalle de pedido inválido.");
            }
            // Find the product again to get the latest stock information
            Product productoActualizado = productRepository.findById(producto.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado (ID: " + producto.getId() + ") al crear pedido."));

            Integer stockDisponible = productoActualizado.getStockActual() != null
                    ? productoActualizado.getStockActual()
                    : 0;
            Integer cantidadNecesaria = detalle.getCantidad() != null ? detalle.getCantidad() : 0;

            if (cantidadNecesaria <= 0) {
                log.warn("Cantidad inválida ({}) en detalle para producto ID {}. Saltando.", cantidadNecesaria,
                        producto.getId());
                throw new RuntimeException(
                        "La cantidad del producto '" + producto.getNombre() + "' debe ser mayor a cero.");
            }

            if (stockDisponible < cantidadNecesaria) {
                log.error(
                        "Stock insuficiente al crear pedido {}. Producto: '{}' (ID {}), Necesario: {}, Disponible: {}",
                        (pedido.getNumeroPedido() != null ? pedido.getNumeroPedido() : "N/A"),
                        productoActualizado.getNombre(), productoActualizado.getId(), cantidadNecesaria,
                        stockDisponible);
                throw new RuntimeException("Stock insuficiente para: " + productoActualizado.getNombre());
            }

            detalle.establecerDatosProducto(); // Use the original product reference potentially? Or updated? Using
                                               // original for snapshot.
            detalle.calcularSubtotal();
            pedido.agregarDetalle(detalle);
        }

        pedido.calcularTotal(); // Calculates subtotal, igv, total

        Order pedidoGuardado = orderRepository.save(pedido);

        log.info("Pedido creado: {}", pedidoGuardado.getNumeroPedido());
        return pedidoGuardado;
    }

    // ========================================
    // CONFIRMAR PEDIDO
    // ========================================

    /**
     * Confirma un pedido (cambio de estado)
     */
    // --- INICIO DE LA INTEGRACIÓN ---
    public Order confirmarPedido(Long pedidoId) {
        log.info("Confirmando pedido ID: {}", pedidoId);

        Order pedido = orderRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            log.warn("Intento de confirmar pedido ID {} que no está PENDIENTE (Estado: {})", pedidoId,
                    pedido.getEstado());
            throw new RuntimeException("Solo se pueden confirmar pedidos que están pendientes.");
        }

        // Validar stock nuevamente justo antes de confirmar
        log.info("Validando stock para confirmar pedido ID {}", pedidoId);
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            log.warn("Pedido ID {} no tiene detalles. Confirmando de todas formas.", pedidoId);
        } else {
            for (OrderDetail detalle : pedido.getDetalles()) {
                // <<--- CORRECCIÓN AQUÍ: Volver a buscar el producto --->>>
                if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
                    log.error("Detalle ID {} no tiene producto asociado o ID nulo.", detalle.getId());
                    throw new RuntimeException("Detalle de pedido inválido al confirmar stock.");
                }
                Long productoId = detalle.getProducto().getId();
                Product productoActualizado = productRepository.findById(productoId)
                        .orElseThrow(() -> {
                            log.error("Producto ID {} asociado al detalle {} no encontrado al confirmar.", productoId,
                                    detalle.getId());
                            return new RuntimeException(
                                    "Producto no encontrado (ID: " + productoId + ") al confirmar stock.");
                        });

                // <<--- QUITAR ESTA LÍNEA (INCORRECTA) --->>>
                // productRepository.refresh(producto); // <--- Eliminar esta línea

                Integer stockDisponible = productoActualizado.getStockActual() != null
                        ? productoActualizado.getStockActual()
                        : 0;
                Integer cantidadNecesaria = detalle.getCantidad() != null ? detalle.getCantidad() : 0;

                if (cantidadNecesaria <= 0) {
                    log.warn("Detalle ID {} tiene cantidad inválida ({}). Saltando validación de stock.",
                            detalle.getId(), cantidadNecesaria);
                    continue; // Saltar si la cantidad es 0 o negativa
                }

                if (stockDisponible < cantidadNecesaria) {
                    log.error(
                            "Stock insuficiente para confirmar pedido {}. Producto: '{}' (ID {}), Necesario: {}, Disponible: {}",
                            pedido.getNumeroPedido(), productoActualizado.getNombre(), productoId, cantidadNecesaria,
                            stockDisponible);
                    throw new RuntimeException("Stock insuficiente para: " + productoActualizado.getNombre()
                            + " (Disponible: " + stockDisponible + ")");
                }
                log.debug("Stock validado para producto ID {}: Necesario {}, Disponible {}", productoId,
                        cantidadNecesaria, stockDisponible);
            }
        }

        pedido.confirmar(); // Cambia estado y fecha
        log.info("Pedido ID {} confirmado.", pedidoId);
        return orderRepository.save(pedido);
    }
    // --- FIN DE LA INTEGRACIÓN ---

    // ========================================
    // CONVERTIR A VENTA - ✅ CORREGIDO
    // ========================================

    /**
     * Convierte un pedido en venta
     * Este método llama a SaleService para crear la venta
     *
     * @param pedidoId  ID del pedido a convertir
     * @param usuario   Usuario que realiza la conversión
     * @param tipoPago  Tipo de pago (CONTADO o CREDITO)
     * @param numCuotas Número de cuotas (obligatorio si es CREDITO)
     * @return Venta creada
     */
    @Transactional(rollbackFor = Exception.class)
    public Sale convertirAVenta(Long pedidoId, User usuario, TipoPago tipoPago, Integer numCuotas) {
        log.info("Convirtiendo pedido ID: {} a venta por usuario {}", pedidoId, usuario.getUsername());

        Order pedido = orderRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));

        if (pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            log.warn("Intento de convertir pedido ID {} a venta, pero no está CONFIRMADO (Estado: {})", pedidoId,
                    pedido.getEstado());
            throw new RuntimeException("Solo se pueden convertir a venta los pedidos que están confirmados.");
        }

        if (tipoPago == TipoPago.CREDITO) {
            if (numCuotas == null || numCuotas < 1 || numCuotas > 24) { // Ajusta el max si es necesario
                throw new RuntimeException(
                        "Para ventas a crédito debe especificar un número de cuotas válido (ej. 1 a 24).");
            }
        }

        // Crear objeto base de la Venta
        Sale venta = Sale.builder()
                .cliente(pedido.getCliente())
                .usuario(usuario) // Usuario que realiza la conversión
                .pedido(pedido) // Asociar al pedido original
                // Subtotal, CostoEnvio se copian del pedido
                .subtotal(pedido.getSubtotal() != null ? pedido.getSubtotal() : BigDecimal.ZERO) // Handle null subtotal
                .costoEnvio(pedido.getCostoEnvio() != null ? pedido.getCostoEnvio() : BigDecimal.ZERO) // Handle null
                                                                                                       // cost
                // El descuento no viene del pedido, asumimos 0 al convertir
                .descuento(BigDecimal.ZERO)
                .tipoPago(tipoPago)
                // IGV y Total se recalcularán en crearVenta a partir de subtotal, descuento,
                // costoEnvio
                .build();

        // Convertir Detalles del Pedido a Detalles de Venta
        List<SaleDetail> ventaDetalles = pedido.getDetalles().stream()
                .map(detallePedido -> {
                    Product productoDetalle = detallePedido.getProducto();
                    if (productoDetalle == null) {
                        log.error("Error crítico: Detalle de pedido ID {} no tiene producto asociado.",
                                detallePedido.getId());
                        throw new RuntimeException("Error interno: Detalle de pedido inválido al convertir.");
                    }
                    return SaleDetail.builder()
                            .producto(productoDetalle)
                            // Copiar datos snapshot del detalle del pedido
                            .nombreProducto(detallePedido.getNombreProducto())
                            .precioUnitario(detallePedido.getPrecioUnitario())
                            .cantidad(detallePedido.getCantidad())
                            .codigoSku(productoDetalle.getCodigoSku()) // Copiar SKU también
                            .descuento(BigDecimal.ZERO) // Asumir 0 descuento por item al convertir
                            .subtotal(detallePedido.getSubtotal()) // Usar subtotal ya calculado
                            // 'venta' se asignará dentro de crearVenta
                            .build();
                })
                .collect(Collectors.toList());

        // Lista de pagos vacía (los pagos se registran después si es CONTADO, o son
        // abonos si es CREDITO)
        List<Payment> pagosIniciales = new ArrayList<>(); // Use ArrayList

        log.info("Llamando a SaleService.crearVenta para pedido ID {}", pedidoId);

        // Pasar los 7 argumentos requeridos
        Sale ventaCreada = saleService.crearVenta(
                venta,
                ventaDetalles,
                pagosIniciales,
                usuario,
                numCuotas,
                venta.getDescuento(), // Pasar el descuento (BigDecimal.ZERO en este caso)
                venta.getCostoEnvio() // Pasar el costo de envío del pedido
        );

        // Marcar el pedido como COMPLETADO/FACTURADO después de crear la venta
        pedido.setEstado(EstadoPedido.CONFIRMADO); // O un estado 'COMPLETADO' si lo tienes
        orderRepository.save(pedido);
        log.info("Pedido ID {} marcado como FACTURADO.", pedidoId);

        return ventaCreada; // Devolver la venta creada
    }

    // ========================================
    // CANCELAR PEDIDO
    // ========================================

    /**
     * Cancela un pedido
     */
    public void cancelarPedido(Long pedidoId) {
        log.info("Cancelando pedido ID: {}", pedidoId);
        Order pedido = orderRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            log.warn("Pedido ID {} ya se encuentra cancelado.", pedidoId);
            return; // Ya está cancelado, no hacer nada más
        }
        // Impedir cancelar si ya está facturado
        if (pedido.getEstado() == EstadoPedido.CONFIRMADO) {
            log.error("Intento de cancelar pedido ID {} que ya está FACTURADO.", pedidoId);
            throw new RuntimeException("No se puede cancelar un pedido que ya ha sido facturado (convertido a venta).");
        }

        pedido.cancelar(); // Cambia estado
        orderRepository.save(pedido);
        log.info("Pedido {} cancelado.", pedido.getNumeroPedido());
    }

    // ========================================
    // BÚSQUEDAS
    // ========================================

    @Transactional(readOnly = true)
    public Optional<Order> buscarPorId(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Order> buscarPorNumero(String numeroPedido) {
        return orderRepository.findByNumeroPedido(numeroPedido);
    }

    @Transactional(readOnly = true)
    public List<Order> obtenerTodos() {
        return orderRepository.findAllByOrderByFechaPedidoDesc();
    }

    @Transactional(readOnly = true)
    public List<Order> obtenerPendientes() {
        return orderRepository.findByEstadoOrderByFechaPedidoDesc(EstadoPedido.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<Order> obtenerPorEstado(EstadoPedido estado) {
        return orderRepository.findByEstadoOrderByFechaPedidoDesc(estado);
    }

    @Transactional(readOnly = true)
    public List<Order> buscarPorCliente(Long clienteId) {
        return orderRepository.findByClienteIdOrderByFechaPedidoDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public long contarPorEstado(EstadoPedido estado) {
        return orderRepository.countByEstado(estado);
    }

    // ========================================
    // UTILIDADES
    // ========================================

    /**
     * Genera número único de pedido
     * Formato: PED-2025-00001
     */
    private String generarNumeroPedido() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String prefijo = "PED-" + year + "-";
        // Asegúrate que generarSiguienteNumero maneje correctamente el caso inicial
        // (devuelva 0 o null)
        Long siguiente = orderRepository.generarSiguienteNumero(prefijo + "%");
        return String.format("%s%05d", prefijo, siguiente != null ? siguiente : 1L); // Formato 5 dígitos, inicia en 1
                                                                                     // si es el primero
    }
}
