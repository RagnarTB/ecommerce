package com.miempresa.ecommerce.controllers.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.Provider;
import com.miempresa.ecommerce.services.ProviderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/proveedores")
@RequiredArgsConstructor
@Slf4j
public class ProviderController {
    private final ProviderService providerService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("proveedores", providerService.obtenerTodos());
        model.addAttribute("titulo", "Gestión de Proveedores");
        return "admin/proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("proveedor", new Provider());
        model.addAttribute("titulo", "Nuevo Proveedor");
        return "admin/proveedores/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Provider proveedor,
            RedirectAttributes redirectAttributes) {
        try {
            providerService.guardar(proveedor);
            redirectAttributes.addFlashAttribute("success", "Proveedor creado correctamente");
        } catch (Exception e) {
            log.error("Error al guardar proveedor: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        var proveedorOpt = providerService.buscarPorId(id);
        if (proveedorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Proveedor no encontrado");
            return "redirect:/admin/proveedores";
        }
        model.addAttribute("proveedor", proveedorOpt.get());
        model.addAttribute("titulo", "Editar Proveedor");
        return "admin/proveedores/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Provider proveedor,
            RedirectAttributes redirectAttributes) {
        try {
            providerService.actualizar(id, proveedor);
            redirectAttributes.addFlashAttribute("success", "Proveedor actualizado correctamente");
        } catch (Exception e) {
            log.error("Error al actualizar proveedor: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/ver/{id}")
    public String verDetalle(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        var proveedorOpt = providerService.buscarPorId(id);
        if (proveedorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Proveedor no encontrado");
            return "redirect:/admin/proveedores";
        }
        model.addAttribute("proveedor", proveedorOpt.get());
        model.addAttribute("titulo", "Detalle del Proveedor");
        return "admin/proveedores/detalle";
    }

    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {
        try {
            providerService.cambiarEstado(id, activo);
            redirectAttributes.addFlashAttribute("success", "Estado actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/buscar")
    @ResponseBody
    public String buscarPorRucONombre(@RequestParam String busqueda) {
        try {
            List<Provider> proveedores = providerService.buscarPorRazonSocialORuc(busqueda);
            // Retornar JSON simplificado
            return "{\"success\": true, \"proveedores\": " + proveedores.size() + "}";
        } catch (Exception e) {
            return "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
        }
    }
}