package com.miempresa.ecommerce.controllers.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miempresa.ecommerce.models.Provider;
import com.miempresa.ecommerce.services.ProviderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API: PROVEEDORES
 * 
 * Endpoints para gestión de proveedores desde frontend/mobile
 * Base URL: /api/proveedores
 */
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@Slf4j
public class ProviderRestController {

    private final ProviderService providerService;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    /**
     * GET /api/proveedores
     * Obtiene todos los proveedores
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodos(
            @RequestParam(required = false) Boolean activos) {
        try {
            List<Provider> proveedores = activos != null && activos
                    ? providerService.obtenerActivos()
                    : providerService.obtenerTodos();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", proveedores);
            response.put("total", proveedores.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener proveedores: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/proveedores/{id}
     * Obtiene un proveedor por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<Provider> proveedor = providerService.buscarPorId(id);

            if (proveedor.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Proveedor no encontrado"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", proveedor.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener proveedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/proveedores
     * Crea un nuevo proveedor
     * 
     * Body:
     * {
     * "ruc": "20123456789",
     * "razonSocial": "Distribuidora XYZ S.A.C.",
     * "direccion": "Av. Principal 123",
     * "telefono": "01-234-5678",
     * "email": "ventas@xyz.com",
     * "contactoNombre": "Juan Pérez",
     * "contactoTelefono": "987654321"
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody Provider proveedor) {
        try {
            Provider proveedorGuardado = providerService.guardar(proveedor);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Proveedor creado exitosamente");
            response.put("data", proveedorGuardado);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al crear proveedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al crear proveedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al crear proveedor"));
        }
    }

    /**
     * PUT /api/proveedores/{id}
     * Actualiza un proveedor existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Long id,
            @RequestBody Provider proveedor) {
        try {
            Provider proveedorActualizado = providerService.actualizar(id, proveedor);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Proveedor actualizado exitosamente");
            response.put("data", proveedorActualizado);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al actualizar proveedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al actualizar proveedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al actualizar proveedor"));
        }
    }

    /**
     * DELETE /api/proveedores/{id}
     * Elimina (desactiva) un proveedor
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        try {
            providerService.eliminar(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Proveedor eliminado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al eliminar proveedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al eliminar proveedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al eliminar proveedor"));
        }
    }

    // ========================================
    // BÚSQUEDAS
    // ========================================

    /**
     * GET /api/proveedores/buscar
     * Busca proveedores por razón social o RUC
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscar(@RequestParam String busqueda) {
        try {
            List<Provider> proveedores = providerService.buscarPorRazonSocialORuc(busqueda);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", proveedores);
            response.put("total", proveedores.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al buscar proveedores: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/proveedores/ruc/{ruc}
     * Busca un proveedor por RUC
     */
    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<Map<String, Object>> buscarPorRuc(@PathVariable String ruc) {
        try {
            Optional<Provider> proveedor = providerService.buscarPorRuc(ruc);

            if (proveedor.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Proveedor no encontrado"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", proveedor.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al buscar proveedor por RUC: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/proveedores/existe/{ruc}
     * Verifica si existe un proveedor con ese RUC
     */
    @GetMapping("/existe/{ruc}")
    public ResponseEntity<Map<String, Object>> existePorRuc(@PathVariable String ruc) {
        try {
            boolean existe = providerService.existePorRuc(ruc);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("existe", existe);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al verificar existencia de proveedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // CAMBIO DE ESTADO
    // ========================================

    /**
     * PUT /api/proveedores/{id}/estado
     * Cambia el estado de un proveedor
     * 
     * Body: { "activo": true }
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        try {
            Boolean activo = request.get("activo");

            if (activo == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse("El campo 'activo' es obligatorio"));
            }

            Provider proveedor = providerService.cambiarEstado(id, activo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", activo ? "Proveedor activado" : "Proveedor desactivado");
            response.put("data", proveedor);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al cambiar estado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al cambiar estado"));
        }
    }

    // ========================================
    // ESTADÍSTICAS
    // ========================================

    /**
     * GET /api/proveedores/estadisticas
     * Obtiene estadísticas de proveedores
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            long totalActivos = providerService.contarActivos();
            long totalGeneral = providerService.obtenerTodos().size();

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalActivos", totalActivos);
            estadisticas.put("totalGeneral", totalGeneral);
            estadisticas.put("totalInactivos", totalGeneral - totalActivos);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", estadisticas);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener estadísticas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // UTILIDADES
    // ========================================

    private Map<String, Object> crearErrorResponse(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", mensaje);
        return response;
    }
}