package com.miempresa.ecommerce.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.Order;
import com.miempresa.ecommerce.models.enums.EstadoPedido;

/**
 * REPOSITORY: PEDIDO
 * 
 * Interface para acceder a la tabla 'pedidos'.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

        /**
         * Busca un pedido por su número.
         */
        Optional<Order> findByNumeroPedido(String numeroPedido);

        /**
         * Busca pedidos por estado.
         */
        List<Order> findByEstadoOrderByFechaPedidoDesc(EstadoPedido estado);

        /**
         * Busca todos los pedidos ordenados por fecha (sin filtrar por estado).
         */
        List<Order> findAllByOrderByFechaPedidoDesc();

        /**
         * Busca pedidos de un cliente.
         */
        List<Order> findByClienteIdOrderByFechaPedidoDesc(Long clienteId);

        /**
         * Busca pedidos en un rango de fechas.
         */
        List<Order> findByFechaPedidoBetweenOrderByFechaPedidoDesc(
                        LocalDateTime fechaInicio,
                        LocalDateTime fechaFin);

        /**
         * Busca pedidos de un cliente por estado.
         */
        List<Order> findByClienteIdAndEstadoOrderByFechaPedidoDesc(
                        Long clienteId,
                        EstadoPedido estado);

        /**
         * Cuenta pedidos por estado.
         */
        long countByEstado(EstadoPedido estado);

        /**
         * Obtiene los últimos pedidos creados.
         */
        List<Order> findTop20ByOrderByFechaPedidoDesc();

        /**
         * Busca pedidos con filtros múltiples (cliente, estado, fechas).
         */
        @Query("""
                        SELECT o FROM Order o
                        WHERE (:clienteId IS NULL OR o.cliente.id = :clienteId)
                          AND (:estado IS NULL OR o.estado = :estado)
                          AND (:fechaInicio IS NULL OR o.fechaPedido >= :fechaInicio)
                          AND (:fechaFin IS NULL OR o.fechaPedido <= :fechaFin)
                        ORDER BY o.fechaPedido DESC
                        """)
        List<Order> buscarConFiltros(
                        @Param("clienteId") Long clienteId,
                        @Param("estado") EstadoPedido estado,
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        /**
         * Genera el siguiente número de pedido (según prefijo).
         */
        @Query("""
                        SELECT COALESCE(MAX(CAST(SUBSTRING(o.numeroPedido, 10) AS long)), 0) + 1
                        FROM Order o
                        WHERE o.numeroPedido LIKE :prefijo
                        """)
        Long generarSiguienteNumero(@Param("prefijo") String prefijo);
}
