package com.miempresa.ecommerce.models;

import com.miempresa.ecommerce.models.enums.EstadoPedido;
import com.miempresa.ecommerce.models.enums.TipoEntrega;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD: PEDIDO
 * 
 * Representa los pedidos creados por los clientes desde la web.
 * Un pedido es la solicitud inicial que luego se convierte en venta.
 * 
 * Flujo:
 * 1. Cliente crea pedido desde web → Estado: PENDIENTE
 * 2. Admin revisa y confirma → Estado: CONFIRMADO
 * 3. Admin convierte a venta → Se crea registro en tabla "ventas"
 */

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // NÚMERO DE PEDIDO
    // ========================================

    /**
     * Número único de pedido
     * Formato: PED-2025-00001
     * Se genera automáticamente
     */
    @Column(name = "numero_pedido", nullable = false, unique = true, length = 20)
    private String numeroPedido;

    // ========================================
    // RELACIONES
    // ========================================

    /**
     * Cliente que hizo el pedido
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Customer cliente;

    /**
     * Detalle del pedido (lista de productos)
     * 
     * @OneToMany - Un pedido tiene MUCHOS detalles
     */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetail> detalles = new ArrayList<>();

    // ========================================
    // MONTOS
    // ========================================

    /**
     * Subtotal (suma de productos sin incluir envío)
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Costo de envío
     */
    @Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    /**
     * Total a pagar (subtotal + envío)
     */
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // ========================================
    // ENTREGA
    // ========================================

    /**
     * Tipo de entrega (RECOJO_TIENDA o DELIVERY)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", nullable = false, length = 20)
    private TipoEntrega tipoEntrega;

    /**
     * Dirección de entrega (solo si es DELIVERY)
     */
    @Column(name = "direccion_entrega", length = 300)
    private String direccionEntrega;

    /**
     * Referencia de ubicación
     * Ejemplo: "Casa blanca con reja negra, al lado de la bodega"
     */
    @Column(name = "referencia_entrega", length = 255)
    private String referenciaEntrega;

    // ========================================
    // ESTADO
    // ========================================

    /**
     * Estado del pedido (PENDIENTE, CONFIRMADO, CANCELADO)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    // ========================================
    // OBSERVACIONES
    // ========================================

    /**
     * Notas o comentarios del cliente
     */
    @Column(name = "notas_cliente", columnDefinition = "TEXT")
    private String notasCliente;

    /**
     * Observaciones del admin
     */
    @Column(name = "observaciones_admin", columnDefinition = "TEXT")
    private String observacionesAdmin;

    // ========================================
    // AUDITORÍA
    // ========================================

    @CreationTimestamp
    @Column(name = "fecha_pedido", nullable = false, updatable = false)
    private LocalDateTime fechaPedido;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Fecha de confirmación del pedido
     */
    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Agrega un detalle al pedido
     */
    public void agregarDetalle(OrderDetail detalle) {
        this.detalles.add(detalle);
        detalle.setPedido(this);
    }

    /**
     * Calcula el total del pedido
     */
    public void calcularTotal() {
        this.subtotal = this.detalles.stream()
                .map(OrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.total = this.subtotal.add(this.costoEnvio);
    }

    /**
     * Verifica si el pedido está pendiente
     */
    public boolean estaPendiente() {
        return this.estado == EstadoPedido.PENDIENTE;
    }

    /**
     * Verifica si el pedido está confirmado
     */
    public boolean estaConfirmado() {
        return this.estado == EstadoPedido.CONFIRMADO;
    }

    /**
     * Confirma el pedido
     */
    public void confirmar() {
        this.estado = EstadoPedido.CONFIRMADO;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    /**
     * Cancela el pedido
     */
    public void cancelar() {
        this.estado = EstadoPedido.CANCELADO;
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Cuál es la diferencia entre PEDIDO y VENTA?
 * 
 * PEDIDO:
 * - Creado por el cliente desde la web
 * - Estado inicial: PENDIENTE
 * - NO descuenta stock
 * - NO genera comprobante
 * 
 * VENTA:
 * - Creada por el admin al confirmar un pedido
 * - SÍ descuenta stock
 * - SÍ genera comprobante (boleta/factura)
 * - Registra forma de pago
 * 
 * 2. ¿Por qué tener PEDIDO y VENTA separados?
 * - El cliente puede abandonar el pedido (no confirmar)
 * - El admin puede revisar antes de aceptar (verificar stock, precio, datos)
 * - Evita ventas falsas o problemas de stock
 * 
 * 3. ¿Cómo se genera el número de pedido?
 * - Formato: PED-2025-00001
 * - Se genera automáticamente en el Service
 * - Es único y secuencial
 * 
 * 4. ¿Qué es el método calcularTotal()?
 * - Suma todos los subtotales de los detalles
 * - Añade el costo de envío
 * - Actualiza el total del pedido
 * 
 * Ejemplo:
 * Producto 1: 100 x 2 = 200
 * Producto 2: 50 x 3 = 150
 * Subtotal: 350
 * Envío: 15
 * Total: 365
 */