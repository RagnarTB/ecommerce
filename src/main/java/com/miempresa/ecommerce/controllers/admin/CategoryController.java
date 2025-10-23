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

import com.miempresa.ecommerce.models.Category;
import com.miempresa.ecommerce.services.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/categorias")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoryService.obtenerTodas());
        model.addAttribute("titulo", "Gestión de Categorías");
        return "admin/categorias/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("categoria", new Category());
        model.addAttribute("titulo", "Nueva Categoría");
        return "admin/categorias/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Category categoria,
            RedirectAttributes redirectAttributes) {
        try {
            categoryService.guardar(categoria);
            redirectAttributes.addFlashAttribute("success", "Categoría creada correctamente");
        } catch (Exception e) {
            log.error("Error al guardar categoría: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        var categoriaOpt = categoryService.buscarPorId(id);
        if (categoriaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Categoría no encontrada");
            return "redirect:/admin/categorias";
        }
        model.addAttribute("categoria", categoriaOpt.get());
        model.addAttribute("titulo", "Editar Categoría");
        return "admin/categorias/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Category categoria,
            RedirectAttributes redirectAttributes) {
        try {
            categoryService.actualizar(id, categoria);
            redirectAttributes.addFlashAttribute("success", "Categoría actualizada correctamente");
        } catch (Exception e) {
            log.error("Error al actualizar categoría: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {
        try {
            categoryService.cambiarEstado(id, activo);
            redirectAttributes.addFlashAttribute("success", "Estado actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }
}