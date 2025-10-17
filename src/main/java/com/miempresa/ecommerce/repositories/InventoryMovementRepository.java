package com.miempresa.ecommerce.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.InventoryMovement;
import com.miempresa.ecommerce.models.enums.MotivoMovimiento;
import com.miempresa.ecommerce.models.enums.TipoMovimiento;

/**
 * REPOSITORY: MOVIMIENTO DE INVENTARIO
 * 
 * Interface para acceder a la tabla 'movimientos_inventario'.
 */
@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

        /**
         * Busca movimientos de un producto específico.
         */
        List<InventoryMovement> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);

        /**
         * Busca movimientos por tipo.
         */
        List<InventoryMovement> findByTipoOrderByFechaMovimientoDesc(TipoMovimiento tipo);

        /**
         * Busca movimientos por motivo.
         */
        List<InventoryMovement> findByMotivoOrderByFechaMovimientoDesc(MotivoMovimiento motivo);

        /**
         * Busca movimientos en un rango de fechas.
         */
        List<InventoryMovement> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                        LocalDateTime fechaInicio,
                        LocalDateTime fechaFin);

        /**
         * Busca movimientos de un producto en un rango de fechas.
         */
        List<InventoryMovement> findByProductoIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                        Long productoId,
                        LocalDateTime fechaInicio,
                        LocalDateTime fechaFin);

        /**
         * Busca movimientos por usuario.
         */
        List<InventoryMovement> findByUsuarioIdOrderByFechaMovimientoDesc(Long usuarioId);

        /**
         * Busca movimientos con filtros múltiples (dinámicos).
         */
        @Query("""
                        SELECT m FROM InventoryMovement m
                        WHERE (:productoId IS NULL OR m.producto.id = :productoId)
                          AND (:tipo IS NULL OR m.tipo = :tipo)
                          AND (:motivo IS NULL OR m.motivo = :motivo)
                          AND (:fechaInicio IS NULL OR m.fechaMovimiento >= :fechaInicio)
                          AND (:fechaFin IS NULL OR m.fechaMovimiento <= :fechaFin)
                        ORDER BY m.fechaMovimiento DESC
                        """)
        List<InventoryMovement> buscarConFiltros(
                        @Param("productoId") Long productoId,
                        @Param("tipo") TipoMovimiento tipo,
                        @Param("motivo") MotivoMovimiento motivo,
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        /**
         * Obtiene los últimos movimientos registrados.
         */
        List<InventoryMovement> findTop20ByOrderByFechaMovimientoDesc();

        /**
         * Cuenta movimientos por tipo.
         */
        long countByTipo(TipoMovimiento tipo);

        /**
         * Busca movimientos relacionados a una referencia específica.
         * Ejemplo: todos los movimientos de la venta #25.
         */
        List<InventoryMovement> findByReferenciaIdAndReferenciaTipo(Long referenciaId, String referenciaTipo);
}
