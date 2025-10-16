package com.miempresa.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * ENTIDAD: PAGO-CUOTA (Distribución de pagos)
 * 
 * Representa cómo se distribuye un pago entre las cuotas de un crédito.
 * 
 * ¿Por qué existe esta tabla?
 * Cuando un cliente abona S/ 300 a un crédito de 12 cuotas,
 * necesitamos saber exactamente cuánto se aplicó a cada cuota.
 * 
 * Ejemplo:
 * Pago #25 de S/ 300 se distribuye así:
 * - S/ 25 a Cuota 1
 * - S/ 25 a Cuota 2
 * - S/ 25 a Cuota 3
 * - ... (12 registros en total)
 */

@Entity
@Table(name = "pago_cuota")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInstallment {

    // ========================================
    // CLAVE PRIMARIA
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ========================================
    // RELACIONES
    // ========================================

    /**
     * Pago del cual se distribuye el monto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    @ToString.Exclude
    private Payment pago;

    /**
     * Cuota a la que se aplica parte del pago
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuota_id", nullable = false)
    @ToString.Exclude
    private Installment cuota;

    // ========================================
    // INFORMACIÓN
    // ========================================

    /**
     * Monto del pago aplicado a esta cuota específica
     */
    @Column(name = "monto_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoAplicado;
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Para qué sirve esta tabla intermedia?
 * 
 * Sin esta tabla:
 * - No sabrías exactamente cuánto de un pago fue a cada cuota
 * - Dificulta la auditoría
 * - No puedes rastrear el origen de los abonos
 * 
 * Con esta tabla:
 * - Trazabilidad total: cada peso aplicado a cada cuota
 * - Puedes ver: "El pago #25 aplicó S/ 25 a la cuota 1"
 * - Facilita reportes y conciliaciones
 * 
 * 2. Ejemplo completo:
 * 
 * Crédito de S/ 1200 en 12 cuotas de S/ 100 c/u
 * Cliente abona S/ 300 (Pago #25)
 * 
 * Se crean 12 registros en pago_cuota:
 * 
 * | id | pago_id | cuota_id | monto_aplicado |
 * |----|---------|----------|----------------|
 * | 1 | 25 | 1 | 25.00 |
 * | 2 | 25 | 2 | 25.00 |
 * | 3 | 25 | 3 | 25.00 |
 * | 4 | 25 | 4 | 25.00 |
 * | 5 | 25 | 5 | 25.00 |
 * | 6 | 25 | 6 | 25.00 |
 * | 7 | 25 | 7 | 25.00 |
 * | 8 | 25 | 8 | 25.00 |
 * | 9 | 25 | 9 | 25.00 |
 * | 10 | 25 | 10 | 25.00 |
 * | 11 | 25 | 11 | 25.00 |
 * | 12 | 25 | 12 | 25.00 |
 * 
 * Total: S/ 300 (25 x 12)
 * 
 * 3. Flujo de creación:
 * 
 * ```java
 * // Cliente abona S/ 300
 * Payment pago = new Payment();
 * pago.setMonto(new BigDecimal("300"));
 * 
 * // Distribuir entre cuotas
 * credito.aplicarPago(new BigDecimal("300"));
 * 
 * // Para cada cuota, crear registro
 * for (Installment cuota : credito.getCuotas()) {
 * PaymentInstallment distribucion = PaymentInstallment.builder()
 * .pago(pago)
 * .cuota(cuota)
 * .montoAplicado(new BigDecimal("25"))
 * .build();
 * 
 * pago.agregarDistribucion(distribucion);
 * }
 * ```
 * 
 * 4. Diagrama de relaciones:
 * 
 * PAYMENT (Pago)
 * ↓ (1 pago tiene N distribuciones)
 * PAYMENT_INSTALLMENT (Distribución)
 * ↓ (cada distribución va a 1 cuota)
 * INSTALLMENT (Cuota)
 * ↓ (cada cuota pertenece a 1 crédito)
 * CREDIT (Crédito)
 */