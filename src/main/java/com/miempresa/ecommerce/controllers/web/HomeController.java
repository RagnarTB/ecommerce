package com.miempresa.ecommerce.controllers.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.miempresa.ecommerce.models.Order;
import com.miempresa.ecommerce.models.OrderDetail;
import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.services.CategoryService;
import com.miempresa.ecommerce.services.ConfigurationService;
import com.miempresa.ecommerce.services.CustomerService;
import com.miempresa.ecommerce.services.OrderService;
import com.miempresa.ecommerce.services.ProductService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CONTROLLER: HOME (Catálogo Público)
 * 
 * Maneja el catálogo público de productos.
 */

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final ConfigurationService configurationService;

    // ========================================
    // PÁGINA PRINCIPAL
    // ========================================

    @GetMapping({ "/", "/home" })
    public String home(Model model) {
        log.debug("Accediendo a la página principal");

        model.addAttribute("productosDestacados",
                productService.obtenerDestacados());
        model.addAttribute("categorias",
                categoryService.obtenerActivas());
        model.addAttribute("config",
                configurationService.obtenerTodasComoMapa());

        return "web/home";
    }

    // ========================================
    // CATÁLOGO DE PRODUCTOS
    // ========================================

    @GetMapping("/catalogo")
    public String catalogo(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String busqueda,
            Model model) {

        List<Product> productos;

        if (busqueda != null && !busqueda.isEmpty()) {
            productos = productService.buscarPorNombre(busqueda);
        } else if (categoriaId != null) {
            productos = productService.buscarPorCategoria(categoriaId);
        } else {
            productos = productService.obtenerActivos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoryService.obtenerActivas());
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("config", configurationService.obtenerTodasComoMapa());

        return "web/catalogo";
    }

    // ========================================
    // DETALLE DE PRODUCTO
    // ========================================

    @GetMapping("/producto/{id}")
    public String detalleProducto(@PathVariable Long id, Model model) {
        var productoOpt = productService.buscarPorId(id);

        if (productoOpt.isEmpty()) {
            return "redirect:/catalogo";
        }

        Product producto = productoOpt.get();

        model.addAttribute("producto", producto);
        model.addAttribute("relacionados",
                productService.buscarPorCategoria(producto.getCategoria().getId()));
        model.addAttribute("config", configurationService.obtenerTodasComoMapa());

        return "web/producto-detalle";
    }

    // ========================================
    // CARRITO DE COMPRAS
    // ========================================

    @PostMapping("/carrito/agregar")
    public String agregarAlCarrito(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            HttpSession session) {

        log.info("Agregando producto {} al carrito", productoId);

        // Obtener carrito de la sesión
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");

        if (carrito == null) {
            carrito = new HashMap<>();
        }

        // Agregar o actualizar cantidad
        carrito.merge(productoId, cantidad, Integer::sum);

        session.setAttribute("carrito", carrito);

        return "redirect:/carrito";
    }

    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");

        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("carritoVacio", true);
            return "web/carrito";
        }

        // Cargar productos del carrito
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
            var productoOpt = productService.buscarPorId(entry.getKey());

            if (productoOpt.isPresent()) {
                Product producto = productoOpt.get();
                Integer cantidad = entry.getValue();
                BigDecimal subtotal = producto.getPrecioActual()
                        .multiply(BigDecimal.valueOf(cantidad));

                Map<String, Object> item = new HashMap<>();
                item.put("producto", producto);
                item.put("cantidad", cantidad);
                item.put("subtotal", subtotal);

                items.add(item);
                total = total.add(subtotal);
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("config", configurationService.obtenerTodasComoMapa());

        return "web/carrito";
    }

    // ========================================
    // CHECKOUT
    // ========================================

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        // Similar a verCarrito pero muestra formulario de datos
        model.addAttribute("config", configurationService.obtenerTodasComoMapa());
        return "web/checkout";
    }

    @PostMapping("/checkout/procesar")
    public String procesarPedido(
            @RequestParam String documento,
            @RequestParam String tipoEntrega,
            @RequestParam(required = false) String direccion,
            HttpSession session,
            Model model) {

        log.info("Procesando pedido para documento: {}", documento);

        try {
            // Obtener o crear cliente
            var cliente = customerService.obtenerOCrearDesdeApi(documento);

            // Obtener carrito
            @SuppressWarnings("unchecked")
            Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");

            if (carrito == null || carrito.isEmpty()) {
                throw new RuntimeException("El carrito está vacío");
            }

            // Crear orden
            Order order = Order.builder()
                    .cliente(cliente)
                    .tipoEntrega(com.miempresa.ecommerce.models.enums.TipoEntrega.valueOf(tipoEntrega))
                    .direccionEntrega(direccion)
                    .build();

            // Crear detalles
            List<OrderDetail> detalles = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
                var producto = productService.buscarPorId(entry.getKey())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                OrderDetail detalle = OrderDetail.builder()
                        .producto(producto)
                        .cantidad(entry.getValue())
                        .build();

                detalles.add(detalle);
            }

            // Crear pedido
            Order pedidoCreado = orderService.crearPedido(order, detalles);

            // Limpiar carrito
            session.removeAttribute("carrito");

            log.info("Pedido creado: {}", pedidoCreado.getNumeroPedido());

            model.addAttribute("pedido", pedidoCreado);
            model.addAttribute("mensaje", "¡Pedido realizado con éxito!");
            model.addAttribute("config", configurationService.obtenerTodasComoMapa());

            return "web/pedido-exitoso";

        } catch (Exception e) {
            log.error("Error al procesar pedido: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            return "web/checkout";
        }
    }

    // ========================================
    // ELIMINAR DEL CARRITO
    // ========================================

    @PostMapping("/carrito/eliminar/{productoId}")
    public String eliminarDelCarrito(@PathVariable Long productoId,
            HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");

        if (carrito != null) {
            carrito.remove(productoId);
            session.setAttribute("carrito", carrito);
        }

        return "redirect:/carrito";
    }

    // ========================================
    // ACTUALIZAR CANTIDAD
    // ========================================

    @PostMapping("/carrito/actualizar")
    public String actualizarCantidad(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");

        if (carrito != null && cantidad > 0) {
            carrito.put(productoId, cantidad);
            session.setAttribute("carrito", carrito);
        }

        return "redirect:/carrito";
    }
}

