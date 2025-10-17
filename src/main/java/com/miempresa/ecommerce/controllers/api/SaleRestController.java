package com.miempresa.ecommerce.controllers.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

import com.miempresa.ecommerce.models.Payment;
import com.miempresa.ecommerce.models.Sale;
import com.miempresa.ecommerce.models.SaleDetail;
import com.miempresa.ecommerce.models.User;
import com.miempresa.ecommerce.models.enums.MetodoPago;
import com.miempresa.ecommerce.models.enums.TipoPago;
import com.miempresa.ecommerce.services.CreditService;
import com.miempresa.ecommerce.services.SaleService;
import com.miempresa.ecommerce.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API: VENTAS
 * 
 * Endpoints para gestión de ventas desde frontend/mobile
 * Base URL: /api/ventas
 */
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Slf4j
public class SaleRestController {

    private final SaleService saleService;
    private final CreditService creditService;
    private final UserService userService;

    // ========================================
    // CREAR VENTA
    // ========================================

    /**
     * POST /api/ventas
     * Crea una nueva venta
     * 
     * Body:
     * {
     * "venta": { ... },
     * "detalles": [ ... ],
     * "pagos": [ ... ],
     * "numCuotas": 12 (opcional, solo si es a crédito)
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Obtener usuario autenticado
            User usuario = obtenerUsuarioAutenticado(userDetails);

            // Parsear datos del request
            Sale venta = parsearVenta(request.get("venta"));
            List<SaleDetail> detalles = parsearDetalles(request.get("detalles"));
            List<Payment> pagos = parsearPagos(request.get("pagos"));
            Integer numCuotas = request.containsKey("numCuotas")
                    ? (Integer) request.get("numCuotas")
                    : null;

            // Validar que haya detalles
            if (detalles == null || detalles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse("Debe incluir al menos un producto"));
            }

            // Validar número de cuotas si es a crédito
            if (venta.getTipoPago() == TipoPago.CREDITO) {
                if (numCuotas == null || numCuotas < 1 || numCuotas > 24) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(crearErrorResponse("Para ventas a crédito debe especificar entre 1 y 24 cuotas"));
                }
            }

            // Crear venta
            Sale ventaGuardada = saleService.crearVenta(venta, detalles, pagos, usuario, numCuotas);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Venta creada exitosamente");
            response.put("data", ventaGuardada);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al crear venta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al crear venta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al crear venta"));
        }
    }

    // ========================================
    // CONSULTAS
    // ========================================

    /**
     * GET /api/ventas
     * Obtiene todas las ventas
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodas(
            @RequestParam(required = false) String periodo) {
        try {
            List<Sale> ventas;

            if ("dia".equalsIgnoreCase(periodo)) {
                ventas = saleService.obtenerDelDia();
            } else if ("mes".equalsIgnoreCase(periodo)) {
                ventas = saleService.obtenerDelMes();
            } else {
                ventas = saleService.obtenerTodas();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", ventas);
            response.put("total", ventas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener ventas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/ventas/{id}
     * Obtiene una venta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<Sale> venta = saleService.buscarPorId(id);

            if (venta.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Venta no encontrada"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", venta.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener venta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/ventas/numero/{numeroVenta}
     * Obtiene una venta por su número
     */
    @GetMapping("/numero/{numeroVenta}")
    public ResponseEntity<Map<String, Object>> obtenerPorNumero(@PathVariable String numeroVenta) {
        try {
            Optional<Sale> venta = saleService.buscarPorNumero(numeroVenta);

            if (venta.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Venta no encontrada"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", venta.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener venta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/ventas/cliente/{clienteId}
     * Obtiene ventas de un cliente
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Map<String, Object>> obtenerPorCliente(@PathVariable Long clienteId) {
        try {
            List<Sale> ventas = saleService.buscarPorCliente(clienteId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", ventas);
            response.put("total", ventas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener ventas del cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/ventas/dia
     * Obtiene ventas del día actual
     */
    @GetMapping("/dia")
    public ResponseEntity<Map<String, Object>> obtenerDelDia() {
        try {
            List<Sale> ventas = saleService.obtenerDelDia();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", ventas);
            response.put("total", ventas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener ventas del día: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/ventas/mes
     * Obtiene ventas del mes actual
     */
    @GetMapping("/mes")
    public ResponseEntity<Map<String, Object>> obtenerDelMes() {
        try {
            List<Sale> ventas = saleService.obtenerDelMes();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", ventas);
            response.put("total", ventas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener ventas del mes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/ventas/rango
     * Obtiene ventas en un rango de fechas
     * 
     * Params: fechaInicio, fechaFin (formato: yyyy-MM-dd'T'HH:mm:ss)
     */
    @GetMapping("/rango")
    public ResponseEntity<Map<String, Object>> obtenerPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            List<Sale> ventas = saleService.buscarPorFechas(fechaInicio, fechaFin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", ventas);
            response.put("total", ventas.size());
            response.put("fechaInicio", fechaInicio);
            response.put("fechaFin", fechaFin);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener ventas por rango: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // ANULAR VENTA
    // ========================================

    /**
     * POST /api/ventas/{id}/anular
     * Anula una venta
     */
    @PostMapping("/{id}/anular")
    public ResponseEntity<Map<String, Object>> anular(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User usuario = obtenerUsuarioAutenticado(userDetails);
            saleService.anularVenta(id, usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Venta anulada exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al anular venta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al anular venta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al anular venta"));
        }
    }

    // ========================================
    // ABONOS A CRÉDITO
    // ========================================

    /**
     * POST /api/ventas/creditos/{creditoId}/abono
     * Registra un abono a un crédito
     * 
     * Body:
     * {
     * "monto": 100.00,
     * "metodoPago": "EFECTIVO",
     * "referencia": "Abono parcial" (opcional)
     * }
     */
    @PostMapping("/creditos/{creditoId}/abono")
    public ResponseEntity<Map<String, Object>> registrarAbono(
            @PathVariable Long creditoId,
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
            Payment pago = creditService.registrarAbono(creditoId, monto, metodoPago, referencia, usuario);

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
    // ESTADÍSTICAS
    // ========================================

    /**
     * GET /api/ventas/estadisticas
     * Obtiene estadísticas de ventas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            // Si no se especifican fechas, usar el mes actual
            if (fechaInicio == null || fechaFin == null) {
                fechaInicio = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                fechaFin = LocalDateTime.now();
            }

            List<Sale> ventas = saleService.buscarPorFechas(fechaInicio, fechaFin);
            BigDecimal totalVentas = saleService.calcularTotalVentasPorFecha(fechaInicio, fechaFin);

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalVentas", totalVentas != null ? totalVentas : BigDecimal.ZERO);
            estadisticas.put("cantidadVentas", ventas.size());
            estadisticas.put("promedioVenta", ventas.isEmpty()
                    ? BigDecimal.ZERO
                    : totalVentas.divide(BigDecimal.valueOf(ventas.size()), 2, java.math.RoundingMode.HALF_UP));
            estadisticas.put("fechaInicio", fechaInicio);
            estadisticas.put("fechaFin", fechaFin);

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
     * GET /api/ventas/estadisticas/dia
     * Estadísticas del día actual
     */
    @GetMapping("/estadisticas/dia")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDelDia() {
        try {
            LocalDateTime inicioDelDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime finDelDia = LocalDateTime.now();

            List<Sale> ventas = saleService.obtenerDelDia();
            BigDecimal totalVentas = saleService.calcularTotalVentasPorFecha(inicioDelDia, finDelDia);

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalVentas", totalVentas != null ? totalVentas : BigDecimal.ZERO);
            estadisticas.put("cantidadVentas", ventas.size());
            estadisticas.put("fecha", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", estadisticas);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener estadísticas del día: {}", e.getMessage());
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

    private Sale parsearVenta(Object ventaObj) {
        // Implementar parsing de JSON a Sale
        // Por simplicidad, asumiendo que viene correctamente mapeado
        return (Sale) ventaObj;
    }

    private List<SaleDetail> parsearDetalles(Object detallesObj) {
        // Implementar parsing de JSON a List<SaleDetail>
        return (List<SaleDetail>) detallesObj;
    }

    private List<Payment> parsearPagos(Object pagosObj) {
        // Implementar parsing de JSON a List<Payment>
        return (List<Payment>) pagosObj;
    }

    private Map<String, Object> crearErrorResponse(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", mensaje);
        return response;
    }
}