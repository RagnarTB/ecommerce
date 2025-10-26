package com.miempresa.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonBackReference; // Importar
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pago_cuota")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    @JsonBackReference("payment-installments")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    @ToString.Exclude
    private Payment pago;

    // <<--- ANOTACIÓN AÑADIDA AQUÍ --->>
    // No necesita JsonBackReference aquí si Installment no tiene lista de
    // PaymentInstallment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuota_id", nullable = false)
    @ToString.Exclude
    private Installment cuota;

    @Column(name = "monto_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoAplicado;
}