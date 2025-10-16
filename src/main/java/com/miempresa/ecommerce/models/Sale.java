package com.miempresa.ecommerce.models;

import com.miempresa.ecommerce.models.enums.EstadoVenta;
import com.miempresa.ecommerce.models.enums.TipoPago;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD: VENTA
 * 
 * Representa una venta confirmada y registrada.
 * Se crea cuando un admin confirma un pedido o vende directamente en el POS.
 * 
 * Diferencias con Pedido:
 * - La venta SÍ descuenta stock
 * - La venta SÍ genera comprobante
 * - La venta registra forma de pago
 * - La venta puede ser al CONTADO o a CRÉDITO
 */

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // NÚMERO DE VENTA
    // ========================================

    /**
     * Número único de venta
     * Formato: VEN-2025-00001
     * Se usa para comprobantes
     */
    @Column(name = "numero_venta", nullable = false, unique = true, length = 20)
    private String numeroVenta;

    // ========================================
    // RELACIONES
    // ========================================

    /**
     * Cliente que compró
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Customer cliente;

    /**
     * Usuario (admin/trabajador) que registró la venta
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    /**
     * Pedido origen (si la venta proviene de un pedido web)
     * Puede ser NULL si la venta es directa del POS
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Order pedido;

    /**
     * Detalle de la venta (productos vendidos)
     */
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleDetail> detalles = new ArrayList<>();

    /**
     * Pagos realizados (puede ser múltiple si es multipago)
     */
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> pagos = new ArrayList<>();

    // ========================================
    // MONTOS
    // ========================================

    /**
     * Subtotal (suma de productos)
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Descuento aplicado
     */
    @Column(name = "descuento", precision = 10, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    /**
     * Costo de envío
     */
    @Column(name = "costo_envio", precision = 10, scale = 2)
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    /**
     * IGV (18% en Perú)
     */
    @Column(name = "igv", precision = 10, scale = 2)
    private BigDecimal igv;

    /**
     * Total de la venta
     */
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // ========================================
    // TIPO Y FORMA DE PAGO
    // ========================================

    /**
     * Tipo de pago (CONTADO o CREDITO)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false, length = 10)
    private TipoPago tipoPago;

    // ========================================
    // ESTADO
    // ========================================

    /**
     * Estado de la venta (COMPLETADA o ANULADA)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    // ========================================
    // COMPROBANTE
    // ========================================

    /**
     * Ruta del archivo PDF del comprobante
     */
    @Column(name = "ruta_comprobante", length = 255)
    private String rutaComprobante;

    // ========================================
    // OBSERVACIONES
    // ========================================

    /**
     * Observaciones de la venta
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // ========================================
    // AUDITORÍA
    // ========================================

    @CreationTimestamp
    @Column(name = "fecha_venta", nullable = false, updatable = false)
    private LocalDateTime fechaVenta;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Fecha de anulación (si aplica)
     */
    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Agrega un detalle a la venta
     */
    public void agregarDetalle(SaleDetail detalle) {
        this.detalles.add(detalle);
        detalle.setVenta(this);
    }

    /**
     * Agrega un pago a la venta
     */
    public void agregarPago(Payment pago) {
        this.pagos.add(pago);
        pago.setVenta(this);
    }

    /**
     * Calcula el total de la venta
     */
    public void calcularTotal() {
        // Subtotal de productos
        this.subtotal = this.detalles.stream()
                .map(SaleDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Base imponible (subtotal - descuento + envío)
        BigDecimal baseImponible = this.subtotal
                .subtract(this.descuento != null ? this.descuento : BigDecimal.ZERO)
                .add(this.costoEnvio != null ? this.costoEnvio : BigDecimal.ZERO);

        // IGV (18%)
        this.igv = baseImponible.multiply(new BigDecimal("0.18"));

        // Total
        this.total = baseImponible.add(this.igv);
    }

    /**
     * Verifica si la venta está completada
     */
    public boolean estaCompletada() {
        return this.estado == EstadoVenta.COMPLETADA;
    }

    /**
     * Verifica si la venta está anulada
     */
    public boolean estaAnulada() {
        return this.estado == EstadoVenta.ANULADA;
    }

    /**
     * Anula la venta
     */
    public void anular() {
        this.estado = EstadoVenta.ANULADA;
        this.fechaAnulacion = LocalDateTime.now();
    }

    /**
     * Verifica si la venta es al contado
     */
    public boolean esContado() {
        return this.tipoPago == TipoPago.CONTADO;
    }

    /**
     * Verifica si la venta es a crédito
     */
    public boolean esCredito() {
        return this.tipoPago == TipoPago.CREDITO;
    }

    /**
     * Calcula el monto total pagado
     */
    public BigDecimal getMontoTotalPagado() {
        return this.pagos.stream()
                .map(Payment::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Cuándo se crea una VENTA?
 * 
 * Escenario 1: Desde un pedido web
 * - Cliente crea pedido → PENDIENTE
 * - Admin confirma pedido → CONFIRMADO
 * - Admin convierte a venta → Se crea VENTA
 * 
 * Escenario 2: Venta directa en POS
 * - Admin registra venta directamente
 * - Se crea VENTA sin pedido previo
 * 
 * 2. ¿Cómo funciona el IGV?
 * 
 * Cálculo:
 * Subtotal: S/ 1000
 * Descuento: S/ 100
 * Envío: S/ 20
 * Base imponible: 1000 - 100 + 20 = S/ 920
 * IGV (18%): 920 * 0.18 = S/ 165.60
 * Total: 920 + 165.60 = S/ 1085.60
 * 
 * 3. ¿Qué es multipago?
 * 
 * Ejemplo:
 * Total: S/ 500
 * Pago 1: Efectivo S/ 300
 * Pago 2: Yape S/ 150
 * Pago 3: Tarjeta S/ 50
 * 
 * Se crean 3 registros en la tabla "pagos"
 * 
 * 4. ¿Qué sucede al anular una venta?
 * - Estado cambia a ANULADA
 * - Se devuelve el stock (movimiento de inventario ENTRADA)
 * - Si es a crédito, se cancelan las cuotas pendientes
 */