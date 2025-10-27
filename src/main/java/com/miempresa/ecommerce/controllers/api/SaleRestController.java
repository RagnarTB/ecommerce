package com.miempresa.ecommerce.controllers.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList; // <<--- AÑADIR import

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

// <<--- IMPORTAR DTOs INTERNOS SI LOS MOVIERON A CLASES SEPARADAS --->>>
// import com.miempresa.ecommerce.controllers.admin.SaleController.PagoRequest;
// import com.miempresa.ecommerce.controllers.admin.SaleController.VentaPosRequest;
// import com.miempresa.ecommerce.controllers.admin.SaleController.VentaProducto;
import com.miempresa.ecommerce.models.Customer; // Importar Customer si no estaba
import com.miempresa.ecommerce.models.Product; // Importar Product si no estaba
import com.miempresa.ecommerce.security.SecurityUtils; // Importar SecurityUtils si no estaba
import com.miempresa.ecommerce.services.CustomerService; // Importar CustomerService si no estaba
import com.miempresa.ecommerce.services.ProductService; // Importar ProductService si no estaba

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/ventas") // <<--- CORREGIR RUTA SI ES DIFERENTE
@RequiredArgsConstructor
@Slf4j
public class SaleRestController {

    private final SaleService saleService;
    private final CreditService creditService;
    private final UserService userService;
    // <<--- AÑADIR SERVICIOS FALTANTES SI LOS USAS EN LOS PARSERS --->>>
    private final CustomerService customerService;
    private final ProductService productService;

