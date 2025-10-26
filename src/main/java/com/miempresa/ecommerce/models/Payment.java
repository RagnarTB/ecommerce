package com.miempresa.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Importar
import com.miempresa.ecommerce.models.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @ToString.Exclude
    private Sale venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credito_id")
    @ToString.Exclude
    private Credit credito;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonManagedReference("payment-installments")
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentInstallment> distribucionCuotas = new ArrayList<>();

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_pago", nullable = false, updatable = false)
    private LocalDateTime fechaPago;

    public void agregarDistribucion(PaymentInstallment distribucion) {
        if (this.distribucionCuotas == null)
            this.distribucionCuotas = new ArrayList<>();
        this.distribucionCuotas.add(distribucion);
        distribucion.setPago(this);
    }

    /**
     * Verifica si el pago es al contado
     */
    public boolean esContado() {
        return this.credito == null;
    }

    /**
     * Verifica si el pago es abono a crédito
     */
    public boolean esAbonoCredito() {
        return this.credito != null;
    }
}