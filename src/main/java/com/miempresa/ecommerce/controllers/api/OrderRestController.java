package com.miempresa.ecommerce.controllers.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miempresa.ecommerce.models.Order;
import com.miempresa.ecommerce.models.OrderDetail;
import com.miempresa.ecommerce.models.Sale;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.models.enums.EstadoPedido;
import com.miempresa.ecommerce.models.enums.TipoPago;
import com.miempresa.ecommerce.services.OrderService;
import com.miempresa.ecommerce.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API: PEDIDOS
 * 
 * Endpoints para gestión de pedidos desde frontend/mobile
 * Base URL: /api/pedidos
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Slf4j
public class OrderRestController {

    private final OrderService orderService;
    private final UserService userService;

    // ========================================
    // CREAR PEDIDO
    // ========================================

    /**
     * POST /api/pedidos
     * Crea un nuevo pedido
     * 
     * Body:
     * {
     * "pedido": { ... },
     * "detalles": [ ... ]
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(
            @RequestBody Map<String, Object> request) {
        try {
            // Parsear datos del request
            Order pedido = parsearPedido(request.get("pedido"));
            List<OrderDetail> detalles = parsearDetalles(request.get("detalles"));

            // Validar que haya detalles
            if (detalles == null || detalles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse("Debe incluir al menos un producto"));
            }

            // Crear pedido
            Order pedidoGuardado = orderService.crearPedido(pedido, detalles);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido creado exitosamente");
            response.put("data", pedidoGuardado);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al crear pedido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al crear pedido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al crear pedido"));
        }
    }

    // ========================================
    // CONSULTAS
    // ========================================

    /**
     * GET /api/pedidos
     * Obtiene todos los pedidos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodos(
            @RequestParam(required = false) String estado) {
        try {
            List<Order> pedidos;

            if (estado != null && !estado.isBlank()) {
                EstadoPedido estadoPedido = EstadoPedido.valueOf(estado.toUpperCase());
                pedidos = orderService.obtenerPorEstado(estadoPedido);
            } else {
                pedidos = orderService.obtenerTodos();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pedidos);
            response.put("total", pedidos.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse("Estado inválido: " + estado));

        } catch (Exception e) {
            log.error("Error al obtener pedidos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/pedidos/{id}
     * Obtiene un pedido por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<Order> pedido = orderService.buscarPorId(id);

            if (pedido.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Pedido no encontrado"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pedido.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener pedido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/pedidos/numero/{numeroPedido}
     * Obtiene un pedido por su número
     */
    @GetMapping("/numero/{numeroPedido}")
    public ResponseEntity<Map<String, Object>> obtenerPorNumero(@PathVariable String numeroPedido) {
        try {
            Optional<Order> pedido = orderService.buscarPorNumero(numeroPedido);

            if (pedido.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Pedido no encontrado"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pedido.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener pedido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/pedidos/cliente/{clienteId}
     * Obtiene pedidos de un cliente
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Map<String, Object>> obtenerPorCliente(@PathVariable Long clienteId) {
        try {
            List<Order> pedidos = orderService.buscarPorCliente(clienteId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pedidos);
            response.put("total", pedidos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener pedidos del cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/pedidos/pendientes
     * Obtiene pedidos pendientes
     */
    @GetMapping("/pendientes")
    public ResponseEntity<Map<String, Object>> obtenerPendientes() {
        try {
            List<Order> pedidos = orderService.obtenerPendientes();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pedidos);
            response.put("total", pedidos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener pedidos pendientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // CAMBIOS DE ESTADO
    // ========================================

    /**
     * PUT /api/pedidos/{id}/confirmar
     * Confirma un pedido
     */
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<Map<String, Object>> confirmar(@PathVariable Long id) {
        try {
            Order pedido = orderService.confirmarPedido(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido confirmado exitosamente");
            response.put("data", pedido);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al confirmar pedido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al confirmar pedido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al confirmar pedido"));
        }
    }

    /**
     * PUT /api/pedidos/{id}/cancelar
     * Cancela un pedido
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Map<String, Object>> cancelar(@PathVariable Long id) {
        try {
            orderService.cancelarPedido(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido cancelado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al cancelar pedido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al cancelar pedido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al cancelar pedido"));
        }
    }

    // ========================================
    // CONVERTIR A VENTA
    // ========================================

    /**
     * POST /api/pedidos/{id}/convertir-venta
     * Convierte un pedido en venta
     * 
     * Body:
     * {
     * "tipoPago": "CONTADO" | "CREDITO",
     * "numCuotas": 12 (opcional, solo si es a crédito)
     * }
     */
    @PostMapping("/{id}/convertir-venta")
    public ResponseEntity<Map<String, Object>> convertirAVenta(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = obtenerUsuarioAutenticado(userDetails);

            // Parsear datos
            TipoPago tipoPago = TipoPago.valueOf(request.get("tipoPago").toString());
            Integer numCuotas = request.containsKey("numCuotas")
                    ? (Integer) request.get("numCuotas")
                    : null;

            // Validar número de cuotas si es a crédito
            if (tipoPago == TipoPago.CREDITO) {
                if (numCuotas == null || numCuotas < 1 || numCuotas > 24) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(crearErrorResponse("Para ventas a crédito debe especificar entre 1 y 24 cuotas"));
                }
            }

            // Convertir a venta
            Sale venta = orderService.convertirAVenta(id, usuario, tipoPago, numCuotas);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pedido convertido a venta exitosamente");
            response.put("data", venta);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al convertir pedido a venta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al convertir pedido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al convertir pedido"));
        }
    }

    // ========================================
    // ESTADÍSTICAS
    // ========================================

    /**
     * GET /api/pedidos/estadisticas
     * Obtiene estadísticas de pedidos
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            long totalPendientes = orderService.contarPorEstado(EstadoPedido.PENDIENTE);
            long totalConfirmados = orderService.contarPorEstado(EstadoPedido.CONFIRMADO);

            long totalCancelados = orderService.contarPorEstado(EstadoPedido.CANCELADO);

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("pendientes", totalPendientes);
            estadisticas.put("confirmados", totalConfirmados);

            estadisticas.put("cancelados", totalCancelados);
            estadisticas.put("total", totalPendientes + totalConfirmados + totalCancelados);

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
    // UTILIDADES PRIVADAS
    // ========================================

    private User obtenerUsuarioAutenticado(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        return userService.buscarPorUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private Order parsearPedido(Object pedidoObj) {
        // Implementar parsing de JSON a Order
        // Por simplicidad, asumiendo que viene correctamente mapeado
        return (Order) pedidoObj;
    }

    private List<OrderDetail> parsearDetalles(Object detallesObj) {
        // Implementar parsing de JSON a List<OrderDetail>
        return (List<OrderDetail>) detallesObj;
    }

    private Map<String, Object> crearErrorResponse(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", mensaje);
        return response;
    }
}