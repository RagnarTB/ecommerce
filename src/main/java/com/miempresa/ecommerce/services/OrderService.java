package com.miempresa.ecommerce.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miempresa.ecommerce.models.Order;
import com.miempresa.ecommerce.models.OrderDetail;
import com.miempresa.ecommerce.models.Payment;
import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.models.Sale;
import com.miempresa.ecommerce.models.SaleDetail;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.models.enums.EstadoPedido;
import com.miempresa.ecommerce.models.enums.TipoPago;
import com.miempresa.ecommerce.repositories.OrderRepository;
import com.miempresa.ecommerce.repositories.ProductRepository;

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

    // ========================================
    // CREAR PEDIDO
    // ========================================

    /**
     * Crea un nuevo pedido desde la web
     */
    @Transactional(rollbackFor = Exception.class) // ✅ AGREGADO
    public Order crearPedido(Order pedido, List<OrderDetail> detalles) {
        log.info("Creando nuevo pedido para cliente: {}", pedido.getCliente().getNombreCompleto());

        // Generar número de pedido
        pedido.setNumeroPedido(generarNumeroPedido());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // Agregar detalles y calcular totales
        for (OrderDetail detalle : detalles) {
            // Validar stock disponible
            Product producto = detalle.getProducto();
            if (!producto.hayStock() || producto.getStockActual() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            detalle.establecerDatosProducto();
            detalle.calcularSubtotal();
            pedido.agregarDetalle(detalle);
        }

        pedido.calcularTotal();

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
    public Order confirmarPedido(Long pedidoId) {
        log.info("Confirmando pedido ID: {}", pedidoId);

        Optional<Order> pedidoOpt = buscarPorId(pedidoId);

        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado");
        }

        Order pedido = pedidoOpt.get();

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new RuntimeException("Solo se pueden confirmar pedidos pendientes");
        }

        // Validar stock nuevamente
        for (OrderDetail detalle : pedido.getDetalles()) {
            Product producto = detalle.getProducto();
            if (producto.getStockActual() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
        }

        pedido.confirmar();

        return orderRepository.save(pedido);
    }

    // ========================================
    // CONVERTIR A VENTA - ✅ CORREGIDO
    // ========================================

    /**
     * Convierte un pedido en venta
     * Este método llama a SaleService para crear la venta
     * 
     * @param pedidoId  ID del pedido a convertir
     * @param usuario   Usuario que realiza la conversión
     * @param tipoPago  Tipo de pago (CONTADO o CREDITO) - ✅ AGREGADO
     * @param numCuotas Número de cuotas (obligatorio si es CREDITO) - ✅ AGREGADO
     * @return Venta creada
     */
    @Transactional(rollbackFor = Exception.class) // ✅ AGREGADO
    public Sale convertirAVenta(Long pedidoId, User usuario, TipoPago tipoPago, Integer numCuotas) { // ✅ PARÁMETROS
                                                                                                     // AGREGADOS
        log.info("Convirtiendo pedido ID: {} a venta", pedidoId);

        Optional<Order> pedidoOpt = buscarPorId(pedidoId);

        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado");
        }

        Order pedido = pedidoOpt.get();

        if (pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new RuntimeException("El pedido debe estar confirmado primero");
        }

        // ✅ VALIDACIÓN AGREGADA
        if (tipoPago == TipoPago.CREDITO) {
            if (numCuotas == null || numCuotas < 1 || numCuotas > 24) {
                throw new RuntimeException("Para ventas a crédito debe especificar entre 1 y 24 cuotas");
            }
        }

        // Crear venta desde el pedido
        Sale venta = Sale.builder()
                .cliente(pedido.getCliente())
                .usuario(usuario)
                .pedido(pedido)
                .subtotal(pedido.getSubtotal())
                .costoEnvio(pedido.getCostoEnvio())
                .total(pedido.getTotal())
                .tipoPago(tipoPago) // ✅ AGREGADO
                .build();

        // Convertir detalles de pedido a detalles de venta
        List<SaleDetail> ventaDetalles = pedido.getDetalles().stream()
                .map(detalle -> SaleDetail.builder()
                        .producto(detalle.getProducto())
                        .nombreProducto(detalle.getNombreProducto())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .cantidad(detalle.getCantidad())
                        .subtotal(detalle.getSubtotal())
                        .build())
                .toList();

        // Crear pagos vacíos (se registrarán después)
        List<Payment> pagos = List.of();

        // Crear venta con el parámetro numCuotas - ✅ CORREGIDO
        return saleService.crearVenta(venta, ventaDetalles, pagos, usuario, numCuotas);
    }

    // ========================================
    // CANCELAR PEDIDO
    // ========================================

    /**
     * Cancela un pedido
     */
    public void cancelarPedido(Long pedidoId) {
        log.info("Cancelando pedido ID: {}", pedidoId);

        Optional<Order> pedidoOpt = buscarPorId(pedidoId);

        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado");
        }

        Order pedido = pedidoOpt.get();

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new RuntimeException("El pedido ya está cancelado");
        }

        pedido.cancelar();
        orderRepository.save(pedido);

        log.info("Pedido cancelado: {}", pedido.getNumeroPedido());
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

        Long siguiente = orderRepository.generarSiguienteNumero(prefijo + "%");

        return String.format("%s%05d", prefijo, siguiente);
    }
}