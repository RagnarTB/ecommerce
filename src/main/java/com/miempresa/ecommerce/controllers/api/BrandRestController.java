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

import com.miempresa.ecommerce.models.Brand;
import com.miempresa.ecommerce.services.BrandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API: MARCAS
 * 
 * Endpoints para gestión de marcas desde frontend/mobile
 * Base URL: /api/marcas
 */
@RestController
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
@Slf4j
public class BrandRestController {

    private final BrandService brandService;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    /**
     * GET /api/marcas
     * Obtiene todas las marcas
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodas(
            @RequestParam(required = false) Boolean activas) {
        try {
            List<Brand> marcas = activas != null && activas
                    ? brandService.obtenerActivas()
                    : brandService.obtenerTodas();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", marcas);
            response.put("total", marcas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener marcas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/marcas/{id}
     * Obtiene una marca por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<Brand> marca = brandService.buscarPorId(id);

            if (marca.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Marca no encontrada"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", marca.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener marca: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/marcas
     * Crea una nueva marca
     * 
     * Body:
     * {
     * "nombre": "Samsung",
     * "descripcion": "Marca de tecnología"
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody Brand marca) {
        try {
            Brand marcaGuardada = brandService.guardar(marca);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Marca creada exitosamente");
            response.put("data", marcaGuardada);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al crear marca: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al crear marca: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al crear marca"));
        }
    }

    /**
     * PUT /api/marcas/{id}
     * Actualiza una marca existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Long id,
            @RequestBody Brand marca) {
        try {
            Brand marcaActualizada = brandService.actualizar(id, marca);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Marca actualizada exitosamente");
            response.put("data", marcaActualizada);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al actualizar marca: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al actualizar marca: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al actualizar marca"));
        }
    }

    /**
     * DELETE /api/marcas/{id}
     * Elimina (desactiva) una marca
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        try {
            brandService.eliminar(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Marca eliminada exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al eliminar marca: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al eliminar marca: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al eliminar marca"));
        }
    }

    // ========================================
    // CAMBIO DE ESTADO
    // ========================================

    /**
     * PUT /api/marcas/{id}/estado
     * Cambia el estado de una marca
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

            Brand marca = brandService.cambiarEstado(id, activo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", activo ? "Marca activada" : "Marca desactivada");
            response.put("data", marca);

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
     * GET /api/marcas/estadisticas
     * Obtiene estadísticas de marcas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            long totalActivas = brandService.contarActivas();
            long totalGeneral = brandService.obtenerTodas().size();

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalActivas", totalActivas);
            estadisticas.put("totalGeneral", totalGeneral);
            estadisticas.put("totalInactivas", totalGeneral - totalActivas);

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