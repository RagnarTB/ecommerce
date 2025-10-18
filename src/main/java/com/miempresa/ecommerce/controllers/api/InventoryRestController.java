package com.miempresa.ecommerce.controllers.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miempresa.ecommerce.models.InventoryMovement;
import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.models.enums.MotivoMovimiento;
import com.miempresa.ecommerce.models.enums.TipoMovimiento;
import com.miempresa.ecommerce.services.InventoryMovementService;
import com.miempresa.ecommerce.services.ProductService;
import com.miempresa.ecommerce.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API: INVENTARIO
 * 
 * Endpoints para gestión de movimientos de inventario desde frontend/mobile
 * Base URL: /api/inventario
 */
@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Slf4j
public class InventoryRestController {

    private final InventoryMovementService inventoryMovementService;
    private final ProductService productService;
    private final UserService userService;

    // ========================================
    // CONSULTAS DE MOVIMIENTOS
    // ========================================

    /**
     * GET /api/inventario/movimientos
     * Obtiene todos los movimientos de inventario
     */
    @GetMapping("/movimientos")
    public ResponseEntity<Map<String, Object>> obtenerTodos(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String motivo) {
        try {
            List<InventoryMovement> movimientos;

            if (productoId != null) {
                movimientos = inventoryMovementService.obtenerPorProducto(productoId);
            } else if (tipo != null) {
                movimientos = inventoryMovementService.obtenerPorTipo(
                        TipoMovimiento.valueOf(tipo.toUpperCase()));
            } else if (motivo != null) {
                movimientos = inventoryMovementService.obtenerPorMotivo(
                        MotivoMovimiento.valueOf(motivo.toUpperCase()));
            } else {
                movimientos = inventoryMovementService.obtenerTodos();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", movimientos);
            response.put("total", movimientos.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse("Tipo o motivo inválido: " + e.getMessage()));

        } catch (Exception e) {
            log.error("Error al obtener movimientos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/inventario/movimientos/{id}
     * Obtiene un movimiento por ID
     */
    @GetMapping("/movimientos/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<InventoryMovement> movimiento = inventoryMovementService.buscarPorId(id);

            if (movimiento.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Movimiento no encontrado"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", movimiento.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener movimiento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/inventario/movimientos/producto/{productoId}
     * Obtiene movimientos de un producto específico
     */
    @GetMapping("/movimientos/producto/{productoId}")
    public ResponseEntity<Map<String, Object>> obtenerPorProducto(@PathVariable Long productoId) {
        try {
            List<InventoryMovement> movimientos = inventoryMovementService.obtenerPorProducto(productoId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", movimientos);
            response.put("total", movimientos.size());
            response.put("productoId", productoId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener movimientos del producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/inventario/movimientos/rango
     * Obtiene movimientos en un rango de fechas
     * 
     * Params: fechaInicio, fechaFin (formato: yyyy-MM-dd'T'HH:mm:ss)
     */
    @GetMapping("/movimientos/rango")
    public ResponseEntity<Map<String, Object>> obtenerPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<InventoryMovement> movimientos = inventoryMovementService.obtenerPorFechas(
                    fechaInicio, fechaFin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", movimientos);
            response.put("total", movimientos.size());
            response.put("fechaInicio", fechaInicio);
            response.put("fechaFin", fechaFin);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener movimientos por rango: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/inventario/movimientos/ultimos
     * Obtiene los últimos movimientos (top 20)
     */
    @GetMapping("/movimientos/ultimos")
    public ResponseEntity<Map<String, Object>> obtenerUltimos() {
        try {
            List<InventoryMovement> movimientos = inventoryMovementService.obtenerUltimos();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", movimientos);
            response.put("total", movimientos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener últimos movimientos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/inventario/movimientos/filtrar
     * Búsqueda avanzada con múltiples filtros
     */
    @GetMapping("/movimientos/filtrar")
    public ResponseEntity<Map<String, Object>> buscarConFiltros(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            TipoMovimiento tipoMovimiento = tipo != null ? TipoMovimiento.valueOf(tipo.toUpperCase()) : null;
            MotivoMovimiento motivoMovimiento = motivo != null ? MotivoMovimiento.valueOf(motivo.toUpperCase())
                    : null;

            List<InventoryMovement> movimientos = inventoryMovementService.buscarConFiltros(
                    productoId, tipoMovimiento, motivoMovimiento, fechaInicio, fechaFin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", movimientos);
            response.put("total", movimientos.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse("Filtro inválido: " + e.getMessage()));

        } catch (Exception e) {
            log.error("Error al filtrar movimientos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // REGISTRAR MOVIMIENTOS
    // ========================================

    /**
     * POST /api/inventario/entrada
     * Registra una entrada de inventario
     * 
     * Body:
     * {
     * "productoId": 5,
     * "cantidad": 100,
     * "motivo": "COMPRA",
     * "observaciones": "Compra a proveedor XYZ"
     * }
     */
    @PostMapping("/entrada")
    public ResponseEntity<Map<String, Object>> registrarEntrada(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = obtenerUsuarioAutenticado(userDetails);

            // Parsear datos
            Long productoId = Long.valueOf(request.get("productoId").toString());
            Integer cantidad = Integer.valueOf(request.get("cantidad").toString());
            MotivoMovimiento motivo = MotivoMovimiento.valueOf(request.get("motivo").toString());
            String observaciones = request.containsKey("observaciones")
                    ? request.get("observaciones").toString()
                    : null;

            // Validar producto existe
            Optional<Product> productoOpt = productService.buscarPorId(productoId);
            if (productoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Producto no encontrado"));
            }

            // Validar cantidad positiva
            if (cantidad <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse("La cantidad debe ser mayor a cero"));
            }

            // Registrar entrada
            InventoryMovement movimiento = inventoryMovementService.registrarEntrada(
                    productoId, cantidad, motivo, usuario, observaciones);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Entrada registrada exitosamente");
            response.put("data", movimiento);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse("Motivo inválido: " + e.getMessage()));

        } catch (RuntimeException e) {
            log.error("Error al registrar entrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al registrar entrada: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al registrar entrada"));
        }
    }

    /**
     * POST /api/inventario/salida
     * Registra una salida de inventario
     * 
     * Body:
     * {
     * "productoId": 5,
     * "cantidad": 50,
     * "motivo": "AJUSTE_NEGATIVO",
     * "observaciones": "Productos dañados"
     * }
     */
    @PostMapping("/salida")
    public ResponseEntity<Map<String, Object>> registrarSalida(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = obtenerUsuarioAutenticado(userDetails);

            // Parsear datos
            Long productoId = Long.valueOf(request.get("productoId").toString());
            Integer cantidad = Integer.valueOf(request.get("cantidad").toString());
            MotivoMovimiento motivo = MotivoMovimiento.valueOf(request.get("motivo").toString());
            String observaciones = request.containsKey("observaciones")
                    ? request.get("observaciones").toString()
                    : null;

            // Validar producto existe
            Optional<Product> productoOpt = productService.buscarPorId(productoId);
            if (productoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Producto no encontrado"));
            }

            // Validar cantidad positiva
            if (cantidad <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse("La cantidad debe ser mayor a cero"));
            }

            // Validar stock suficiente
            Product producto = productoOpt.get();
            if (producto.getStockActual() < cantidad) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse(
                                String.format("Stock insuficiente. Disponible: %d, Solicitado: %d",
                                        producto.getStockActual(), cantidad)));
            }

            // Registrar salida
            InventoryMovement movimiento = inventoryMovementService.registrarSalida(
                    productoId, cantidad, motivo, usuario, observaciones);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Salida registrada exitosamente");
            response.put("data", movimiento);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse("Motivo inválido: " + e.getMessage()));

        } catch (RuntimeException e) {
            log.error("Error al registrar salida: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al registrar salida: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al registrar salida"));
        }
    }

    /**
     * POST /api/inventario/movimiento
     * Registra un movimiento genérico (entrada o salida)
     * 
     * Body:
     * {
     * "productoId": 5,
     * "cantidad": 30,
     * "tipo": "ENTRADA",
     * "motivo": "COMPRA",
     * "observaciones": "Nota opcional"
     * }
     */
    @PostMapping("/movimiento")
    public ResponseEntity<Map<String, Object>> registrarMovimiento(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = obtenerUsuarioAutenticado(userDetails);

            // Parsear datos
            Long productoId = Long.valueOf(request.get("productoId").toString());
            Integer cantidad = Integer.valueOf(request.get("cantidad").toString());
            TipoMovimiento tipo = TipoMovimiento.valueOf(request.get("tipo").toString());
            MotivoMovimiento motivo = MotivoMovimiento.valueOf(request.get("motivo").toString());
            String observaciones = request.containsKey("observaciones")
                    ? request.get("observaciones").toString()
                    : null;

            // Validaciones
            if (cantidad <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse("La cantidad debe ser mayor a cero"));
            }

            // Registrar movimiento
            InventoryMovement movimiento = inventoryMovementService.registrarMovimiento(
                    productoId, cantidad, tipo, motivo, usuario, observaciones);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Movimiento registrado exitosamente");
            response.put("data", movimiento);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse("Tipo o motivo inválido: " + e.getMessage()));

        } catch (RuntimeException e) {
            log.error("Error al registrar movimiento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al registrar movimiento: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al registrar movimiento"));
        }
    }

    // ========================================
    // ALERTAS DE STOCK
    // ========================================

    /**
     * GET /api/inventario/alertas/stock-bajo
     * Obtiene productos con stock bajo
     */
    @GetMapping("/alertas/stock-bajo")
    public ResponseEntity<Map<String, Object>> obtenerStockBajo() {
        try {
            List<Product> productos = productService.obtenerProductosStockBajo();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", productos);
            response.put("total", productos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener productos con stock bajo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/inventario/alertas/sin-stock
     * Obtiene productos sin stock
     */
    @GetMapping("/alertas/sin-stock")
    public ResponseEntity<Map<String, Object>> obtenerSinStock() {
        try {
            List<Product> productos = productService.obtenerProductosSinStock();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", productos);
            response.put("total", productos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener productos sin stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // ESTADÍSTICAS
    // ========================================

    /**
     * GET /api/inventario/estadisticas
     * Obtiene estadísticas de inventario
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            long totalMovimientos = inventoryMovementService.obtenerTodos().size();
            long totalEntradas = inventoryMovementService.contarPorTipo(TipoMovimiento.ENTRADA);
            long totalSalidas = inventoryMovementService.contarPorTipo(TipoMovimiento.SALIDA);
            int productosStockBajo = productService.obtenerProductosStockBajo().size();
            int productosSinStock = productService.obtenerProductosSinStock().size();

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalMovimientos", totalMovimientos);
            estadisticas.put("totalEntradas", totalEntradas);
            estadisticas.put("totalSalidas", totalSalidas);
            estadisticas.put("productosStockBajo", productosStockBajo);
            estadisticas.put("productosSinStock", productosSinStock);

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

    /**
     * GET /api/inventario/estadisticas/tipo
     * Obtiene estadísticas por tipo de movimiento
     */
    @GetMapping("/estadisticas/tipo")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorTipo() {
        try {
            long entradas = inventoryMovementService.contarPorTipo(TipoMovimiento.ENTRADA);
            long salidas = inventoryMovementService.contarPorTipo(TipoMovimiento.SALIDA);

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("entradas", entradas);
            estadisticas.put("salidas", salidas);
            estadisticas.put("total", entradas + salidas);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", estadisticas);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener estadísticas por tipo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // UTILIDADES PRIVADAS
    // ========================================

    private User obtenerUsuarioAutenticado(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        return userService.buscarPorUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Map<String, Object> crearErrorResponse(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", mensaje);
        return response;
    }
}