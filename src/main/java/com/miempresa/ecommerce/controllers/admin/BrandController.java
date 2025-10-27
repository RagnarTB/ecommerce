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

import com.miempresa.ecommerce.models.Brand;
import com.miempresa.ecommerce.services.BrandService;

import jakarta.validation.Valid; // <<--- AÑADIR import
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
        model.addAttribute("marcas", brandService.obtenerTodas());
        model.addAttribute("titulo", "Gestión de Marcas");
        return "admin/marcas/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Brand marca = new Brand();
        marca.setActivo(true); // Valor por defecto
        model.addAttribute("marca", marca);
        model.addAttribute("titulo", "Nueva Marca");
        model.addAttribute("esNuevo", true); // <<--- ASEGURAR que se pasa
        return "admin/marcas/form";
    }

    // Corregido: Añadir @Valid y BindingResult
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("marca") Brand marca, // <<--- AÑADIR @Valid
            BindingResult result, // <<--- AÑADIR BindingResult
            RedirectAttributes redirectAttributes,
            Model model) { // <<--- AÑADIR Model

        // Si hay errores de validación, volver al formulario
        if (result.hasErrors()) {
            log.warn("Errores de validación al guardar marca: {}", result.getAllErrors());
            model.addAttribute("titulo", "Nueva Marca");
            model.addAttribute("esNuevo", true);
            // Asegúrate de que cualquier otro dato necesario para el formulario se añada
            // aquí si es necesario
            return "admin/marcas/form"; // <<--- CORREGIDO: Volver al form
        }

        try {
            brandService.guardar(marca);
            redirectAttributes.addFlashAttribute("success", "Marca creada correctamente");
        } catch (Exception e) {
            log.error("Error al guardar marca: {}", e.getMessage(), e); // Loguear excepción completa
            // Usar Model para mostrar error en el formulario
            model.addAttribute("error", "Error al guardar: " + e.getMessage());
            model.addAttribute("titulo", "Nueva Marca");
            model.addAttribute("esNuevo", true);
            return "admin/marcas/form"; // <<--- CORREGIDO: Volver al form en caso de error
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
        model.addAttribute("esNuevo", false); // <<--- ASEGURAR que se pasa
        return "admin/marcas/form";
    }

    // Corregido: Añadir @Valid y BindingResult
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
            @Valid @ModelAttribute("marca") Brand marca, // <<--- AÑADIR @Valid
            BindingResult result, // <<--- AÑADIR BindingResult
            RedirectAttributes redirectAttributes,
            Model model) { // <<--- AÑADIR Model

        // Si hay errores de validación, volver al formulario
        if (result.hasErrors()) {
            log.warn("Errores de validación al actualizar marca ID {}: {}", id, result.getAllErrors());
            model.addAttribute("titulo", "Editar Marca");
            model.addAttribute("esNuevo", false);
            // Asegurar que el ID esté presente en el objeto para que el form lo mantenga
            marca.setId(id);
            return "admin/marcas/form"; // <<--- CORREGIDO: Volver al form
        }

        try {
            // Asegurar que el ID del path variable coincida
            marca.setId(id);
            brandService.actualizar(id, marca);
            redirectAttributes.addFlashAttribute("success", "Marca actualizada correctamente");
        } catch (Exception e) {
            log.error("Error al actualizar marca ID {}: {}", id, e.getMessage(), e); // Loguear excepción
            // Usar Model para mostrar error en el formulario
            model.addAttribute("error", "Error al actualizar: " + e.getMessage());
            model.addAttribute("titulo", "Editar Marca");
            model.addAttribute("esNuevo", false);
            // Asegurar ID para mantenerlo en el form
            marca.setId(id);
            return "admin/marcas/form"; // <<--- CORREGIDO: Volver al form en caso de error
        }
        return "redirect:/admin/marcas";
    }

    // Usar lógica del servicio con validación
    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {
        try {
            brandService.cambiarEstado(id, activo);
            String mensaje = activo ? "activada" : "desactivada";
            redirectAttributes.addFlashAttribute("success", "Marca " + mensaje + " correctamente");
        } catch (Exception e) {
            log.error("Error al cambiar estado de marca ID {}: {}", id, e.getMessage()); // Loguear error
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/marcas";
    }

    // Método ELIMINAR (usualmente llama a cambiarEstado para borrado lógico)
    /*
     * @GetMapping("/eliminar/{id}")
     * public String eliminar(@PathVariable Long id, RedirectAttributes
     * redirectAttributes) {
     * try {
     * brandService.eliminar(id); // Llama a cambiarEstado(id, false) internamente
     * redirectAttributes.addFlashAttribute("success",
     * "Marca eliminada (desactivada) correctamente");
     * } catch (Exception e) {
     * log.error("Error al eliminar marca ID {}: {}", id, e.getMessage());
     * redirectAttributes.addFlashAttribute("error", e.getMessage());
     * }
     * return "redirect:/admin/marcas";
     * }
     */
}