/**
 * EXPLICACIÓN DEL FLUJO DE COMPRA WEB:
 * 
 * 1. Cliente navega por el catálogo:
 * GET / → HomeController.home()
 * GET /catalogo → HomeController.catalogo()
 * GET /producto/5 → HomeController.detalleProducto()
 * 
 * 2. Cliente agrega productos al carrito:
 * POST /carrito/agregar → Se guarda en HttpSession
 * Carrito = Map<productoId, cantidad>
 * Ejemplo: {5: 2, 8: 1, 12: 3}
 * 
 * 3. Cliente ve su carrito:
 * GET /carrito → Muestra productos con total
 * 
 * 4. Cliente va al checkout:
 * GET /checkout → Formulario de datos
 * 
 * 5. Cliente completa y envía:
 * POST /checkout/procesar
 * - Busca/crea cliente con API Decolecta
 * - Crea Order con OrderDetails
 * - Estado: PENDIENTE
 * - Limpia carrito
 * - Muestra confirmación
 * 
 * 6. Admin revisa pedido:
 * GET /admin/pedidos
 * - Ve pedido PENDIENTE
 * - Confirma: POST /admin/pedidos/confirmar/5
 * - Convierte a venta: POST /admin/pedidos/convertir-venta/5
 * - Se descuenta stock
 * - Se genera boleta
 * 
 * ¿Por qué usar HttpSession para el carrito?
 * - No requiere login del cliente
 * - Rápido y simple
 * - Se pierde al cerrar navegador (comportamiento esperado)
 * - No ocupa espacio en BD
 * 
 * Alternativa para persistir:
 * - Guardar carrito en cookies (más complejo)
 * - Guardar en BD (requiere identificar cliente)
 */