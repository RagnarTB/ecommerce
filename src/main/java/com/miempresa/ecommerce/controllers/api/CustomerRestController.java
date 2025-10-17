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

import com.miempresa.ecommerce.models.Customer;
import com.miempresa.ecommerce.services.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API: CLIENTES
 * 
 * Endpoints para gestión de clientes desde frontend/mobile
 * Base URL: /api/clientes
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
public class CustomerRestController {

    private final CustomerService customerService;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    /**
     * GET /api/clientes
     * Obtiene todos los clientes
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodos(
            @RequestParam(required = false) Boolean activos) {
        try {
            List<Customer> clientes = activos != null && activos
                    ? customerService.obtenerActivos()
                    : customerService.obtenerTodos();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", clientes);
            response.put("total", clientes.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener clientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/clientes/{id}
     * Obtiene un cliente por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<Customer> cliente = customerService.buscarPorId(id);

            if (cliente.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Cliente no encontrado"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cliente.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/clientes
     * Crea un nuevo cliente
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody Customer cliente) {
        try {
            Customer clienteGuardado = customerService.guardar(cliente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente creado exitosamente");
            response.put("data", clienteGuardado);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al crear cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al crear cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al crear cliente"));
        }
    }

    /**
     * PUT /api/clientes/{id}
     * Actualiza un cliente existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Long id,
            @RequestBody Customer cliente) {
        try {
            Customer clienteActualizado = customerService.actualizar(id, cliente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente actualizado exitosamente");
            response.put("data", clienteActualizado);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al actualizar cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al actualizar cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al actualizar cliente"));
        }
    }

    /**
     * DELETE /api/clientes/{id}
     * Elimina (desactiva) un cliente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        try {
            customerService.eliminar(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente eliminado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al eliminar cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al eliminar cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al eliminar cliente"));
        }
    }

    // ========================================
    // BÚSQUEDAS
    // ========================================

    /**
     * GET /api/clientes/buscar
     * Busca clientes por nombre o razón social
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarPorNombre(
            @RequestParam String nombre) {
        try {
            List<Customer> clientes = customerService.buscarPorNombre(nombre);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", clientes);
            response.put("total", clientes.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al buscar clientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/clientes/buscar-avanzado
     * Busca clientes por documento o nombre
     */
    @GetMapping("/buscar-avanzado")
    public ResponseEntity<Map<String, Object>> buscarPorDocumentoONombre(
            @RequestParam String busqueda) {
        try {
            List<Customer> clientes = customerService.buscarPorDocumentoONombre(busqueda);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", clientes);
            response.put("total", clientes.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al buscar clientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/clientes/documento/{numeroDocumento}
     * Busca un cliente por número de documento
     */
    @GetMapping("/documento/{numeroDocumento}")
    public ResponseEntity<Map<String, Object>> buscarPorDocumento(
            @PathVariable String numeroDocumento) {
        try {
            Optional<Customer> cliente = customerService.buscarPorDocumento(numeroDocumento);

            if (cliente.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Cliente no encontrado"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cliente.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al buscar cliente por documento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/clientes/existe/{numeroDocumento}
     * Verifica si existe un cliente con ese documento
     */
    @GetMapping("/existe/{numeroDocumento}")
    public ResponseEntity<Map<String, Object>> existePorDocumento(
            @PathVariable String numeroDocumento) {
        try {
            boolean existe = customerService.existePorDocumento(numeroDocumento);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("existe", existe);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al verificar existencia de cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // INTEGRACIÓN CON API DECOLECTA
    // ========================================

    /**
     * POST /api/clientes/obtener-o-crear
     * Obtiene un cliente desde BD o lo crea consultando API Decolecta
     * 
     * Body: { "numeroDocumento": "12345678" }
     */
    @PostMapping("/obtener-o-crear")
    public ResponseEntity<Map<String, Object>> obtenerOCrearDesdeApi(
            @RequestBody Map<String, String> request) {
        try {
            String numeroDocumento = request.get("numeroDocumento");

            if (numeroDocumento == null || numeroDocumento.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse("Número de documento es obligatorio"));
            }

            Customer cliente = customerService.obtenerOCrearDesdeApi(numeroDocumento);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente obtenido exitosamente");
            response.put("data", cliente);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al obtener/crear cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al procesar solicitud"));
        }
    }

    /**
     * GET /api/clientes/consultar-api/{numeroDocumento}
     * Consulta directamente la API Decolecta (DNI/RUC)
     * Siempre crea/actualiza el registro en BD
     */
    @GetMapping("/consultar-api/{numeroDocumento}")
    public ResponseEntity<Map<String, Object>> consultarApi(
            @PathVariable String numeroDocumento) {
        try {
            Customer cliente = customerService.obtenerOCrearDesdeApi(numeroDocumento);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Consulta exitosa");
            response.put("data", cliente);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al consultar API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al consultar API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al consultar API"));
        }
    }

    // ========================================
    // CAMBIO DE ESTADO
    // ========================================

    /**
     * PUT /api/clientes/{id}/estado
     * Cambia el estado de un cliente
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

            Customer cliente = customerService.cambiarEstado(id, activo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", activo ? "Cliente activado" : "Cliente desactivado");
            response.put("data", cliente);

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
     * GET /api/clientes/estadisticas
     * Obtiene estadísticas de clientes
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            long totalActivos = customerService.contarActivos();
            long totalGeneral = customerService.obtenerTodos().size();

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