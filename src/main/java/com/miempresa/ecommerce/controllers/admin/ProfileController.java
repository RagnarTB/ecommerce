package com.miempresa.ecommerce.controllers.admin;

import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.Profile;
import com.miempresa.ecommerce.services.PermissionService;
import com.miempresa.ecommerce.services.ProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/perfiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final ProfileService profileService;
    private final PermissionService permissionService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("perfiles", profileService.obtenerTodos());
        model.addAttribute("titulo", "Gestión de Perfiles");
        return "admin/perfiles/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("perfil", new Profile());
        model.addAttribute("permisos", permissionService.obtenerActivos());
        model.addAttribute("titulo", "Nuevo Perfil");
        return "admin/perfiles/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Profile perfil,
            @RequestParam(required = false) Set<Long> permisosIds,
            RedirectAttributes redirectAttributes) {
        try {
            Profile perfilGuardado = profileService.guardar(perfil);

            if (permisosIds != null && !permisosIds.isEmpty()) {
                profileService.asignarPermisos(perfilGuardado.getId(), permisosIds);
            }

            redirectAttributes.addFlashAttribute("success", "Perfil creado correctamente");
        } catch (Exception e) {
            log.error("Error al guardar perfil: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/perfiles";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        var perfilOpt = profileService.buscarPorId(id);
        if (perfilOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Perfil no encontrado");
            return "redirect:/admin/perfiles";
        }

        model.addAttribute("perfil", perfilOpt.get());
        model.addAttribute("permisos", permissionService.obtenerActivos());
        model.addAttribute("titulo", "Editar Perfil");
        return "admin/perfiles/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Profile perfil,
            @RequestParam(required = false) Set<Long> permisosIds,
            RedirectAttributes redirectAttributes) {
        try {
            profileService.actualizar(id, perfil);

            if (permisosIds != null) {
                profileService.asignarPermisos(id, permisosIds);
            }

            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        } catch (Exception e) {
            log.error("Error al actualizar perfil: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/perfiles";
    }

    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {
        try {
            profileService.cambiarEstado(id, activo);
            redirectAttributes.addFlashAttribute("success", "Estado actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/perfiles";
    }
}