package com.miempresa.ecommerce.controllers.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.Customer;
import com.miempresa.ecommerce.models.enums.TipoDocumento;
import com.miempresa.ecommerce.services.CustomerService;

import lombok.Data;
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

    // ✅ NUEVO: Endpoint para crear cliente desde modal
    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearCliente(@RequestBody CrearClienteRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validaciones
            if (request.getNumeroDocumento() == null || request.getNumeroDocumento().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "El número de documento es obligatorio");
                return ResponseEntity.badRequest().body(response);
            }

            TipoDocumento tipo = TipoDocumento.valueOf(request.getTipoDocumento());

            // Validar longitud según tipo
            if (tipo == TipoDocumento.DNI && request.getNumeroDocumento().length() != 8) {
                response.put("success", false);
                response.put("error", "El DNI debe tener 8 dígitos");
                return ResponseEntity.badRequest().body(response);
            }

            if (tipo == TipoDocumento.RUC && request.getNumeroDocumento().length() != 11) {
                response.put("success", false);
                response.put("error", "El RUC debe tener 11 dígitos");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar campos requeridos según tipo
            if (tipo == TipoDocumento.DNI) {
                if (request.getNombres() == null || request.getNombres().trim().isEmpty() ||
                        request.getApellidos() == null || request.getApellidos().trim().isEmpty()) {
                    response.put("success", false);
                    response.put("error", "Nombres y apellidos son obligatorios para DNI");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                if (request.getRazonSocial() == null || request.getRazonSocial().trim().isEmpty()) {
                    response.put("success", false);
                    response.put("error", "La razón social es obligatoria para RUC");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Crear el cliente
            Customer cliente = Customer.builder()
                    .tipoDocumento(tipo)
                    .numeroDocumento(request.getNumeroDocumento())
                    .nombres(request.getNombres())
                    .apellidos(request.getApellidos())
                    .razonSocial(request.getRazonSocial())
                    .email(request.getEmail())
                    .telefono(request.getTelefono())
                    .direccion(request.getDireccion())
                    .activo(true)
                    .build();

            Customer clienteGuardado = customerService.guardar(cliente);

            response.put("success", true);
            response.put("data", clienteGuardado);
            response.put("mensaje", "Cliente creado correctamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al crear cliente: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // DTO para recibir datos del formulario
    @Data
    public static class CrearClienteRequest {
        private String tipoDocumento;
        private String numeroDocumento;
        private String nombres;
        private String apellidos;
        private String razonSocial;
        private String email;
        private String telefono;
        private String direccion;
    }
}
