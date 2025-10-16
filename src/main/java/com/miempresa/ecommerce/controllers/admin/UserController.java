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

import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.services.ProfileService;
import com.miempresa.ecommerce.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CONTROLLER: USUARIOS
 */

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final ProfileService profileService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", userService.obtenerActivos());
        model.addAttribute("titulo", "Gestión de Usuarios");
        return "admin/usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new User());
        model.addAttribute("perfiles", profileService.obtenerActivos());
        model.addAttribute("titulo", "Nuevo Usuario");
        return "admin/usuarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute User usuario,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        log.info("Creando usuario: {}", usuario.getUsername());

        try {
            userService.crearUsuario(usuario, password);
            redirectAttributes.addFlashAttribute("success",
                    "Usuario creado correctamente");

        } catch (Exception e) {
            log.error("Error al crear usuario: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        var usuarioOpt = userService.buscarPorId(id);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/usuarios";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        model.addAttribute("perfiles", profileService.obtenerActivos());
        model.addAttribute("titulo", "Editar Usuario");

        return "admin/usuarios/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id,
            @ModelAttribute User usuario,
            RedirectAttributes redirectAttributes) {
        try {
            userService.actualizar(id, usuario);
            redirectAttributes.addFlashAttribute("success",
                    "Usuario actualizado correctamente");

        } catch (Exception e) {
            log.error("Error al actualizar usuario: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
            @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {
        try {
            userService.cambiarEstado(id, activo);
            redirectAttributes.addFlashAttribute("success",
                    "Estado actualizado correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/usuarios";
    }
}