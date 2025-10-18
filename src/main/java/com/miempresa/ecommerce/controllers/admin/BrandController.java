package com.miempresa.ecommerce.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.Brand;
import com.miempresa.ecommerce.services.BrandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/marcas")
@RequiredArgsConstructor
@Slf4j
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("marcas", brandService.obtenerActivas());
        model.addAttribute("titulo", "Gestión de Marcas");
        return "admin/marcas/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("marca", new Brand());
        model.addAttribute("titulo", "Nueva Marca");
        return "admin/marcas/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Brand marca,
            RedirectAttributes redirectAttributes) {
        try {
            brandService.guardar(marca);
            redirectAttributes.addFlashAttribute("success", "Marca creada correctamente");
        } catch (Exception e) {
            log.error("Error al guardar marca: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/marcas";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        var marcaOpt = brandService.buscarPorId(id);
        if (marcaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Marca no encontrada");
            return "redirect:/admin/marcas";
        }
        model.addAttribute("marca", marcaOpt.get());
        model.addAttribute("titulo", "Editar Marca");
        return "admin/marcas/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Brand marca,
            RedirectAttributes redirectAttributes) {
        try {
            brandService.actualizar(id, marca);
            redirectAttributes.addFlashAttribute("success", "Marca actualizada correctamente");
        } catch (Exception e) {
            log.error("Error al actualizar marca: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/marcas";
    }

    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {
        try {
            brandService.cambiarEstado(id, activo);
            redirectAttributes.addFlashAttribute("success", "Estado actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/marcas";
    }
}