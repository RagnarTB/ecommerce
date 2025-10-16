package com.miempresa.ecommerce.controllers.admin;

import com.miempresa.ecommerce.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * CONTROLLER: CRÉDITOS
 */

@Controller
@RequestMapping("/admin/creditos")
@RequiredArgsConstructor
@Slf4j
public class CreditController {

    private final CreditService creditService;
    private final UserService userService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("creditos", creditService.obtenerActivos());
        model.addAttribute("titulo", "Gestión de Créditos");
        return "admin/creditos/lista";
    }

    @GetMapping("/ver/{id}")
    public String verDetalle(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        var creditoOpt = creditService.buscarPorId(id);

        if (creditoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Crédito no encontrado");
            return "redirect:/admin/creditos";
        }

        model.addAttribute("credito", creditoOpt.get());
        model.addAttribute("cuotas",
                creditService.obtenerCuotasDeCredito(id));
        model.addAttribute("titulo", "Detalle del Crédito");

        return "admin/creditos/detalle";
    }

    @GetMapping("/cuotas-vencidas")
    public String cuotasVencidas(Model model) {
        model.addAttribute("cuotas", creditService.obtenerCuotasVencidas());
        model.addAttribute("titulo", "Cuotas Vencidas");
        return "admin/creditos/cuotas-vencidas";
    }

    @PostMapping("/registrar-abono")
    public String registrarAbono(
            @RequestParam Long creditoId,
            @RequestParam BigDecimal monto,
            @RequestParam String metodoPago,
            @RequestParam(required = false) String referencia,
            RedirectAttributes redirectAttributes) {

        log.info("Registrando abono de {} al crédito {}", monto, creditoId);

        try {
            String username = com.miempresa.ecommerce.security.SecurityUtils.getCurrentUsername();
            var usuario = userService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            creditService.registrarAbono(creditoId, monto,
                    com.miempresa.ecommerce.models.enums.MetodoPago.valueOf(metodoPago),
                    referencia, usuario);

            redirectAttributes.addFlashAttribute("success", "Abono registrado");

        } catch (Exception e) {
            log.error("Error al registrar abono: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/creditos/ver/" + creditoId;
    }
}