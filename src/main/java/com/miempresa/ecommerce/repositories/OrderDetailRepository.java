package com.miempresa.ecommerce.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.OrderDetail;

/**
 * REPOSITORY: DETALLE DE PEDIDO
 * 
 * Interface para acceder a la tabla 'pedido_detalles'.
 */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    /**
     * Busca los detalles de un pedido.
     */
    List<OrderDetail> findByPedidoId(Long pedidoId);

    /**
     * Busca detalles de un producto específico.
     */
    List<OrderDetail> findByProductoId(Long productoId);

    /**
     * Elimina todos los detalles de un pedido.
     */
    void deleteByPedidoId(Long pedidoId);

    /**
     * Cuenta cuántos detalles tiene un pedido.
     */
    long countByPedidoId(Long pedidoId);

    /**
     * Obtiene los productos más pedidos.
     */
    @Query("""
            SELECT d.producto.id,
                   d.producto.nombre,
                   SUM(d.cantidad) AS total
            FROM OrderDetail d
            GROUP BY d.producto.id, d.producto.nombre
            ORDER BY total DESC
            """)
    List<Object[]> obtenerProductosMasPedidos();
}
