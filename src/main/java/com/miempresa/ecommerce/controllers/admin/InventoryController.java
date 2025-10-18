package com.miempresa.ecommerce.controllers.admin;

import com.miempresa.ecommerce.security.SecurityUtils;
import com.miempresa.ecommerce.services.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.InventoryMovement;
import com.miempresa.ecommerce.models.enums.MotivoMovimiento;
import com.miempresa.ecommerce.models.enums.TipoMovimiento;

@Controller
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    private final InventoryMovementService inventoryMovementService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public String listar(@RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String tipo,
            Model model) {
        List<InventoryMovement> movimientos;

        if (productoId != null) {
            movimientos = inventoryMovementService.obtenerPorProducto(productoId);
        } else if (tipo != null) {
            movimientos = inventoryMovementService.obtenerPorTipo(
                    TipoMovimiento.valueOf(tipo));
        } else {
            movimientos = inventoryMovementService.obtenerUltimos();
        }

        model.addAttribute("movimientos", movimientos);
        model.addAttribute("productos", productService.obtenerActivos());
        model.addAttribute("titulo", "Movimientos de Inventario");
        return "admin/inventario/lista";
    }

    @GetMapping("/registrar-entrada")
    public String mostrarFormularioEntrada(Model model) {
        model.addAttribute("productos", productService.obtenerActivos());
        model.addAttribute("motivos", MotivoMovimiento.values());
        model.addAttribute("titulo", "Registrar Entrada");
        return "admin/inventario/entrada-form";
    }

    @PostMapping("/registrar-entrada")
    public String registrarEntrada(@RequestParam Long productoId,
            @RequestParam Integer cantidad,
            @RequestParam String motivo,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes redirectAttributes) {
        try {
            String username = SecurityUtils.getCurrentUsername();
            var usuario = userService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            inventoryMovementService.registrarEntrada(
                    productoId,
                    cantidad,
                    MotivoMovimiento.valueOf(motivo),
                    usuario,
                    observaciones);

            redirectAttributes.addFlashAttribute("success", "Entrada registrada correctamente");
        } catch (Exception e) {
            log.error("Error al registrar entrada: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/inventario";
    }

    @GetMapping("/registrar-salida")
    public String mostrarFormularioSalida(Model model) {
        model.addAttribute("productos", productService.obtenerActivos());
        model.addAttribute("motivos", MotivoMovimiento.values());
        model.addAttribute("titulo", "Registrar Salida");
        return "admin/inventario/salida-form";
    }

    @PostMapping("/registrar-salida")
    public String registrarSalida(@RequestParam Long productoId,
            @RequestParam Integer cantidad,
            @RequestParam String motivo,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes redirectAttributes) {
        try {
            String username = SecurityUtils.getCurrentUsername();
            var usuario = userService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            inventoryMovementService.registrarSalida(
                    productoId,
                    cantidad,
                    MotivoMovimiento.valueOf(motivo),
                    usuario,
                    observaciones);

            redirectAttributes.addFlashAttribute("success", "Salida registrada correctamente");
        } catch (Exception e) {
            log.error("Error al registrar salida: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/inventario";
    }

    @GetMapping("/alertas")
    public String verAlertas(Model model) {
        model.addAttribute("stockBajo", productService.obtenerProductosStockBajo());
        model.addAttribute("sinStock", productService.obtenerProductosSinStock());
        model.addAttribute("titulo", "Alertas de Inventario");
        return "admin/inventario/alertas";
    }
}