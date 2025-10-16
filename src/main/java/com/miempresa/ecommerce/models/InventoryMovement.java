package com.miempresa.ecommerce.models;

import com.miempresa.ecommerce.models.enums.MotivoMovimiento;
import com.miempresa.ecommerce.models.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDAD: MOVIMIENTO DE INVENTARIO
 * 
 * Registra todos los movimientos de entrada y salida de stock.
 * Permite tener un historial completo de cambios en el inventario.
 * 
 * Tipos de movimiento:
 * - ENTRADA: Compra, Ajuste positivo, Devolución
 * - SALIDA: Venta, Ajuste negativo, Merma
 */

@Entity
@Table(name = "movimientos_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

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
     * Producto afectado por el movimiento
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Product producto;

    /**
     * Usuario que registró el movimiento
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    // ========================================
    // TIPO Y MOTIVO DEL MOVIMIENTO
    // ========================================

    /**
     * Tipo de movimiento (ENTRADA o SALIDA)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoMovimiento tipo;

    /**
     * Motivo del movimiento
     * (COMPRA, VENTA, AJUSTE, DEVOLUCION, MERMA)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 20)
    private MotivoMovimiento motivo;

    // ========================================
    // CANTIDADES Y STOCK
    // ========================================

    /**
     * Cantidad de unidades movidas
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Stock ANTES del movimiento
     */
    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    /**
     * Stock DESPUÉS del movimiento
     */
    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo;

    // ========================================
    // REFERENCIA
    // ========================================

    /**
     * ID de referencia del documento relacionado
     * - Si es VENTA → id de la venta
     * - Si es COMPRA → id de la compra
     * - Si es AJUSTE → NULL
     */
    @Column(name = "referencia_id")
    private Long referenciaId;

    /**
     * Tipo de referencia
     * Ejemplo: "VENTA", "COMPRA", "AJUSTE"
     */
    @Column(name = "referencia_tipo", length = 50)
    private String referenciaTipo;

    // ========================================
    // OBSERVACIONES
    // ========================================

    /**
     * Observaciones adicionales del movimiento
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // ========================================
    // AUDITORÍA
    // ========================================

    @CreationTimestamp
    @Column(name = "fecha_movimiento", nullable = false, updatable = false)
    private LocalDateTime fechaMovimiento;

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Verifica si es un movimiento de entrada
     */
    public boolean esEntrada() {
        return this.tipo == TipoMovimiento.ENTRADA;
    }

    /**
     * Verifica si es un movimiento de salida
     */
    public boolean esSalida() {
        return this.tipo == TipoMovimiento.SALIDA;
    }

    /**
     * Obtiene la descripción completa del movimiento
     */
    public String getDescripcionCompleta() {
        return String.format("%s - %s: %d unidades (Stock: %d → %d)",
                this.tipo.getNombre(),
                this.motivo.getNombre(),
                this.cantidad,
                this.stockAnterior,
                this.stockNuevo);
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. ¿Por qué registrar todos los movimientos?
 * - Trazabilidad: sabes SIEMPRE quién, cuándo y por qué cambió el stock
 * - Auditoría: puedes revisar el historial completo
 * - Control: detectas errores o robos
 * 
 * 2. ¿Cómo funciona el registro automático?
 * 
 * Ejemplo de VENTA:
 * 1. Cliente compra 3 laptops
 * 2. Producto "Laptop HP" tiene stock = 10
 * 3. Se crea movimiento:
 * - tipo: SALIDA
 * - motivo: VENTA
 * - cantidad: 3
 * - stock_anterior: 10
 * - stock_nuevo: 7
 * - referencia_id: 25 (id de la venta)
 * - referencia_tipo: "VENTA"
 * 4. Stock del producto se actualiza: 10 → 7
 * 
 * Ejemplo de COMPRA a proveedor:
 * 1. Compras 50 laptops al proveedor
 * 2. Producto "Laptop HP" tiene stock = 7
 * 3. Se crea movimiento:
 * - tipo: ENTRADA
 * - motivo: COMPRA
 * - cantidad: 50
 * - stock_anterior: 7
 * - stock_nuevo: 57
 * - referencia_id: 12 (id de la compra)
 * - referencia_tipo: "COMPRA"
 * 4. Stock del producto se actualiza: 7 → 57
 * 
 * 3. ¿Para qué sirven stockAnterior y stockNuevo?
 * - Permiten ver el cambio exacto
 * - Si hay discrepancias, puedes detectarlas
 * - Ejemplo: Si el stock debería ser 57 pero está en 50, algo falló
 * 
 * 4. ¿Qué es referenciaId y referenciaTipo?
 * - Conectan el movimiento con el documento que lo originó
 * - Puedes hacer clic en un movimiento y ver la venta/compra completa
 * - Facilita la auditoría
 */