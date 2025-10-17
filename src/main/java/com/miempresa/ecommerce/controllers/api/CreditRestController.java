package com.miempresa.ecommerce.controllers.api;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.miempresa.ecommerce.models.Credit;
import com.miempresa.ecommerce.models.Installment;
import com.miempresa.ecommerce.models.Payment;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.models.enums.MetodoPago;
import com.miempresa.ecommerce.services.CreditService;
import com.miempresa.ecommerce.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API: CRÉDITOS
 * 
 * Endpoints para gestión de créditos y cuotas desde frontend/mobile
 * Base URL: /api/creditos
 */
@RestController
@RequestMapping("/api/creditos")
@RequiredArgsConstructor
@Slf4j
public class CreditRestController {

    private final CreditService creditService;
    private final UserService userService;

    // ========================================
    // CONSULTAS DE CRÉDITOS
    // ========================================

    /**
     * GET /api/creditos
     * Obtiene todos los créditos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodos(
            @RequestParam(required = false) Boolean activos) {
        try {
            List<Credit> creditos = activos != null && activos
                    ? creditService.obtenerActivos()
                    : creditService.obtenerTodos();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", creditos);
            response.put("total", creditos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener créditos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/creditos/{id}
     * Obtiene un crédito por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<Credit> credito = creditService.buscarPorId(id);

            if (credito.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Crédito no encontrado"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", credito.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener crédito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/creditos/venta/{ventaId}
     * Obtiene el crédito de una venta
     */
    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<Map<String, Object>> obtenerPorVenta(@PathVariable Long ventaId) {
        try {
            Optional<Credit> credito = creditService.buscarPorVenta(ventaId);

            if (credito.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("No se encontró crédito para esta venta"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", credito.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener crédito por venta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/creditos/cliente/{clienteId}
     * Obtiene créditos de un cliente
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Map<String, Object>> obtenerPorCliente(@PathVariable Long clienteId) {
        try {
            List<Credit> creditos = creditService.obtenerPorCliente(clienteId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", creditos);
            response.put("total", creditos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener créditos del cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/creditos/activos
     * Obtiene créditos activos
     */
    @GetMapping("/activos")
    public ResponseEntity<Map<String, Object>> obtenerActivos() {
        try {
            List<Credit> creditos = creditService.obtenerActivos();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", creditos);
            response.put("total", creditos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener créditos activos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // ALERTAS Y VENCIMIENTOS
    // ========================================

    /**
     * GET /api/creditos/con-cuotas-vencidas
     * Obtiene créditos con cuotas vencidas
     */
    @GetMapping("/con-cuotas-vencidas")
    public ResponseEntity<Map<String, Object>> obtenerConCuotasVencidas() {
        try {
            List<Credit> creditos = creditService.obtenerCreditosConCuotasVencidas();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", creditos);
            response.put("total", creditos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener créditos con cuotas vencidas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/creditos/proximos-a-vencer
     * Obtiene créditos con cuotas próximas a vencer
     * 
     * Params: dias (default: 7)
     */
    @GetMapping("/proximos-a-vencer")
    public ResponseEntity<Map<String, Object>> obtenerProximosAVencer(
            @RequestParam(defaultValue = "7") int dias) {
        try {
            List<Credit> creditos = creditService.obtenerCreditosConCuotasProximasAVencer(dias);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", creditos);
            response.put("total", creditos.size());
            response.put("dias", dias);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener créditos próximos a vencer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // CONSULTAS DE CUOTAS
    // ========================================

    /**
     * GET /api/creditos/{id}/cuotas
     * Obtiene las cuotas de un crédito
     */
    @GetMapping("/{id}/cuotas")
    public ResponseEntity<Map<String, Object>> obtenerCuotas(@PathVariable Long id) {
        try {
            List<Installment> cuotas = creditService.obtenerCuotasDeCredito(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cuotas);
            response.put("total", cuotas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener cuotas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/creditos/cuotas/vencidas
     * Obtiene todas las cuotas vencidas
     */
    @GetMapping("/cuotas/vencidas")
    public ResponseEntity<Map<String, Object>> obtenerCuotasVencidas() {
        try {
            List<Installment> cuotas = creditService.obtenerCuotasVencidas();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cuotas);
            response.put("total", cuotas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener cuotas vencidas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/creditos/cuotas/vencen-hoy
     * Obtiene cuotas que vencen hoy
     */
    @GetMapping("/cuotas/vencen-hoy")
    public ResponseEntity<Map<String, Object>> obtenerCuotasQueVencenHoy() {
        try {
            List<Installment> cuotas = creditService.obtenerCuotasQueVencenHoy();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cuotas);
            response.put("total", cuotas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener cuotas que vencen hoy: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/creditos/cuotas/proximas-a-vencer
     * Obtiene cuotas próximas a vencer
     * 
     * Params: dias (default: 7)
     */
    @GetMapping("/cuotas/proximas-a-vencer")
    public ResponseEntity<Map<String, Object>> obtenerCuotasProximasAVencer(
            @RequestParam(defaultValue = "7") int dias) {
        try {
            List<Installment> cuotas = creditService.obtenerCuotasProximasAVencer(dias);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cuotas);
            response.put("total", cuotas.size());
            response.put("dias", dias);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener cuotas próximas a vencer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // ABONOS
    // ========================================

    /**
     * POST /api/creditos/{id}/abono
     * Registra un abono a un crédito
     * 
     * Body:
     * {
     * "monto": 100.00,
     * "metodoPago": "EFECTIVO",
     * "referencia": "Abono parcial" (opcional)
     * }
     */
    @PostMapping("/{id}/abono")
    public ResponseEntity<Map<String, Object>> registrarAbono(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = obtenerUsuarioAutenticado(userDetails);

            // Parsear datos
            BigDecimal monto = new BigDecimal(request.get("monto").toString());
            MetodoPago metodoPago = MetodoPago.valueOf(request.get("metodoPago").toString());
            String referencia = request.containsKey("referencia")
                    ? request.get("referencia").toString()
                    : null;

            // Registrar abono
            Payment pago = creditService.registrarAbono(id, monto, metodoPago, referencia, usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Abono registrado exitosamente");
            response.put("data", pago);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al registrar abono: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al registrar abono: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al registrar abono"));
        }
    }

    // ========================================
    // ANULAR CRÉDITO
    // ========================================

    /**
     * POST /api/creditos/{id}/anular
     * Anula un crédito
     */
    @PostMapping("/{id}/anular")
    public ResponseEntity<Map<String, Object>> anular(@PathVariable Long id) {
        try {
            creditService.anularCredito(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Crédito anulado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al anular crédito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al anular crédito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al anular crédito"));
        }
    }

    // ========================================
    // ESTADÍSTICAS Y RESÚMENES
    // ========================================

    /**
     * GET /api/creditos/estadisticas
     * Obtiene estadísticas generales de créditos
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            long totalActivos = creditService.contarActivos();
            BigDecimal deudaPendienteTotal = creditService.obtenerTotalDeudaPendiente();
            long cuotasVencidas = creditService.contarCuotasVencidas();
            BigDecimal montoVencido = creditService.sumarMontoPendienteCuotasVencidas();

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("creditosActivos", totalActivos);
            estadisticas.put("deudaPendienteTotal",
                    deudaPendienteTotal != null ? deudaPendienteTotal : BigDecimal.ZERO);
            estadisticas.put("cuotasVencidas", cuotasVencidas);
            estadisticas.put("montoVencido", montoVencido != null ? montoVencido : BigDecimal.ZERO);

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
     * GET /api/creditos/cliente/{clienteId}/deuda
     * Obtiene la deuda total de un cliente
     */
    @GetMapping("/cliente/{clienteId}/deuda")
    public ResponseEntity<Map<String, Object>> obtenerDeudaCliente(@PathVariable Long clienteId) {
        try {
            BigDecimal deuda = creditService.obtenerDeudaCliente(clienteId);

            Map<String, Object> datos = new HashMap<>();
            datos.put("clienteId", clienteId);
            datos.put("deudaTotal", deuda != null ? deuda : BigDecimal.ZERO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", datos);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener deuda del cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // MANTENIMIENTO
    // ========================================

    /**
     * POST /api/creditos/actualizar-estados
     * Actualiza el estado de todas las cuotas vencidas
     */
    @PostMapping("/actualizar-estados")
    public ResponseEntity<Map<String, Object>> actualizarEstados() {
        try {
            creditService.actualizarEstadoCuotasVencidas();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estados de cuotas actualizados");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al actualizar estados: {}", e.getMessage());
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