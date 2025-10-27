package com.miempresa.ecommerce.controllers.admin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders; // <<--- AÑADIDO
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // <<--- AÑADIDO
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.miempresa.ecommerce.config.EmpresaConfig; // <<--- AÑADIDO
import com.miempresa.ecommerce.controllers.admin.SaleController.PagoRequest;
import com.miempresa.ecommerce.controllers.admin.SaleController.VentaProducto;
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
import com.miempresa.ecommerce.utils.PdfGeneratorUtil; // <<--- AÑADIDO

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
    private final EmpresaConfig empresaConfig; // <<--- AÑADIDO (Asegúrate de que esté inyectado)

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
        private Integer numCuotas; // ✅ AGREGADO

        // Getters y Setters
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

        public Integer getNumCuotas() {
            return numCuotas;
        }

        public void setNumCuotas(Integer numCuotas) {
            this.numCuotas = numCuotas;
        }

        // toString para logging (opcional, cuidado con datos sensibles)
        @Override
        public String toString() {
            return "VentaPosRequest{" +
                    "clienteDocumento='" + clienteDocumento + '\'' +
                    ", tipoPago='" + tipoPago + '\'' +
                    ", numCuotas=" + numCuotas +
                    ", descuento=" + descuento +
                    ", costoEnvio=" + costoEnvio +
                    ", total=" + total +
                    ", productos=" + (productos != null ? productos.size() : 0) + " items" +
                    ", pagos=" + (pagos != null ? pagos.size() : 0) + " items" +
                    '}';
        }
    }

    public static class VentaProducto {
        @JsonAlias({ "id", "productoId" })
        private Long id;
        private Integer cantidad;
        private BigDecimal precio;

        // Getters y Setters
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

        // Getters y Setters
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
     * - descuento (opcional)
     * - costoEnvio (opcional)
     */
    @PostMapping("/pos/registrar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarVentaPOS(
            @RequestBody VentaPosRequest request) {

        log.info("🔵 Registrando venta desde POS");
        log.info("📦 Request recibido: {}", request); // toString implementado en VentaPosRequest

        Map<String, Object> response = new HashMap<>();

        try {
            // ========================================
            // 1. VALIDACIONES INICIALES
            // ========================================
            if (request.getTipoPago() == null || request.getTipoPago().isBlank()) {
                response.put("success", false);
                response.put("error", "Debe especificar el tipo de pago");
                return ResponseEntity.badRequest().body(response);
            }

            TipoPago tipoPagoEnum; // Renombrado para claridad
            try {
                tipoPagoEnum = TipoPago.valueOf(request.getTipoPago());
            } catch (IllegalArgumentException e) {
                response.put("success", false);
                response.put("error", "Tipo de pago inválido: " + request.getTipoPago());
                return ResponseEntity.badRequest().body(response);
            }

            Integer numCuotas = null;
            if (tipoPagoEnum == TipoPago.CREDITO) {
                numCuotas = request.getNumCuotas();
                if (numCuotas == null || numCuotas < 1 || numCuotas > 24) {
                    response.put("success", false);
                    response.put("error", "Para ventas a crédito debe especificar entre 1 y 24 cuotas");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            log.info("✅ Tipo de pago: {} | Cuotas: {}", tipoPagoEnum, numCuotas);

            // ========================================
            // 2. VALIDAR PRODUCTOS
            // ========================================
            if (request.getProductos() == null || request.getProductos().isEmpty()) {
                response.put("success", false);
                response.put("error", "Debe incluir al menos un producto");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("✅ Productos en request: {}", request.getProductos().size());

            // ========================================
            // 3. OBTENER CLIENTE (ESPECÍFICO o GENÉRICO)
            // ========================================
            Customer cliente;

            if (request.getClienteDocumento() != null && !request.getClienteDocumento().isBlank()) {
                try {
                    cliente = customerService.obtenerOCrearDesdeApi(request.getClienteDocumento());
                    log.info("✅ Cliente específico encontrado/creado: {} ({})",
                            cliente.getNombreCompleto(), cliente.getNumeroDocumento());
                } catch (Exception e) {
                    log.error("❌ Error al buscar/crear cliente específico", e);
                    response.put("success", false);
                    response.put("error", "Error al procesar cliente: " + e.getMessage());
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                try {
                    cliente = customerService.obtenerClienteGenerico();
                    log.info("✅ Usando Cliente Genérico: {}", cliente.getNombreCompleto());
                } catch (RuntimeException e) {
                    log.error("❌ Error CRÍTICO al obtener cliente genérico", e);
                    response.put("success", false);
                    response.put("error", "Error interno: Cliente genérico no encontrado.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }

            if (cliente == null) {
                log.error("❌ Error inesperado: El objeto cliente es nulo después de la lógica.");
                response.put("success", false);
                response.put("error", "Error interno: No se pudo determinar el cliente.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // ========================================
            // 4. OBTENER USUARIO ACTUAL
            // ========================================
            String username = SecurityUtils.getCurrentUsername();
            User usuario = userService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            log.info("✅ Usuario: {}", usuario.getUsername());

            // ========================================
            // 5. PROCESAR PRODUCTOS
            // ========================================
            List<SaleDetail> detalles = new ArrayList<>();
            BigDecimal subtotalCalculado = BigDecimal.ZERO;

            for (VentaProducto productoReq : request.getProductos()) {
                if (productoReq.getId() == null) {
                    response.put("success", false);
                    response.put("error", "ID de producto no puede ser nulo");
                    return ResponseEntity.badRequest().body(response);
                }

                if (productoReq.getCantidad() == null || productoReq.getCantidad() <= 0) {
                    response.put("success", false);
                    response.put("error", "Cantidad debe ser mayor a cero");
                    return ResponseEntity.badRequest().body(response);
                }

                Product producto = productService.buscarPorId(productoReq.getId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: ID " + productoReq.getId()));

                if (!producto.hayStock() || producto.getStockActual() < productoReq.getCantidad()) {
                    response.put("success", false);
                    response.put("error", "Stock insuficiente para: " + producto.getNombre());
                    return ResponseEntity.badRequest().body(response);
                }

                SaleDetail detalle = SaleDetail.builder()
                        .producto(producto)
                        .cantidad(productoReq.getCantidad())
                        .build();

                detalle.establecerDatosProducto();
                detalle.calcularSubtotal();
                detalles.add(detalle);

                subtotalCalculado = subtotalCalculado.add(detalle.getSubtotal());

                log.info("✅ Producto agregado: {} x{} = S/ {}",
                        producto.getNombre(),
                        productoReq.getCantidad(),
                        detalle.getSubtotal());
            }

            log.info("✅ Subtotal calculado: S/ {}", subtotalCalculado);

            // ========================================
            // 6. PROCESAR PAGOS
            // ========================================
            List<Payment> pagos = new ArrayList<>();
            BigDecimal totalPagado = BigDecimal.ZERO;

            if (tipoPagoEnum == TipoPago.CONTADO) {
                if (request.getPagos() == null || request.getPagos().isEmpty()) {
                    response.put("success", false);
                    response.put("error", "Debe especificar al menos un método de pago");
                    return ResponseEntity.badRequest().body(response);
                }

                for (PagoRequest pagoReq : request.getPagos()) {
                    if (pagoReq.getMetodoPago() == null || pagoReq.getMetodoPago().isBlank()) {
                        response.put("success", false);
                        response.put("error", "Método de pago no puede estar vacío");
                        return ResponseEntity.badRequest().body(response);
                    }

                    MetodoPago metodo;
                    try {
                        metodo = MetodoPago.valueOf(pagoReq.getMetodoPago());
                    } catch (IllegalArgumentException e) {
                        response.put("success", false);
                        response.put("error", "Método de pago inválido: " + pagoReq.getMetodoPago());
                        return ResponseEntity.badRequest().body(response);
                    }

                    BigDecimal monto = pagoReq.getMonto();
                    if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                        response.put("success", false);
                        response.put("error", "El monto del pago debe ser mayor a cero");
                        return ResponseEntity.badRequest().body(response);
                    }

                    Payment pago = Payment.builder()
                            .metodoPago(metodo)
                            .monto(monto)
                            // usuario se asigna en el servicio
                            .build();

                    pagos.add(pago);
                    totalPagado = totalPagado.add(monto);

                    log.info("✅ Pago agregado: {} - S/ {}", metodo, monto);
                }

                // Validar pago suficiente contra el TOTAL esperado del request
                BigDecimal totalEsperado = request.getTotal() != null ? request.getTotal() : BigDecimal.ZERO;
                if (totalPagado.compareTo(totalEsperado) < 0) {
                    log.warn("Pago insuficiente. Total Esperado: S/ {}, Pagado: S/ {}", totalEsperado, totalPagado);
                    response.put("success", false);
                    response.put("error", String.format(
                            "Pago insuficiente. Total Venta: S/ %.2f, Monto Pagado: S/ %.2f",
                            totalEsperado, totalPagado));
                    return ResponseEntity.badRequest().body(response);
                }

                log.info("✅ Total pagado: S/ {}", totalPagado);
            } else {
                log.info("✅ Venta a CRÉDITO, sin pagos iniciales o procesados dentro del crédito");
            }

            // ========================================
            // 7. CREAR VENTA
            // ========================================
            Sale venta = Sale.builder()
                    .cliente(cliente)
                    .tipoPago(tipoPagoEnum) // Usar el Enum
                    .build();

            log.info("🔄 Llamando a saleService.crearVenta()...");

            Sale ventaGuardada;
            try {
                // --- INICIO DE LA INTEGRACIÓN ---
                // Corregido: Pasar descuento y costoEnvio al servicio
                ventaGuardada = saleService.crearVenta(
                        venta,
                        detalles,
                        pagos,
                        usuario,
                        request.getNumCuotas(), // Puede ser null si es CONTADO
                        request.getDescuento(), // <<--- PASAR DESCUENTO DEL REQUEST
                        request.getCostoEnvio() // <<--- PASAR COSTOENVIO DEL REQUEST
                );
                // --- FIN DE LA INTEGRACIÓN ---
                log.info("✅ Venta creada exitosamente: {}", ventaGuardada.getNumeroVenta());
            } catch (Exception e) {
                log.error("❌ Error al crear venta en el servicio", e);
                response.put("success", false);
                response.put("error", "Error al procesar la venta: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // ========================================
            // 8. RESPUESTA EXITOSA
            // ========================================
            response.put("success", true);
            response.put("mensaje", "Venta registrada correctamente");
            response.put("numeroVenta", ventaGuardada.getNumeroVenta());
            response.put("ventaId", ventaGuardada.getId());
            response.put("total", ventaGuardada.getTotal()); // Usar el total calculado por el backend

            // Calcular vuelto basado en el total pagado y el total final de la venta
            if (tipoPagoEnum == TipoPago.CONTADO) {
                BigDecimal vuelto = totalPagado.subtract(ventaGuardada.getTotal());
                // Asegurarse de que el vuelto no sea negativo (aunque la validación anterior
                // debería prevenirlo)
                if (vuelto.compareTo(BigDecimal.ZERO) < 0) {
                    log.warn(
                            "Se detectó un vuelto negativo (S/ {}), esto no debería ocurrir. TotalPagado={}, TotalVenta={}",
                            vuelto, totalPagado, ventaGuardada.getTotal());
                    vuelto = BigDecimal.ZERO; // Forzar a cero si es negativo
                }
                response.put("vuelto", vuelto.setScale(2, java.math.RoundingMode.HALF_UP)); // Redondear vuelto
                log.info("Calculando vuelto: Total Pagado S/ {}, Total Venta S/ {}, Vuelto S/ {}", totalPagado,
                        ventaGuardada.getTotal(), vuelto);
            }

            log.info("✅ Respuesta enviada al frontend: {}", response);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("❌ Error de negocio al registrar venta: {}", e.getMessage(), e); // Log stacktrace
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("❌ Error inesperado al registrar venta", e);
            response.put("success", false);
            response.put("error", "Error interno del servidor. Contacte al administrador.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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

    // ========================================
    // IMPRIMIR BOLETA
    // ========================================
    @GetMapping("/imprimir/{id}")
    public ResponseEntity<byte[]> imprimirBoleta(@PathVariable Long id) {
        log.info("Solicitud para imprimir boleta de venta ID: {}", id);
        try {
            Sale venta = saleService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

            byte[] pdfBytes = PdfGeneratorUtil.generateBoletaPdf(
                    venta,
                    empresaConfig.getNombre(),
                    empresaConfig.getRuc(),
                    empresaConfig.getDireccion());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "Boleta-" + venta.getNumeroVenta() + ".pdf";
            headers.setContentDispositionFormData("inline", filename); // inline para ver en navegador
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("Boleta PDF generada para venta {}, enviando respuesta.", venta.getNumeroVenta());
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (RuntimeException re) {
            log.error("Error al buscar venta para imprimir PDF (ID {}): {}", id, re.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 si no se encuentra
        } catch (Exception e) {
            log.error("Error al generar PDF de boleta para venta ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
