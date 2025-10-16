package com.miempresa.ecommerce.controllers.admin;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.services.ConfigurationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CONTROLLER: CONFIGURACIÓN
 * 
 * Gestiona la configuración del sistema.
 */

@Controller
@RequestMapping("/admin/configuracion")
@RequiredArgsConstructor
@Slf4j
public class ConfigurationController {

    private final ConfigurationService configurationService;

    @GetMapping
    public String mostrarConfiguracion(Model model) {
        Map<String, String> config = configurationService.obtenerTodasComoMapa();
        model.addAttribute("config", config);
        model.addAttribute("titulo", "Configuración del Sistema");
        return "admin/configuracion/index";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Map<String, String> configuraciones,
            RedirectAttributes redirectAttributes) {
        log.info("Guardando configuraciones");

        try {
            configurationService.guardarMultiples(configuraciones);
            redirectAttributes.addFlashAttribute("success",
                    "Configuración guardada correctamente");

        } catch (Exception e) {
            log.error("Error al guardar configuración: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/configuracion";
    }
}