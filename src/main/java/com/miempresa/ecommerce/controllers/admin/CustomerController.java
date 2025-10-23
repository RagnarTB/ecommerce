package com.miempresa.ecommerce.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.Customer;
import com.miempresa.ecommerce.services.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CONTROLLER: CLIENTES
 */

@Controller
@RequestMapping("/admin/clientes")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", customerService.obtenerTodos());
        model.addAttribute("titulo", "Gestión de Clientes");
        return "admin/clientes/lista";
    }

    @GetMapping("/ver/{id}")
    public String verDetalle(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        var clienteOpt = customerService.buscarPorId(id);

        if (clienteOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cliente no encontrado");
            return "redirect:/admin/clientes";
        }

        model.addAttribute("cliente", clienteOpt.get());
        model.addAttribute("titulo", "Detalle del Cliente");

        return "admin/clientes/detalle";
    }

    @GetMapping("/buscar")
    @ResponseBody
    public String buscarPorDocumento(@RequestParam String documento) {
        try {
            Customer cliente = customerService.obtenerOCrearDesdeApi(documento);
            return "{\"success\": true, \"cliente\": " +
                    "{\"id\": " + cliente.getId() + ", " +
                    "\"nombre\": \"" + cliente.getNombreCompleto() + "\"}}";
        } catch (Exception e) {
            return "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        customerService.cambiarEstado(id, activo);
        return "redirect:/admin/clientes"; // vuelve a la lista
    }
}
