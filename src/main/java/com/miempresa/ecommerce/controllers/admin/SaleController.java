package com.miempresa.ecommerce.controllers.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.miempresa.ecommerce.models.Customer;
import com.miempresa.ecommerce.models.Payment;
import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.models.Sale;
import com.miempresa.ecommerce.models.SaleDetail;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.models.enums.MetodoPago;
import com.miempresa.ecommerce.models.enums.TipoPago;
import com.miempresa.ecommerce.security.SecurityUtils;
import com.miempresa.ecommerce.services.CustomerService;
import com.miempresa.ecommerce.services.ProductService;
import com.miempresa.ecommerce.services.SaleService;
import com.miempresa.ecommerce.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CONTROLLER: VENTAS
 * 
 * Gestiona ventas, POS y pagos.
 */

@Controller
@RequestMapping("/admin/ventas")
@RequiredArgsConstructor
@Slf4j
public class SaleController {

    private final SaleService saleService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final UserService userService;

    // ========================================
    // LISTAR VENTAS
    // ========================================

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas", saleService.obtenerTodas());
        model.addAttribute("titulo", "Gestión de Ventas");
        return "admin/ventas/lista";
    }

    // ========================================
    // PUNTO DE VENTA (POS)
    // ========================================

    @GetMapping("/pos")
    public String mostrarPOS(Model model) {
        log.info("Accediendo al POS");

        model.addAttribute("productos", productService.obtenerActivos());
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("titulo", "Punto de Venta");

        return "admin/ventas/pos";
    }

    // DTOs internos (puedes mover a clases propias si prefieres)
    /* DTOs con JsonAlias para tolerar variantes desde el frontend */
    public static class VentaPosRequest {
        private Long clienteId;
        private String clienteDocumento;
        private List<VentaProducto> productos;
        private BigDecimal subtotal;
        private BigDecimal descuento;
        private BigDecimal costoEnvio;
        private BigDecimal total;
        private String tipoPago;
        private List<PagoRequest> pagos;

        public Long getClienteId() {
            return clienteId;
        }

        public void setClienteId(Long clienteId) {
            this.clienteId = clienteId;
        }

        public String getClienteDocumento() {
            return clienteDocumento;
        }

        public void setClienteDocumento(String clienteDocumento) {
            this.clienteDocumento = clienteDocumento;
        }

        public List<VentaProducto> getProductos() {
            return productos;
        }

        public void setProductos(List<VentaProducto> productos) {
            this.productos = productos;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }

        public BigDecimal getDescuento() {
            return descuento;
        }

        public void setDescuento(BigDecimal descuento) {
            this.descuento = descuento;
        }

        public BigDecimal getCostoEnvio() {
            return costoEnvio;
        }

        public void setCostoEnvio(BigDecimal costoEnvio) {
            this.costoEnvio = costoEnvio;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public String getTipoPago() {
            return tipoPago;
        }

        public void setTipoPago(String tipoPago) {
            this.tipoPago = tipoPago;
        }

        public List<PagoRequest> getPagos() {
            return pagos;
        }

        public void setPagos(List<PagoRequest> pagos) {
            this.pagos = pagos;
        }

    }

    public static class VentaProducto {
        @JsonAlias({ "id", "productoId" })
        private Long id;
        private Integer cantidad;
        private BigDecimal precio;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public BigDecimal getPrecio() {
            return precio;
        }

        public void setPrecio(BigDecimal precio) {
            this.precio = precio;
        }

    }

    public static class PagoRequest {
        @JsonAlias({ "metodo", "metodoPago" })
        private String metodoPago;
        private BigDecimal monto;

        public String getMetodoPago() {
            return metodoPago;
        }

        public void setMetodoPago(String metodoPago) {
            this.metodoPago = metodoPago;
        }

        public BigDecimal getMonto() {
            return monto;
        }

        public void setMonto(BigDecimal monto) {
            this.monto = monto;
        }

    }

    /**
     * Procesa una venta desde el POS
     * 
     * Recibe JSON con:
     * - clienteDocumento
     * - productos [{id, cantidad, precio}]
     * - tipoPago (CONTADO/CREDITO)
     * - pagos [{metodo, monto}]
     * - numCuotas (si es crédito)
     */
    @PostMapping("/pos/registrar")
    @ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> registrarVentaPOS(
            @RequestParam String clienteDocumento,
            @RequestParam String productosJson,
            @RequestParam TipoPago tipoPago,
            @RequestParam(required = false) Integer numCuotas,
            @RequestParam String pagosJson) {

        log.info("Registrando venta desde POS para cliente: {}", clienteDocumento);

        Map<String, Object> response = new java.util.HashMap<>();

        try {
            // ========================================
            // 1. VALIDACIONES INICIALES
            // ========================================

            if (tipoPago == TipoPago.CREDITO && (numCuotas == null || numCuotas < 1)) {
                response.put("success", false);
                response.put("error", "Debe especificar el número de cuotas");
                return org.springframework.http.ResponseEntity.badRequest().body(response);
            }

            // ========================================
            // 2. OBTENER O CREAR CLIENTE
            // ========================================

            Customer cliente = customerService.obtenerOCrearDesdeApi(clienteDocumento);

            // ========================================
            // 3. OBTENER USUARIO ACTUAL
            // ========================================

            String username = SecurityUtils.getCurrentUsername();
            User usuario = userService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // ========================================
            // 4. PARSEAR PRODUCTOS
            // ========================================

            com.google.gson.JsonArray productosArray = com.google.gson.JsonParser
                    .parseString(productosJson)
                    .getAsJsonArray();

            List<SaleDetail> detalles = new java.util.ArrayList<>();

            for (com.google.gson.JsonElement elemento : productosArray) {
                com.google.gson.JsonObject productoJson = elemento.getAsJsonObject();

                // Extraer datos del producto
                Long productoId = productoJson.get("id").getAsLong();
                Integer cantidad = productoJson.get("cantidad").getAsInt();

                // Buscar producto en BD
                Product producto = productService.buscarPorId(productoId)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: ID " + productoId));

                // Validar stock disponible
                if (!producto.hayStock() || producto.getStockActual() < cantidad) {
                    response.put("success", false);
                    response.put("error", "Stock insuficiente para: " + producto.getNombre());
                    return org.springframework.http.ResponseEntity.badRequest().body(response);
                }

                // Crear detalle de venta
                SaleDetail detalle = SaleDetail.builder()
                        .producto(producto)
                        .cantidad(cantidad)
                        .build();

                detalle.establecerDatosProducto();
                detalle.calcularSubtotal();
                detalles.add(detalle);
            }

            // Validar que haya al menos un producto
            if (detalles.isEmpty()) {
                response.put("success", false);
                response.put("error", "Debe agregar al menos un producto");
                return org.springframework.http.ResponseEntity.badRequest().body(response);
            }

            // ========================================
            // 5. PARSEAR PAGOS
            // ========================================

            com.google.gson.JsonArray pagosArray = com.google.gson.JsonParser
                    .parseString(pagosJson)
                    .getAsJsonArray();

            List<Payment> pagos = new java.util.ArrayList<>();
            BigDecimal totalPagado = BigDecimal.ZERO;

            for (com.google.gson.JsonElement elemento : pagosArray) {
                com.google.gson.JsonObject pagoJson = elemento.getAsJsonObject();

                // Extraer datos del pago
                MetodoPago metodo = MetodoPago.valueOf(pagoJson.get("metodo").getAsString());
                BigDecimal monto = new BigDecimal(pagoJson.get("monto").getAsString());
                String referencia = pagoJson.has("referencia")
                        ? pagoJson.get("referencia").getAsString()
                        : null;

                // Validar monto positivo
                if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                    response.put("success", false);
                    response.put("error", "El monto del pago debe ser mayor a cero");
                    return org.springframework.http.ResponseEntity.badRequest().body(response);
                }

                Payment pago = Payment.builder()
                        .metodoPago(metodo)
                        .monto(monto)
                        .referencia(referencia)
                        .usuario(usuario)
                        .build();

                pagos.add(pago);
                totalPagado = totalPagado.add(monto);
            }

            // ========================================
            // 6. CALCULAR TOTAL DE LA VENTA
            // ========================================

            BigDecimal totalVenta = detalles.stream()
                    .map(SaleDetail::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ========================================
            // 7. VALIDAR PAGOS
            // ========================================

            if (tipoPago == TipoPago.CONTADO) {
                // En contado, el total pagado debe ser igual o mayor al total
                if (totalPagado.compareTo(totalVenta) < 0) {
                    response.put("success", false);
                    response.put("error", String.format(
                            "Pago insuficiente. Total: S/ %.2f, Pagado: S/ %.2f",
                            totalVenta, totalPagado));
                    return org.springframework.http.ResponseEntity.badRequest().body(response);
                }
            } else {
                // En crédito, puede haber pago inicial o no
                if (totalPagado.compareTo(totalVenta) > 0) {
                    response.put("success", false);
                    response.put("error", "El pago inicial no puede ser mayor al total");
                    return org.springframework.http.ResponseEntity.badRequest().body(response);
                }
            }

            // ========================================
            // 8. CREAR VENTA
            // ========================================

            Sale venta = Sale.builder()
                    .cliente(cliente)
                    .tipoPago(tipoPago)
                    .build();

            Sale ventaGuardada = saleService.crearVenta(venta, detalles, pagos, usuario, numCuotas);

            log.info("Venta registrada desde POS: {}", ventaGuardada.getNumeroVenta());

            // ========================================
            // 9. PREPARAR RESPUESTA EXITOSA
            // ========================================

            response.put("success", true);
            response.put("mensaje", "Venta registrada correctamente");
            response.put("numeroVenta", ventaGuardada.getNumeroVenta());
            response.put("ventaId", ventaGuardada.getId());
            response.put("total", ventaGuardada.getTotal());

            // Calcular vuelto si es contado
            if (tipoPago == TipoPago.CONTADO) {
                BigDecimal vuelto = totalPagado.subtract(totalVenta);
                response.put("vuelto", vuelto);
            }

            return org.springframework.http.ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error de negocio al registrar venta: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Error inesperado al registrar venta: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Error interno del servidor. Contacte al administrador.");
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    // ========================================
    // VER DETALLE DE VENTA
    // ========================================

    @GetMapping("/ver/{id}")
    public String verDetalle(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {

        var ventaOpt = saleService.buscarPorId(id);

        if (ventaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Venta no encontrada");
            return "redirect:/admin/ventas";
        }

        model.addAttribute("venta", ventaOpt.get());
        model.addAttribute("titulo", "Detalle de Venta");

        return "admin/ventas/detalle";
    }

    // ========================================
    // ANULAR VENTA
    // ========================================

    @PostMapping("/anular/{id}")
    public String anular(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Anulando venta ID: {}", id);

        try {
            String username = SecurityUtils.getCurrentUsername();
            User usuario = userService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            saleService.anularVenta(id, usuario);

            redirectAttributes.addFlashAttribute("success", "Venta anulada correctamente");

        } catch (Exception e) {
            log.error("Error al anular venta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Error al anular: " + e.getMessage());
        }

        return "redirect:/admin/ventas";
    }
}

/**
 * NOTA IMPORTANTE:
 * El método registrarVentaPOS está simplificado.
 * En la implementación real necesitarás:
 * 1. Parsear el JSON de productos usando Gson o Jackson
 * 2. Validar stock de cada producto
 * 3. Crear los SaleDetail correctamente
 * 4. Manejar multipagos
 * 5. Generar PDF de boleta
 */