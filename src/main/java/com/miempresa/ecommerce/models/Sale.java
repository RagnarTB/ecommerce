package com.miempresa.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Importar
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

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "numero_venta", nullable = false, unique = true, length = 20)
    private String numeroVenta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Customer cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Order pedido;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonManagedReference("sale-details") // Identificador opcional si hay múltiples referencias
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleDetail> detalles = new ArrayList<>();

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonManagedReference("sale-payments") // Identificador opcional
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> pagos = new ArrayList<>();

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "descuento", precision = 10, scale = 2)
    @Builder.Default // Asegurar que sea 0 si no se especifica
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "costo_envio", precision = 10, scale = 2)
    @Builder.Default // Asegurar que sea 0 si no se especifica
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @Column(name = "igv", precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false, length = 10)
    private TipoPago tipoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default // Asegurar estado inicial
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    @Column(name = "ruta_comprobante", length = 255)
    private String rutaComprobante;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_venta", nullable = false, updatable = false)
    private LocalDateTime fechaVenta;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    // Métodos útiles (sin cambios)...
    // <<--- CÓDIGO DE MÉTODOS ÚTILES OMITIDO POR BREVEDAD --->>
    /**
     * Agrega un detalle a la venta
     */
    public void agregarDetalle(SaleDetail detalle) {
        if (this.detalles == null)
            this.detalles = new ArrayList<>();
        this.detalles.add(detalle);
        detalle.setVenta(this);
    }

    /**
     * Agrega un pago a la venta
     */
    public void agregarPago(Payment pago) {
        if (this.pagos == null)
            this.pagos = new ArrayList<>();
        this.pagos.add(pago);
        pago.setVenta(this);
    }

    /**
     * Calcula el total de la venta
     */
    public void calcularTotal() {
        // Subtotal de productos
        this.subtotal = BigDecimal.ZERO; // Inicializar
        if (this.detalles != null) {
            this.subtotal = this.detalles.stream()
                    .map(SaleDetail::getSubtotal)
                    .filter(java.util.Objects::nonNull) // Evitar NullPointerException
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Asegurar que descuento y costoEnvio no sean null
        BigDecimal desc = this.descuento != null ? this.descuento : BigDecimal.ZERO;
        BigDecimal envio = this.costoEnvio != null ? this.costoEnvio : BigDecimal.ZERO;

        // Base imponible (subtotal - descuento + envío)
        BigDecimal baseImponible = this.subtotal.subtract(desc).add(envio);

        // IGV (18%) - Asegurar que sea positivo
        this.igv = baseImponible.max(BigDecimal.ZERO).multiply(new BigDecimal("0.18"))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // Total
        this.total = baseImponible.add(this.igv)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        ;
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
        if (this.pagos == null)
            return BigDecimal.ZERO;
        return this.pagos.stream()
                .map(Payment::getMonto)
                .filter(java.util.Objects::nonNull) // Evitar NullPointerException
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}