package com.miempresa.ecommerce.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.enums.EstadoPedido;
import com.miempresa.ecommerce.models.enums.TipoPago;
import com.miempresa.ecommerce.security.SecurityUtils;
import com.miempresa.ecommerce.services.OrderService;
import com.miempresa.ecommerce.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CONTROLLER: PEDIDOS
 * 
 * Gestiona pedidos creados desde la web.
 */

@Controller
@RequestMapping("/admin/pedidos")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    // ========================================
    // LISTAR PEDIDOS
    // ========================================

    @GetMapping
    public String listar(
            @RequestParam(required = false) EstadoPedido estado,
            Model model) {

        if (estado != null) {
            model.addAttribute("pedidos", orderService.obtenerPorEstado(estado));
        } else {
            model.addAttribute("pedidos", orderService.obtenerTodos());
        }

        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("titulo", "Gestión de Pedidos");

        return "admin/pedidos/lista";
    }

    // ========================================
    // VER DETALLE
    // ========================================

    @GetMapping("/ver/{id}")
    public String verDetalle(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {

        var pedidoOpt = orderService.buscarPorId(id);

        if (pedidoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
            return "redirect:/admin/pedidos";
        }

        model.addAttribute("pedido", pedidoOpt.get());
        model.addAttribute("titulo", "Detalle del Pedido");

        return "admin/pedidos/detalle";
    }

    // ========================================
    // CONFIRMAR PEDIDO
    // ========================================

    @PostMapping("/confirmar/{id}")
    public String confirmar(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        log.info("Confirmando pedido ID: {}", id);

        try {
            orderService.confirmarPedido(id);
            redirectAttributes.addFlashAttribute("success",
                    "Pedido confirmado correctamente");

        } catch (Exception e) {
            log.error("Error al confirmar pedido: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/pedidos/ver/" + id;
    }

    // ========================================
    // CONVERTIR A VENTA - ✅ CORREGIDO
    // ========================================

    @PostMapping("/convertir-venta/{id}")
    public String convertirAVenta(
            @PathVariable Long id,
            @RequestParam TipoPago tipoPago, // ✅ AGREGADO
            @RequestParam(required = false) Integer numCuotas, // ✅ AGREGADO
            RedirectAttributes redirectAttributes) {

        log.info("Convirtiendo pedido ID: {} a venta", id);

        try {
            String username = SecurityUtils.getCurrentUsername();
            var usuario = userService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // ✅ Validar que si es crédito tenga número de cuotas
            if (tipoPago == com.miempresa.ecommerce.models.enums.TipoPago.CREDITO) {
                if (numCuotas == null || numCuotas < 1) {
                    redirectAttributes.addFlashAttribute("error",
                            "Debe especificar el número de cuotas");
                    return "redirect:/admin/pedidos/ver/" + id;
                }
            }

            var venta = orderService.convertirAVenta(id, usuario, tipoPago, numCuotas); // ✅ PARÁMETROS AGREGADOS

            redirectAttributes.addFlashAttribute("success",
                    "Pedido convertido a venta: " + venta.getNumeroVenta());

            return "redirect:/admin/ventas/ver/" + venta.getId();

        } catch (Exception e) {
            log.error("Error al convertir pedido: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/pedidos/ver/" + id;
        }
    }

    // ========================================
    // CANCELAR PEDIDO
    // ========================================

    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        log.info("Cancelando pedido ID: {}", id);

        try {
            orderService.cancelarPedido(id);
            redirectAttributes.addFlashAttribute("success",
                    "Pedido cancelado correctamente");

        } catch (Exception e) {
            log.error("Error al cancelar pedido: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/pedidos";
    }
}