package com.miempresa.ecommerce.services;

import com.miempresa.ecommerce.models.CartItem;
import com.miempresa.ecommerce.models.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar el carrito de compras
 * Usa @SessionScope para mantener una instancia por sesión HTTP
 */
@Service
@SessionScope
@RequiredArgsConstructor
@Slf4j
public class CartService implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ProductService productService;
    private final List<CartItem> items = new ArrayList<>();

    /**
     * Agrega un producto al carrito
     */
    public void agregar(Long productoId, Integer cantidad) {
        log.info("Agregando producto ID {} con cantidad {} al carrito", productoId, cantidad);

        // Buscar producto
        Product producto = productService.buscarPorId(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar stock
        if (!producto.hayStock() || producto.getStockActual() < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        // Verificar si ya existe en el carrito
        Optional<CartItem> existente = items.stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();

        if (existente.isPresent()) {
            // Actualizar cantidad
            CartItem item = existente.get();
            int nuevaCantidad = item.getCantidad() + cantidad;

            if (nuevaCantidad > producto.getStockActual()) {
                throw new RuntimeException("No hay suficiente stock disponible");
            }

            item.setCantidad(nuevaCantidad);
            log.info("Cantidad actualizada para producto {} a {}", productoId, nuevaCantidad);
        } else {
            // Agregar nuevo item
            String imagenUrl = null;
            if (producto.getImagenPrincipal() != null) {
                imagenUrl = "/uploads/productos/" + producto.getImagenPrincipal().getUrl();
            }

            CartItem nuevoItem = CartItem.builder()
                    .productoId(producto.getId())
                    .nombre(producto.getNombre())
                    .precio(producto.getPrecioActual())
                    .cantidad(cantidad)
                    .imagenUrl(imagenUrl)
                    .stockDisponible(producto.getStockActual())
                    .build();

            items.add(nuevoItem);
            log.info("Nuevo producto agregado al carrito: {}", producto.getNombre());
        }
    }

    /**
     * Actualiza la cantidad de un producto
     */
    public void actualizarCantidad(Long productoId, Integer cantidad) {
        log.info("Actualizando cantidad del producto {} a {}", productoId, cantidad);

        CartItem item = items.stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el carrito"));

        if (cantidad <= 0) {
            eliminar(productoId);
            return;
        }

        // Verificar stock
        Product producto = productService.buscarPorId(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (cantidad > producto.getStockActual()) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStockActual());
        }

        item.setCantidad(cantidad);
    }

    /**
     * Elimina un producto del carrito
     */
    public void eliminar(Long productoId) {
        log.info("Eliminando producto {} del carrito", productoId);
        items.removeIf(item -> item.getProductoId().equals(productoId));
    }

    /**
     * Limpia todo el carrito
     */
    public void limpiar() {
        log.info("Limpiando carrito completo");
        items.clear();
    }

    /**
     * Obtiene todos los items del carrito
     */
    public List<CartItem> obtenerItems() {
        return new ArrayList<>(items);
    }

    /**
     * Obtiene la cantidad total de items
     */
    public int obtenerCantidadTotal() {
        return items.stream()
                .mapToInt(CartItem::getCantidad)
                .sum();
    }

    /**
     * Calcula el subtotal del carrito
     */
    public BigDecimal calcularSubtotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calcula el total con envío
     */
    public BigDecimal calcularTotal(BigDecimal costoEnvio) {
        BigDecimal subtotal = calcularSubtotal();
        if (costoEnvio == null) {
            costoEnvio = BigDecimal.ZERO;
        }
        return subtotal.add(costoEnvio)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Verifica si el carrito está vacío
     */
    public boolean estaVacio() {
        return items.isEmpty();
    }

    /**
     * Verifica stock de todos los productos
     */
    public boolean verificarStockDisponible() {
        for (CartItem item : items) {
            Product producto = productService.buscarPorId(item.getProductoId())
                    .orElse(null);

            if (producto == null || !producto.hayStock() || producto.getStockActual() < item.getCantidad()) {
                log.warn("Stock insuficiente para producto: {}", item.getNombre());
                return false;
            }
        }
        return true;
    }
}
