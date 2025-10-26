package com.miempresa.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Importar
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

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "numero_pedido", nullable = false, unique = true, length = 20)
    private String numeroPedido;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Customer cliente;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonManagedReference("order-details")
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetail> detalles = new ArrayList<>();

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrega", nullable = false, length = 20)
    private TipoEntrega tipoEntrega;

    @Column(name = "direccion_entrega", length = 300)
    private String direccionEntrega;

    @Column(name = "referencia_entrega", length = 255)
    private String referenciaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "notas_cliente", columnDefinition = "TEXT")
    private String notasCliente;

    @Column(name = "observaciones_admin", columnDefinition = "TEXT")
    private String observacionesAdmin;

    @CreationTimestamp
    @Column(name = "fecha_pedido", nullable = false, updatable = false)
    private LocalDateTime fechaPedido;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    // Métodos útiles (sin cambios)...
    // <<--- CÓDIGO DE MÉTODOS ÚTILES OMITIDO POR BREVEDAD --->>
    /**
     * Agrega un detalle al pedido
     */
    public void agregarDetalle(OrderDetail detalle) {
        if (this.detalles == null)
            this.detalles = new ArrayList<>();
        this.detalles.add(detalle);
        detalle.setPedido(this);
    }

    /**
     * Calcula el total del pedido
     */
    public void calcularTotal() {
        this.subtotal = BigDecimal.ZERO; // Inicializar
        if (this.detalles != null) {
            this.subtotal = this.detalles.stream()
                    .map(OrderDetail::getSubtotal)
                    .filter(java.util.Objects::nonNull) // Evitar NullPointerException
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Asegurar que costoEnvio no sea null
        BigDecimal envio = this.costoEnvio != null ? this.costoEnvio : BigDecimal.ZERO;
        this.total = this.subtotal.add(envio)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        ;
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