package com.miempresa.ecommerce.controllers.admin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.Credit;
import com.miempresa.ecommerce.models.Installment;
import com.miempresa.ecommerce.services.CreditService;
import com.miempresa.ecommerce.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        List<Credit> creditosActivos = creditService.obtenerActivos();
        BigDecimal deudaTotal = creditService.obtenerTotalDeudaPendiente();
        List<Installment> cuotasVencidas = creditService.obtenerCuotasVencidas();

        model.addAttribute("creditos", creditosActivos); // ✅ Corregido: cambiar a "creditos" para coincidir con la vista
        model.addAttribute("deudaTotal", deudaTotal != null ? deudaTotal : BigDecimal.ZERO);
        model.addAttribute("cuotasVencidasCount", cuotasVencidas.size());
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
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registrarAbono(
            @RequestParam Long creditoId,
            @RequestParam BigDecimal monto,
            @RequestParam String metodoPago,
            @RequestParam(required = false) String referencia,
            HttpServletRequest request) {

        log.info("Registrando abono de {} al crédito {}", monto, creditoId);

        Map<String, Object> response = new HashMap<>();

        try {
            String username = com.miempresa.ecommerce.security.SecurityUtils.getCurrentUsername();
            var usuario = userService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            creditService.registrarAbono(creditoId, monto,
                    com.miempresa.ecommerce.models.enums.MetodoPago.valueOf(metodoPago),
                    referencia, usuario);

            response.put("success", true);
            response.put("message", "Abono registrado correctamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al registrar abono: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}