package com.miempresa.ecommerce.controllers.web;

import com.miempresa.ecommerce.models.CartItem;
import com.miempresa.ecommerce.models.Order;
import com.miempresa.ecommerce.models.OrderDetail;
import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.models.enums.EstadoPedido;
import com.miempresa.ecommerce.models.enums.TipoEntrega;
import com.miempresa.ecommerce.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para el carrito de compras y checkout
 */
@Controller
@RequestMapping("/carrito")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final ConfigurationService configurationService;

    /**
     * Vista del carrito de compras
     */
    @GetMapping
    public String verCarrito(Model model) {
        model.addAttribute("items", cartService.obtenerItems());
        model.addAttribute("subtotal", cartService.calcularSubtotal());
        model.addAttribute("cantidadTotal", cartService.obtenerCantidadTotal());
        model.addAttribute("config", configurationService.obtenerTodasComoMapa());
        model.addAttribute("titulo", "Carrito de Compras");
        return "web/carrito";
    }

    /**
     * Agregar producto al carrito (AJAX)
     */
    @PostMapping("/agregar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarAlCarrito(
            @RequestParam Long productoId,
            @RequestParam(defaultValue = "1") Integer cantidad) {

        Map<String, Object> response = new HashMap<>();

        try {
            cartService.agregar(productoId, cantidad);

            response.put("success", true);
            response.put("message", "Producto agregado al carrito");
            response.put("cantidadTotal", cartService.obtenerCantidadTotal());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al agregar producto al carrito", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Actualizar cantidad de un producto (AJAX)
     */
    @PostMapping("/actualizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarCantidad(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {

        Map<String, Object> response = new HashMap<>();

        try {
            cartService.actualizarCantidad(productoId, cantidad);

            response.put("success", true);
            response.put("subtotal", cartService.calcularSubtotal());
            response.put("cantidadTotal", cartService.obtenerCantidadTotal());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al actualizar cantidad", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Eliminar producto del carrito (AJAX)
     */
    @PostMapping("/eliminar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarProducto(@RequestParam Long productoId) {
        Map<String, Object> response = new HashMap<>();

        try {
            cartService.eliminar(productoId);

            response.put("success", true);
            response.put("subtotal", cartService.calcularSubtotal());
            response.put("cantidadTotal", cartService.obtenerCantidadTotal());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al eliminar producto", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtener cantidad total del carrito (AJAX)
     */
    @GetMapping("/cantidad")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerCantidad() {
        Map<String, Object> response = new HashMap<>();
        response.put("cantidadTotal", cartService.obtenerCantidadTotal());
        return ResponseEntity.ok(response);
    }

    /**
     * Vista de checkout
     */
    @GetMapping("/checkout")
    public String checkout(Model model) {
        if (cartService.estaVacio()) {
            return "redirect:/carrito";
        }

        model.addAttribute("items", cartService.obtenerItems());
        model.addAttribute("subtotal", cartService.calcularSubtotal());
        model.addAttribute("config", configurationService.obtenerTodasComoMapa());
        model.addAttribute("titulo", "Finalizar Compra");

        return "web/checkout";
    }

    /**
     * Procesar pedido
     */
    @PostMapping("/procesar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> procesarPedido(@RequestBody Map<String, Object> datos) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validar carrito
            if (cartService.estaVacio()) {
                response.put("success", false);
                response.put("error", "El carrito está vacío");
                return ResponseEntity.badRequest().body(response);
            }

            if (!cartService.verificarStockDisponible()) {
                response.put("success", false);
                response.put("error", "Algunos productos no tienen stock suficiente");
                return ResponseEntity.badRequest().body(response);
            }

            // Extraer datos del cliente
            String documento = (String) datos.get("documento");
            String nombres = (String) datos.get("nombres");
            String apellidos = (String) datos.get("apellidos");
            String telefono = (String) datos.get("telefono");
            String email = (String) datos.get("email");
            String tipoEntrega = (String) datos.get("tipoEntrega");
            String direccion = (String) datos.get("direccion");
            String referencia = (String) datos.get("referencia");
            String notas = (String) datos.get("notas");

            // Obtener o crear cliente
            var cliente = customerService.obtenerOCrearDesdeWeb(
                documento, nombres, apellidos, telefono, email);

            // Calcular costo de envío
            BigDecimal costoEnvio = BigDecimal.ZERO;
            if ("DELIVERY".equals(tipoEntrega)) {
                String costoEnvioStr = configurationService.obtener("costo_envio_lima")
                        .orElse("15.00");
                costoEnvio = new BigDecimal(costoEnvioStr);
            }

            // Crear pedido
            Order pedido = Order.builder()
                    .cliente(cliente)
                    .tipoEntrega(TipoEntrega.valueOf(tipoEntrega))
                    .direccionEntrega(direccion)
                    .referenciaEntrega(referencia)
                    .notasCliente(notas)
                    .costoEnvio(costoEnvio)
                    .estado(EstadoPedido.PENDIENTE)
                    .build();

            // Agregar detalles desde el carrito
            for (CartItem item : cartService.obtenerItems()) {
                Product producto = productService.buscarPorId(item.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                OrderDetail detalle = OrderDetail.builder()
                        .producto(producto)
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getPrecio())
                        .build();

                detalle.calcularSubtotal();
                pedido.agregarDetalle(detalle);
            }

            // Calcular total y guardar
            pedido.calcularTotal();
            Order pedidoGuardado = orderService.crearPedidoWeb(pedido);

            // Limpiar carrito
            cartService.limpiar();

            // Respuesta exitosa
            response.put("success", true);
            response.put("message", "Pedido creado exitosamente");
            response.put("numeroPedido", pedidoGuardado.getNumeroPedido());
            response.put("pedidoId", pedidoGuardado.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al procesar pedido", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Vista de confirmación de pedido
     */
    @GetMapping("/confirmacion/{pedidoId}")
    public String confirmacion(@PathVariable Long pedidoId, Model model) {
        Order pedido = orderService.buscarPorId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        model.addAttribute("pedido", pedido);
        model.addAttribute("config", configurationService.obtenerTodasComoMapa());
        model.addAttribute("titulo", "Pedido Confirmado");

        return "web/confirmacion";
    }
}
