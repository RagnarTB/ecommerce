package com.miempresa.ecommerce.controllers.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.Customer;
import com.miempresa.ecommerce.models.Payment;
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
    public String registrarVentaPOS(
            @RequestParam String clienteDocumento,
            @RequestParam String productosJson,
            @RequestParam TipoPago tipoPago,
            @RequestParam(required = false) Integer numCuotas,
            @RequestParam String pagosJson,
            RedirectAttributes redirectAttributes) {

        log.info("Registrando venta desde POS para cliente: {}", clienteDocumento);

        try {
            // Obtener o crear cliente
            Customer cliente = customerService.obtenerOCrearDesdeApi(clienteDocumento);

            // Obtener usuario actual
            String username = SecurityUtils.getCurrentUsername();
            User usuario = userService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Parsear productos y crear detalles
            // (Aquí iría la lógica de parseo del JSON)
            List<SaleDetail> detalles = new ArrayList<>();

            // Parsear pagos
            List<Payment> pagos = new ArrayList<>();

            // Crear venta
            Sale venta = Sale.builder()
                    .cliente(cliente)
                    .tipoPago(tipoPago)
                    .build();

            Sale ventaGuardada = saleService.crearVenta(venta, detalles, pagos, usuario);

            log.info("Venta registrada: {}", ventaGuardada.getNumeroVenta());

            return "{\"success\": true, \"mensaje\": \"Venta registrada\", " +
                    "\"numeroVenta\": \"" + ventaGuardada.getNumeroVenta() + "\"}";

        } catch (Exception e) {
            log.error("Error al registrar venta: {}", e.getMessage(), e);
            return "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
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