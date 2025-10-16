package com.miempresa.ecommerce.services;

import com.miempresa.ecommerce.models.InventoryMovement;
import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.models.enums.MotivoMovimiento;
import com.miempresa.ecommerce.models.enums.TipoMovimiento;
import com.miempresa.ecommerce.repositories.InventoryMovementRepository;
import com.miempresa.ecommerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE: MOVIMIENTO DE INVENTARIO
 * 
 * Gestiona los movimientos de entrada y salida de inventario.
 * Mantiene trazabilidad total del stock.
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;

    // ========================================
    // REGISTRAR MOVIMIENTOS
    // ========================================

    /**
     * Registra un movimiento de inventario
     */
    public InventoryMovement registrarMovimiento(Long productoId, Integer cantidad,
            TipoMovimiento tipo, MotivoMovimiento motivo,
            User usuario, String observaciones) {
        log.info("Registrando movimiento: {} {} de {} unidades para producto ID: {}",
                tipo, motivo, cantidad, productoId);

        Optional<Product> productoOpt = productRepository.findById(productoId);

        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Product producto = productoOpt.get();
        Integer stockAnterior = producto.getStockActual();

        // Aplicar movimiento al producto
        if (tipo == TipoMovimiento.ENTRADA) {
            producto.aumentarStock(cantidad);
        } else {
            if (!producto.disminuirStock(cantidad)) {
                throw new RuntimeException("Stock insuficiente");
            }
        }

        productRepository.save(producto);
        Integer stockNuevo = producto.getStockActual();

        // Crear registro de movimiento
        InventoryMovement movement = InventoryMovement.builder()
                .producto(producto)
                .usuario(usuario)
                .tipo(tipo)
                .motivo(motivo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .observaciones(observaciones)
                .build();

        InventoryMovement movementGuardado = movementRepository.save(movement);

        log.info("Movimiento registrado. Stock: {} → {}", stockAnterior, stockNuevo);

        return movementGuardado;
    }

    /**
     * Registra una entrada de inventario (compra, ajuste positivo, devolución)
     */
    public InventoryMovement registrarEntrada(Long productoId, Integer cantidad,
            MotivoMovimiento motivo, User usuario,
            String observaciones) {
        return registrarMovimiento(productoId, cantidad, TipoMovimiento.ENTRADA,
                motivo, usuario, observaciones);
    }

    /**
     * Registra una salida de inventario (venta, ajuste negativo, merma)
     */
    public InventoryMovement registrarSalida(Long productoId, Integer cantidad,
            MotivoMovimiento motivo, User usuario,
            String observaciones) {
        return registrarMovimiento(productoId, cantidad, TipoMovimiento.SALIDA,
                motivo, usuario, observaciones);
    }

    // ========================================
    // CONSULTAS
    // ========================================

    @Transactional(readOnly = true)
    public Optional<InventoryMovement> buscarPorId(Long id) {
        return movementRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> obtenerTodos() {
        return movementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> obtenerPorProducto(Long productoId) {
        return movementRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> obtenerPorTipo(TipoMovimiento tipo) {
        return movementRepository.findByTipoOrderByFechaMovimientoDesc(tipo);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> obtenerPorMotivo(MotivoMovimiento motivo) {
        return movementRepository.findByMotivoOrderByFechaMovimientoDesc(motivo);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> obtenerPorFechas(LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        return movementRepository.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> buscarConFiltros(Long productoId, TipoMovimiento tipo,
            MotivoMovimiento motivo,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        return movementRepository.buscarConFiltros(productoId, tipo, motivo,
                fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> obtenerUltimos() {
        return movementRepository.findTop20ByOrderByFechaMovimientoDesc();
    }

    @Transactional(readOnly = true)
    public long contarPorTipo(TipoMovimiento tipo) {
        return movementRepository.countByTipo(tipo);
    }
}

/**
 * TIPOS DE MOVIMIENTOS:
 * 
 * ENTRADAS (aumentan stock):
 * - COMPRA: Compra a proveedor
 * - AJUSTE_POSITIVO: Corrección manual hacia arriba
 * - DEVOLUCION: Cliente devuelve producto
 * 
 * SALIDAS (disminuyen stock):
 * - VENTA: Venta a cliente
 * - AJUSTE_NEGATIVO: Corrección manual hacia abajo
 * - MERMA: Producto dañado, vencido o perdido
 * 
 * EJEMPLO DE TRAZABILIDAD:
 * 
 * 1. Compra 100 laptops
 * - Movimiento: ENTRADA - COMPRA
 * - Stock: 0 → 100
 * 
 * 2. Vende 3 laptops
 * - Movimiento: SALIDA - VENTA
 * - Stock: 100 → 97
 * 
 * 3. Cliente devuelve 1 laptop
 * - Movimiento: ENTRADA - DEVOLUCION
 * - Stock: 97 → 98
 * 
 * 4. Se dañan 2 laptops
 * - Movimiento: SALIDA - MERMA
 * - Stock: 98 → 96
 */