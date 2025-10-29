package com.miempresa.ecommerce.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // <<--- AÑADIR import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.Category;
import com.miempresa.ecommerce.services.CategoryService;

import jakarta.validation.Valid; // <<--- AÑADIR import
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
        // Usar un método que devuelva ordenado si existe, o el findAll normal
        // Asumiremos que el servicio tiene un método obtenerTodasOrdenadas()
        model.addAttribute("categorias", categoryService.obtenerTodas());
        model.addAttribute("titulo", "Gestión de Categorías");
        return "admin/categorias/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Category categoria = new Category();
        categoria.setActivo(true); // Valor por defecto
        model.addAttribute("categoria", categoria);
        model.addAttribute("titulo", "Nueva Categoría");
        model.addAttribute("esNuevo", true); // <<--- ASEGURAR que se pasa
        return "admin/categorias/form";
    }

    // Corregido: Añadir @Valid y BindingResult
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoria") Category categoria, // <<--- AÑADIR @Valid
            BindingResult result, // <<--- AÑADIR BindingResult
            RedirectAttributes redirectAttributes,
            Model model) { // <<--- AÑADIR Model

        // Si hay errores de validación, volver al formulario
        if (result.hasErrors()) {
            log.warn("Errores de validación al guardar categoría: {}", result.getAllErrors());
            model.addAttribute("titulo", "Nueva Categoría");
            model.addAttribute("esNuevo", true);
            // Asegúrate de que cualquier otro dato necesario para el formulario se añada
            // aquí si es necesario
            return "admin/categorias/form"; // <<--- CORREGIDO: Volver al form
        }

        try {
            categoryService.guardar(categoria);
            redirectAttributes.addFlashAttribute("success", "Categoría creada correctamente");
        } catch (Exception e) {
            log.error("Error al guardar categoría: {}", e.getMessage(), e); // Loguear la excepción completa
            // Usar Model para mostrar error en el formulario
            model.addAttribute("error", "Error al guardar: " + e.getMessage());
            model.addAttribute("titulo", "Nueva Categoría");
            model.addAttribute("esNuevo", true);
            return "admin/categorias/form"; // <<--- CORREGIDO: Volver al form en caso de error
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
        model.addAttribute("esNuevo", false); // <<--- ASEGURAR que se pasa
        return "admin/categorias/form";
    }

    // Corregido: Añadir @Valid y BindingResult
    // Corregido: Quitar barra final de "/actualizar/{id}/" si existiera
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
            @Valid @ModelAttribute("categoria") Category categoria, // <<--- AÑADIR @Valid
            BindingResult result, // <<--- AÑADIR BindingResult
            RedirectAttributes redirectAttributes,
            Model model) { // <<--- AÑADIR Model

        // Si hay errores de validación, volver al formulario
        if (result.hasErrors()) {
            log.warn("Errores de validación al actualizar categoría ID {}: {}", id, result.getAllErrors());
            model.addAttribute("titulo", "Editar Categoría");
            model.addAttribute("esNuevo", false);
            // Volver a cargar el objeto original si es necesario para mantener IDs, etc.
            // O simplemente asegurar que el ID esté en el objeto recibido
            categoria.setId(id); // Asegura que el ID esté presente
            return "admin/categorias/form"; // <<--- CORREGIDO: Volver al form
        }

        try {
            // Asegurar que el ID del path variable coincida (seguridad)
            categoria.setId(id);
            categoryService.actualizar(id, categoria);
            redirectAttributes.addFlashAttribute("success", "Categoría actualizada correctamente");
        } catch (Exception e) {
            log.error("Error al actualizar categoría ID {}: {}", id, e.getMessage(), e); // Loguear excepción
            // Usar Model para mostrar error en el formulario
            model.addAttribute("error", "Error al actualizar: " + e.getMessage());
            model.addAttribute("titulo", "Editar Categoría");
            model.addAttribute("esNuevo", false);
            return "admin/categorias/form"; // <<--- CORREGIDO: Volver al form en caso de error
        }
        return "redirect:/admin/categorias";
    }

    // Cambiar estado ahora usa la lógica del servicio con validación
    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {
        try {
            categoryService.cambiarEstado(id, activo);
            String mensaje = activo ? "activada" : "desactivada";
            redirectAttributes.addFlashAttribute("success", "Categoría " + mensaje + " correctamente");
        } catch (Exception e) {
            log.error("Error al cambiar estado de categoría ID {}: {}", id, e.getMessage()); // Loguear error
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    // Método ELIMINAR (borrado lógico con validación de productos asociados)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.eliminar(id); // Llama a cambiarEstado(id, false) con validación
            redirectAttributes.addFlashAttribute("success", "Categoría eliminada correctamente");
        } catch (Exception e) {
            log.error("Error al eliminar categoría ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }
}