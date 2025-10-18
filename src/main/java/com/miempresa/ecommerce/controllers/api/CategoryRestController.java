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

import com.miempresa.ecommerce.models.Category;
import com.miempresa.ecommerce.services.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API: CATEGORÍAS
 * 
 * Endpoints para gestión de categorías desde frontend/mobile
 * Base URL: /api/categorias
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Slf4j
public class CategoryRestController {

    private final CategoryService categoryService;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    /**
     * GET /api/categorias
     * Obtiene todas las categorías
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodas(
            @RequestParam(required = false) Boolean activas) {
        try {
            List<Category> categorias = activas != null && activas
                    ? categoryService.obtenerActivas()
                    : categoryService.obtenerTodas();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categorias);
            response.put("total", categorias.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener categorías: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/categorias/{id}
     * Obtiene una categoría por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<Category> categoria = categoryService.buscarPorId(id);

            if (categoria.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Categoría no encontrada"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categoria.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/categorias
     * Crea una nueva categoría
     * 
     * Body:
     * {
     * "nombre": "Electrónica",
     * "descripcion": "Productos electrónicos",
     * "orden": 1
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody Category categoria) {
        try {
            Category categoriaGuardada = categoryService.guardar(categoria);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categoría creada exitosamente");
            response.put("data", categoriaGuardada);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al crear categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al crear categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al crear categoría"));
        }
    }

    /**
     * PUT /api/categorias/{id}
     * Actualiza una categoría existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Long id,
            @RequestBody Category categoria) {
        try {
            Category categoriaActualizada = categoryService.actualizar(id, categoria);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categoría actualizada exitosamente");
            response.put("data", categoriaActualizada);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al actualizar categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al actualizar categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al actualizar categoría"));
        }
    }

    /**
     * DELETE /api/categorias/{id}
     * Elimina (desactiva) una categoría
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        try {
            categoryService.eliminar(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categoría eliminada exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al eliminar categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al eliminar categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al eliminar categoría"));
        }
    }

    // ========================================
    // CAMBIO DE ESTADO
    // ========================================

    /**
     * PUT /api/categorias/{id}/estado
     * Cambia el estado de una categoría
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

            Category categoria = categoryService.cambiarEstado(id, activo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", activo ? "Categoría activada" : "Categoría desactivada");
            response.put("data", categoria);

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
     * GET /api/categorias/estadisticas
     * Obtiene estadísticas de categorías
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            long totalActivas = categoryService.contarActivas();
            long totalGeneral = categoryService.obtenerTodas().size();

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