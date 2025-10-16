package com.miempresa.ecommerce.models;

import com.miempresa.ecommerce.models.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ENTIDAD: PAGO
 * 
 * Representa los pagos realizados para una venta.
 * Puede haber múltiples pagos para una venta (multipago).
 * 
 * Casos de uso:
 * 1. VENTA AL CONTADO: Uno o varios pagos que suman el total
 * 2. VENTA A CRÉDITO: Abonos que se distribuyen entre cuotas
 */

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

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
     * Venta a la que pertenece este pago
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    @ToString.Exclude
    private Sale venta;

    /**
     * Crédito relacionado (si el pago es abono a crédito)
     * NULL si la venta es al contado
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credito_id")
    @ToString.Exclude
    private Credit credito;

    /**
     * Usuario que registró el pago
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    /**
     * Distribución del pago entre cuotas (solo si es abono a crédito)
     */
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentInstallment> distribucionCuotas = new ArrayList<>();

    // ========================================
    // INFORMACIÓN DEL PAGO
    // ========================================

    /**
     * Monto del pago
     */
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    /**
     * Método de pago utilizado
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    /**
     * Número de referencia (operación, transacción, etc.)
     */
    @Column(name = "referencia", length = 100)
    private String referencia;

    /**
     * Observaciones del pago
     */
    @Column(name = "observaciones", length = 255)
    private String observaciones;

    // ========================================
    // AUDITORÍA
    // ========================================

    @CreationTimestamp
    @Column(name = "fecha_pago", nullable = false, updatable = false)
    private LocalDateTime fechaPago;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Agrega una distribución de pago a cuota
     */
    public void agregarDistribucion(PaymentInstallment distribucion) {
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

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Cómo funcionan los MULTIPAGOS?
 * 
 * Ejemplo: Total de venta = S/ 500
 * 
 * Pago 1:
 * - monto: 300
 * - metodoPago: EFECTIVO
 * - referencia: null
 * 
 * Pago 2:
 * - monto: 150
 * - metodoPago: YAPE
 * - referencia: "OP-123456789"
 * 
 * Pago 3:
 * - monto: 50
 * - metodoPago: TARJETA
 * - referencia: "VISA-1234"
 * 
 * Total pagos: 300 + 150 + 50 = 500 ✅
 * 
 * 2. ¿Cómo funcionan los ABONOS A CRÉDITO?
 * 
 * Escenario: Venta a crédito de S/ 1200 en 12 cuotas
 * 
 * Cliente abona S/ 300:
 * - Se crea un PAGO de S/ 300
 * - El pago se distribuye proporcionalmente entre las 12 cuotas
 * - S/ 300 ÷ 12 = S/ 25 por cuota
 * - Se crean 12 registros en PaymentInstallment
 * - Cada cuota reduce su monto pendiente en S/ 25
 * 
 * 3. ¿Para qué sirve el campo "referencia"?
 * - Para pagos con tarjeta: número de operación bancaria
 * - Para Yape/transferencia: código de operación
 * - Para efectivo: puede estar vacío
 * - Facilita la verificación y auditoría
 * 
 * 4. Tabla en base de datos:
 * 
 * | id | venta_id | credito_id | usuario_id | monto | metodo_pago | referencia
 * | fecha_pago |
 * |----|----------|------------|------------|--------|-------------|------------|---------------------|
 * | 1 | 15 | NULL | 3 | 300.00 | EFECTIVO | NULL | 2025-10-16 14:30:00 |
 * | 2 | 15 | NULL | 3 | 150.00 | YAPE | OP-123456 | 2025-10-16 14:30:00 |
 * | 3 | 15 | NULL | 3 | 50.00 | TARJETA | VISA-1234 | 2025-10-16 14:30:00 |
 * | 4 | 18 | 5 | 2 | 300.00 | EFECTIVO | NULL | 2025-10-17 10:15:00 |
 */