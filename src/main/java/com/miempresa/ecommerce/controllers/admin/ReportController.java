package com.miempresa.ecommerce.controllers.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.miempresa.ecommerce.config.EmpresaConfig;
import com.miempresa.ecommerce.models.Sale;
import com.miempresa.ecommerce.services.CreditService;
import com.miempresa.ecommerce.services.CustomerService;
import com.miempresa.ecommerce.services.ProductService;
import com.miempresa.ecommerce.services.SaleService;
import com.miempresa.ecommerce.utils.PdfGeneratorUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CONTROLLER: REPORTES
 * 
 * Gestiona la generación de reportes y estadísticas del sistema.
 * Ahora usa PdfGeneratorUtil para mantener coherencia.
 */

@Controller
@RequestMapping("/admin/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SaleService saleService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final CreditService creditService;
    private final EmpresaConfig empresaConfig;

    // ========================================
    // ÍNDICE DE REPORTES
    // ========================================

    @GetMapping
    public String index(Model model) {
        model.addAttribute("titulo", "Reportes y Estadísticas");
        return "admin/reportes/index";
    }

    // ========================================
    // REPORTE DE VENTAS
    // ========================================

    /**
     * Muestra el reporte de ventas en pantalla
     * 
     * URL: GET /admin/reportes/ventas
     * Vista: admin/reportes/ventas.html
     */
    @GetMapping("/ventas")
    public String reporteVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String periodo,
            Model model) {

        // ✅ Procesar período si se especifica
        LocalDateTime ahora = LocalDateTime.now();

        if (periodo != null) {
            switch (periodo) {
                case "hoy":
                    fechaInicio = ahora.withHour(0).withMinute(0).withSecond(0);
                    fechaFin = ahora;
                    break;
                case "ayer":
                    fechaInicio = ahora.minusDays(1).withHour(0).withMinute(0).withSecond(0);
                    fechaFin = ahora.minusDays(1).withHour(23).withMinute(59).withSecond(59);
                    break;
                case "semana":
                    fechaInicio = ahora.minusDays(7).withHour(0).withMinute(0).withSecond(0);
                    fechaFin = ahora;
                    break;
                case "mes":
                    fechaInicio = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                    fechaFin = ahora;
                    break;
                default:
                    break;
            }
        }

        // Valores por defecto: mes actual
        if (fechaInicio == null) {
            fechaInicio = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }
        if (fechaFin == null) {
            fechaFin = ahora;
        }

        log.debug("Mostrando reporte de ventas: {} - {}", fechaInicio, fechaFin);

        List<Sale> ventas = saleService.buscarPorFechas(fechaInicio, fechaFin);
        BigDecimal totalVentas = saleService.calcularTotalVentasPorFecha(fechaInicio, fechaFin);

        model.addAttribute("ventas", ventas);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("titulo", "Reporte de Ventas");

        return "admin/reportes/ventas";
    }

    /**
     * Genera PDF de reporte de ventas usando PdfGeneratorUtil
     * 
     * URL: GET /admin/reportes/ventas/pdf
     * Retorna: PDF para descargar
     */
    @GetMapping("/ventas/pdf")
    public ResponseEntity<byte[]> generarPdfVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String periodo) {

        try {
            // Determinar fechas según período
            LocalDateTime ahora = LocalDateTime.now();

            if (periodo != null) {
                switch (periodo) {
                    case "dia":
                        fechaInicio = ahora.withHour(0).withMinute(0).withSecond(0);
                        fechaFin = ahora;
                        break;
                    case "semana":
                        fechaInicio = ahora.minusDays(7).withHour(0).withMinute(0).withSecond(0);
                        fechaFin = ahora;
                        break;
                    case "mes":
                        fechaInicio = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                        fechaFin = ahora;
                        break;
                    case "anio":
                        fechaInicio = ahora.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
                        fechaFin = ahora;
                        break;
                    default:
                        break;
                }
            }

            // Valores por defecto si no se especificaron
            if (fechaInicio == null) {
                fechaInicio = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            }
            if (fechaFin == null) {
                fechaFin = ahora;
            }

            log.info("Generando PDF de reporte de ventas desde {} hasta {}", fechaInicio, fechaFin);

            // Obtener datos
            List<Sale> ventas = saleService.buscarPorFechas(fechaInicio, fechaFin);
            BigDecimal totalVentas = saleService.calcularTotalVentasPorFecha(fechaInicio, fechaFin);

            // ✅ USAR PdfGeneratorUtil para mantener coherencia
            byte[] pdfBytes = PdfGeneratorUtil.generateReporteVentasPdf(
                    ventas,
                    totalVentas,
                    fechaInicio,
                    fechaFin,
                    empresaConfig.getNombre(),
                    empresaConfig.getRuc());

            // Configurar headers de respuesta
            String nombreArchivo = PdfGeneratorUtil.generarNombreArchivo("ventas");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);

            log.info("Reporte PDF generado exitosamente: {} ventas, total S/ {}",
                    ventas.size(), totalVentas);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error al generar PDF de ventas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error inesperado al generar PDF de ventas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // REPORTE DE PRODUCTOS
    // ========================================

    /**
     * Muestra el reporte de productos
     * 
     * URL: GET /admin/reportes/productos
     * Vista: admin/reportes/productos.html
     */
    @GetMapping("/productos")
    public String reporteProductos(Model model) {
        log.debug("Mostrando reporte de productos");

        model.addAttribute("productos", productService.obtenerActivos());
        model.addAttribute("stockBajo", productService.obtenerProductosStockBajo());
        model.addAttribute("sinStock", productService.obtenerProductosSinStock());
        model.addAttribute("titulo", "Reporte de Productos");

        return "admin/reportes/productos";
    }

    /**
     * ✅ NUEVO: Genera PDF de inventario completo
     *
     * URL: GET /admin/reportes/productos/pdf
     * Retorna: PDF para descargar
     */
    @GetMapping("/productos/pdf")
    public ResponseEntity<byte[]> generarPdfInventario() {
        try {
            log.info("Generando PDF de inventario completo");

            // Obtener todos los productos activos
            var productos = productService.obtenerActivos();

            // Generar PDF usando PdfGeneratorUtil
            byte[] pdfBytes = PdfGeneratorUtil.generateReporteInventarioPdf(
                    productos,
                    empresaConfig.getNombre(),
                    empresaConfig.getRuc());

            // Configurar headers de respuesta
            String nombreArchivo = PdfGeneratorUtil.generarNombreArchivo("inventario");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);

            log.info("Reporte PDF de inventario generado exitosamente: {} productos", productos.size());

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error al generar PDF de inventario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error inesperado al generar PDF de inventario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // REPORTE DE CRÉDITOS
    // ========================================

    /**
     * Muestra el reporte de créditos
     * 
     * URL: GET /admin/reportes/creditos
     * Vista: admin/reportes/creditos.html
     */
    @GetMapping("/creditos")
    public String reporteCreditos(Model model) {
        log.debug("Mostrando reporte de créditos");

        model.addAttribute("creditosActivos", creditService.obtenerActivos());
        model.addAttribute("cuotasVencidas", creditService.obtenerCuotasVencidas());
        model.addAttribute("deudaTotal", creditService.obtenerTotalDeudaPendiente());
        model.addAttribute("titulo", "Reporte de Créditos");

        return "admin/reportes/creditos";
    }

    /**
     * ✅ NUEVO: Genera PDF de reporte de créditos
     *
     * URL: GET /admin/reportes/creditos/pdf
     * Retorna: PDF para descargar
     */
    @GetMapping("/creditos/pdf")
    public ResponseEntity<byte[]> generarPdfCreditos() {
        try {
            log.info("Generando PDF de reporte de créditos");

            // Obtener datos
            var creditos = creditService.obtenerActivos();
            var deudaTotal = creditService.obtenerTotalDeudaPendiente();
            var cuotasVencidas = creditService.obtenerCuotasVencidas();

            // Generar PDF usando PdfGeneratorUtil
            byte[] pdfBytes = PdfGeneratorUtil.generateReporteCreditosPdf(
                    creditos,
                    deudaTotal,
                    cuotasVencidas.size(),
                    empresaConfig.getNombre(),
                    empresaConfig.getRuc());

            // Configurar headers de respuesta
            String nombreArchivo = PdfGeneratorUtil.generarNombreArchivo("creditos");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);

            log.info("Reporte PDF de créditos generado exitosamente");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error al generar PDF de créditos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error inesperado al generar PDF de créditos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

/**
 * MEJORAS IMPLEMENTADAS:
 * 
 * ✅ Usa PdfGeneratorUtil en lugar de código duplicado
 * ✅ Inyecta EmpresaConfig para datos centralizados
 * ✅ Usa método generarNombreArchivo() de la utilidad
 * ✅ Logs más descriptivos con contexto
 * ✅ Coherencia con el resto del sistema
 * ✅ Eliminadas 80+ líneas de código duplicado
 * 
 * CONFIGURACIÓN REQUERIDA:
 * 
 * En application.properties:
 * empresa.nombre=MI EMPRESA SAC
 * empresa.ruc=20123456789
 * empresa.direccion=Av. Principal 123, Lima
 * empresa.telefono=01-1234567
 * empresa.email=ventas@miempresa.com
 */