    // ========================================
    // CREAR VENTA (desde API genérica, NO POS)
    // ========================================
    /**
     * POST /api/ventas
     * Crea una nueva venta desde una llamada API genérica.
     * El cuerpo del request debe contener toda la información.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Recibida solicitud POST /api/ventas. Request: {}", request);
        try {
            // 1. Obtener Usuario Autenticado (Si la API requiere autenticación)
            User usuario;
            if (userDetails != null) {
                usuario = obtenerUsuarioAutenticado(userDetails);
                log.info("Usuario autenticado: {}", usuario.getUsername());
            } else {
                // Si la API permite ventas anónimas o usa otra autenticación, ajustar aquí.
                // Por ahora, asumimos que requiere un usuario logueado.
                log.error("Intento de crear venta API sin usuario autenticado.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(crearErrorResponse("Se requiere autenticación para crear una venta por API."));
            }

            // 2. Parsear y Validar Datos del Request
            // (Estos métodos parsearX deben extraer y validar datos del Map 'request')
            Sale ventaBase = parsearVenta(request.get("venta")); // Obtiene clienteId, tipoPago, etc. del sub-objeto
                                                                 // "venta"
            List<SaleDetail> detalles = parsearDetalles(request.get("detalles")); // Obtiene [{productoId, cantidad,
                                                                                  // precio?}, ...]
            List<Payment> pagos = parsearPagos(request.get("pagos")); // Obtiene [{metodoPago, monto}, ...]
            Integer numCuotas = request.containsKey("numCuotas") ? (Integer) request.get("numCuotas") : null;
            // <<--- OBTENER DESCUENTO Y ENVIO DEL REQUEST --->>>
            BigDecimal descuento = request.containsKey("descuento")
                    ? new BigDecimal(request.get("descuento").toString())
                    : BigDecimal.ZERO;
            BigDecimal costoEnvio = request.containsKey("costoEnvio")
                    ? new BigDecimal(request.get("costoEnvio").toString())
                    : BigDecimal.ZERO;

            // 3. Validaciones Adicionales
            if (detalles == null || detalles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse("La lista de 'detalles' es obligatoria y no puede estar vacía."));
            }
            if (ventaBase.getTipoPago() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse(
                                "El campo 'tipoPago' dentro de 'venta' es obligatorio (CONTADO o CREDITO)."));
            }
            if (ventaBase.getCliente() == null || ventaBase.getCliente().getId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse("El campo 'cliente' con 'id' dentro de 'venta' es obligatorio."));
            }

            // Validar cuotas si es crédito
            if (ventaBase.getTipoPago() == TipoPago.CREDITO) {
                if (numCuotas == null || numCuotas < 1 || numCuotas > 24) { // Ajusta max si es necesario
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(crearErrorResponse(
                                    "Para ventas a crédito, 'numCuotas' debe ser un número entre 1 y 24."));
                }
            }
            // Validar pagos si es contado
            if (ventaBase.getTipoPago() == TipoPago.CONTADO && (pagos == null || pagos.isEmpty())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearErrorResponse(
                                "Para ventas al contado, la lista 'pagos' es obligatoria y debe contener al menos un pago."));
            }

            // 4. Llamar al Servicio para Crear Venta
            log.info("Llamando a SaleService.crearVenta desde API...");
            // <<--- CORREGIR LA LLAMADA AQUÍ --->>>
            Sale ventaGuardada = saleService.crearVenta(
                    ventaBase,
                    detalles,
                    pagos,
                    usuario,
                    numCuotas,
                    descuento, // <<--- PASAR DESCUENTO
                    costoEnvio // <<--- PASAR COSTOENVIO
            );

            // 5. Preparar Respuesta Exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Venta creada exitosamente vía API.");
            response.put("data", ventaGuardada); // Devolver la venta completa creada

            log.info("Venta {} creada exitosamente vía API.", ventaGuardada.getNumeroVenta());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException | NullPointerException e) { // Capturar errores de parsing o datos faltantes
            log.error("Error en los datos recibidos para crear venta API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse("Datos inválidos o faltantes en la solicitud: " + e.getMessage()));
        } catch (RuntimeException e) { // Capturar errores de negocio (stock, etc.)
            log.error("Error de negocio al crear venta API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) // Usar Bad Request para errores de negocio
                    .body(crearErrorResponse(e.getMessage()));
        } catch (Exception e) { // Capturar errores inesperados
            log.error("Error inesperado al crear venta API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error interno al procesar la venta."));
        }
    }

    // ========================================
    // CONSULTAS (Métodos GET sin cambios)
    // ========================================
    // ... (Mantener los métodos GET /api/ventas, /{id}, /numero/{num},
    // /cliente/{id}, /dia, /mes, /rango como estaban) ...

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodas(
            @RequestParam(required = false) String periodo) {
        try {
            List<Sale> ventas;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

            if ("dia".equalsIgnoreCase(periodo)) {
                log.info("API: Obteniendo ventas del día {}", now.toLocalDate());
                ventas = saleService.buscarPorFechas(startOfDay, now); // Usar buscarPorFechas
            } else if ("mes".equalsIgnoreCase(periodo)) {
                log.info("API: Obteniendo ventas del mes {}", now.getMonth());
                ventas = saleService.buscarPorFechas(startOfMonth, now); // Usar buscarPorFechas
            } else {
                log.info("API: Obteniendo todas las ventas");
                ventas = saleService.obtenerTodas();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", ventas);
            response.put("total", ventas.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error API al obtener ventas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al obtener ventas: " + e.getMessage()));
        }
    }

    // --- MANTENER LOS DEMÁS GET COMO ESTABAN ---
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<Sale> venta = saleService.buscarPorId(id);

            if (venta.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Venta no encontrada con ID: " + id));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", venta.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error API al obtener venta ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al obtener venta: " + e.getMessage()));
        }
    }

    @GetMapping("/numero/{numeroVenta}")
    public ResponseEntity<Map<String, Object>> obtenerPorNumero(@PathVariable String numeroVenta) {
        try {
            Optional<Sale> venta = saleService.buscarPorNumero(numeroVenta);

            if (venta.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Venta no encontrada con número: " + numeroVenta));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", venta.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error API al obtener venta N° {}: {}", numeroVenta, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al obtener venta: " + e.getMessage()));
        }
    }
    // --- ETC ---

    // ========================================
    // ANULAR VENTA (Método POST sin cambios)
    // ========================================
    @PostMapping("/{id}/anular")
    public ResponseEntity<Map<String, Object>> anular(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Recibida solicitud POST /api/ventas/{}/anular", id);
        try {
            User usuario = obtenerUsuarioAutenticado(userDetails);
            saleService.anularVenta(id, usuario); // El servicio maneja la lógica y excepciones

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Venta anulada exitosamente vía API.");

            log.info("Venta ID {} anulada exitosamente vía API por usuario {}", id, usuario.getUsername());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) { // Capturar errores de negocio específicos
            log.error("Error de negocio al anular venta API ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));
        } catch (Exception e) { // Capturar errores inesperados
            log.error("Error inesperado al anular venta API ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error interno al anular la venta."));
        }
    }

    // ========================================
    // ABONOS A CRÉDITO (Método POST sin cambios)
    // ========================================
    @PostMapping("/creditos/{creditoId}/abono")
    public ResponseEntity<Map<String, Object>> registrarAbono(
            @PathVariable Long creditoId,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Recibida solicitud POST /api/ventas/creditos/{}/abono. Request: {}", creditoId, request);
        try {
            User usuario = obtenerUsuarioAutenticado(userDetails);

            // Parsear y validar datos del request
            if (!request.containsKey("monto") || !request.containsKey("metodoPago")) {
                throw new IllegalArgumentException("Los campos 'monto' y 'metodoPago' son obligatorios.");
            }
            BigDecimal monto = new BigDecimal(request.get("monto").toString());
            MetodoPago metodoPago = MetodoPago.valueOf(request.get("metodoPago").toString().toUpperCase()); // Tolerar
                                                                                                            // may/min
            String referencia = request.containsKey("referencia") ? request.get("referencia").toString() : null;

            // Llamar al servicio para registrar el abono
            Payment pago = creditService.registrarAbono(creditoId, monto, metodoPago, referencia, usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Abono registrado exitosamente vía API.");
            response.put("data", pago); // Devolver el pago registrado

            log.info("Abono registrado exitosamente para crédito ID {} vía API.", creditoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException | NullPointerException e) { // Capturar errores de parsing o datos faltantes
            log.error("Error en los datos recibidos para registrar abono API: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse("Datos inválidos o faltantes: " + e.getMessage()));
        } catch (RuntimeException e) { // Capturar errores de negocio (monto excede, etc.)
            log.error("Error de negocio al registrar abono API para crédito ID {}: {}", creditoId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));
        } catch (Exception e) { // Capturar errores inesperados
            log.error("Error inesperado al registrar abono API para crédito ID {}: {}", creditoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error interno al registrar el abono."));
        }
    }

    // ========================================
    // ESTADÍSTICAS (Métodos GET sin cambios)
    // ========================================
    // ... (Mantener los métodos GET /estadisticas y /estadisticas/dia como estaban)
    // ...
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        try {
            LocalDateTime now = LocalDateTime.now();
            // Si no se especifican fechas, usar el mes actual por defecto
            if (fechaInicio == null) {
                fechaInicio = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            }
            if (fechaFin == null) {
                fechaFin = now; // Hasta el momento actual
            }
            log.info("API: Calculando estadísticas de ventas entre {} y {}", fechaInicio, fechaFin);

            List<Sale> ventas = saleService.buscarPorFechas(fechaInicio, fechaFin);
            BigDecimal totalVentas = saleService.calcularTotalVentasPorFecha(fechaInicio, fechaFin);
            int cantidadVentas = ventas.size();
            BigDecimal promedioVenta = cantidadVentas > 0
                    ? totalVentas.divide(BigDecimal.valueOf(cantidadVentas), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalVentas", totalVentas != null ? totalVentas : BigDecimal.ZERO);
            estadisticas.put("cantidadVentas", cantidadVentas);
            estadisticas.put("promedioVenta", promedioVenta);
            estadisticas.put("fechaInicio", fechaInicio.format(DateTimeFormatter.ISO_DATE_TIME)); // Formato estándar
            estadisticas.put("fechaFin", fechaFin.format(DateTimeFormatter.ISO_DATE_TIME));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", estadisticas);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error API al obtener estadísticas de ventas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al calcular estadísticas: " + e.getMessage()));
        }
    }
    // --- MANTENER obtenerEstadisticasDelDia COMO ESTABA ---

    // ========================================
    // UTILIDADES PRIVADAS (Actualizar/Añadir parsers si es necesario)
    // ========================================

    private User obtenerUsuarioAutenticado(UserDetails userDetails) {
        if (userDetails == null) {
            // Considerar si la API permite llamadas sin autenticar o si siempre debe haber
            // uno
            log.warn("Llamada API sin UserDetails (usuario no autenticado).");
            throw new RuntimeException("Usuario no autenticado"); // Lanzar error si es requerido
            // return null; // O devolver null si se permiten llamadas anónimas
        }
        String username = userDetails.getUsername();
        log.debug("Buscando usuario autenticado: {}", username);
        return userService.buscarPorUsername(username)
                .orElseThrow(() -> {
                    log.error("Usuario '{}' autenticado no encontrado en la base de datos.", username);
                    return new RuntimeException("Usuario autenticado '" + username + "' no encontrado en BD.");
                });
    }

    // --- Métodos parsearVenta, parsearDetalles, parsearPagos ---
    // Asegúrate de que estos métodos extraigan correctamente los datos del Map
    // y realicen validaciones básicas (tipos, campos requeridos)
    // Devolverán los objetos de entidad correspondientes.

    // Ejemplo básico (requiere más validación y buscar entidades relacionadas)
    private Sale parsearVenta(Object ventaObj) {
        if (!(ventaObj instanceof Map))
            throw new IllegalArgumentException("El objeto 'venta' debe ser un mapa JSON.");
        Map<?, ?> ventaMap = (Map<?, ?>) ventaObj;

        Sale venta = new Sale();

        // Cliente (asume que viene el ID)
        if (!ventaMap.containsKey("clienteId"))
            throw new IllegalArgumentException("Falta 'clienteId' en el objeto 'venta'.");
        Long clienteId = Long.parseLong(ventaMap.get("clienteId").toString());
        Customer cliente = customerService.buscarPorId(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente con ID " + clienteId + " no encontrado."));
        venta.setCliente(cliente);

        // Tipo Pago
        if (!ventaMap.containsKey("tipoPago"))
            throw new IllegalArgumentException("Falta 'tipoPago' en el objeto 'venta'.");
        venta.setTipoPago(TipoPago.valueOf(ventaMap.get("tipoPago").toString().toUpperCase()));

        // Otros campos opcionales que podrían venir en 'venta' (ej. observaciones)
        if (ventaMap.containsKey("observaciones")) {
            venta.setObservaciones(ventaMap.get("observaciones").toString());
        }
        // Nota: Los totales, descuento, envio se pasan aparte a crearVenta ahora.

        return venta;
    }

    private List<SaleDetail> parsearDetalles(Object detallesObj) {
        if (!(detallesObj instanceof List))
            throw new IllegalArgumentException("El objeto 'detalles' debe ser una lista JSON.");
        List<?> detallesList = (List<?>) detallesObj;
        List<SaleDetail> detallesResult = new ArrayList<>();

        for (Object itemObj : detallesList) {
            if (!(itemObj instanceof Map))
                throw new IllegalArgumentException("Cada item en 'detalles' debe ser un mapa JSON.");
            Map<?, ?> itemMap = (Map<?, ?>) itemObj;

            // Validar campos requeridos por detalle
            if (!itemMap.containsKey("productoId") || !itemMap.containsKey("cantidad")) {
                throw new IllegalArgumentException("Cada detalle debe contener 'productoId' y 'cantidad'.");
            }

            Long productoId = Long.parseLong(itemMap.get("productoId").toString());
            Integer cantidad = Integer.parseInt(itemMap.get("cantidad").toString());

            if (cantidad <= 0)
                throw new IllegalArgumentException(
                        "La cantidad para el producto ID " + productoId + " debe ser mayor a cero.");

            // Buscar producto
            Product producto = productService.buscarPorId(productoId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("Producto con ID " + productoId + " no encontrado."));

            // Podrías permitir que el precio venga en el request o usar el actual del
            // producto
            BigDecimal precioUnitario = producto.getPrecioActual(); // Usar precio actual como default
            if (itemMap.containsKey("precioUnitario")) { // Permitir override desde API
                precioUnitario = new BigDecimal(itemMap.get("precioUnitario").toString());
            }

            SaleDetail detalle = SaleDetail.builder()
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(precioUnitario) // Usar precio determinado
                    // nombre, sku, subtotal se calcularán/establecerán en el servicio
                    .build();
            detallesResult.add(detalle);
        }
        return detallesResult;
    }

    private List<Payment> parsearPagos(Object pagosObj) {
        // Solo parsear si es CONTADO
        // Si es CRÉDITO, esta lista debería estar vacía o ser null,
        // excepto si se permite un pago inicial.
        if (pagosObj == null)
            return new ArrayList<>(); // Devolver lista vacía si no hay pagos
        if (!(pagosObj instanceof List))
            throw new IllegalArgumentException("El objeto 'pagos' debe ser una lista JSON.");
        List<?> pagosList = (List<?>) pagosObj;
        List<Payment> pagosResult = new ArrayList<>();

        for (Object itemObj : pagosList) {
            if (!(itemObj instanceof Map))
                throw new IllegalArgumentException("Cada item en 'pagos' debe ser un mapa JSON.");
            Map<?, ?> itemMap = (Map<?, ?>) itemObj;

            if (!itemMap.containsKey("metodoPago") || !itemMap.containsKey("monto")) {
                throw new IllegalArgumentException("Cada pago debe contener 'metodoPago' y 'monto'.");
            }

            MetodoPago metodo = MetodoPago.valueOf(itemMap.get("metodoPago").toString().toUpperCase());
            BigDecimal monto = new BigDecimal(itemMap.get("monto").toString());
            String referencia = itemMap.containsKey("referencia") ? itemMap.get("referencia").toString() : null;

            if (monto.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("El monto del pago debe ser mayor a cero.");

            Payment pago = Payment.builder()
                    .metodoPago(metodo)
                    .monto(monto)
                    .referencia(referencia)
                    // venta y usuario se asignan en el servicio
                    .build();
            pagosResult.add(pago);
        }
        return pagosResult;
    }

    private Map<String, Object> crearErrorResponse(String mensaje) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", mensaje);
        return response;
    }
}