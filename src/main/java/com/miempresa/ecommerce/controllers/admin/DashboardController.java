package com.miempresa.ecommerce.controllers.admin;

import com.miempresa.ecommerce.security.SecurityUtils;
import com.miempresa.ecommerce.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * CONTROLLER: DASHBOARD
 * 
 * Muestra el panel principal del admin con estadísticas.
 */

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final SaleService saleService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final CreditService creditService;

    /**
     * Dashboard principal
     * 
     * URL: GET /admin/dashboard
     * Vista: templates/admin/dashboard.html
     */
    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        log.info("Usuario {} accedió al dashboard", SecurityUtils.getCurrentUsername());

        try {
            // ========================================
            // ESTADÍSTICAS DEL DÍA
            // ========================================

            LocalDateTime inicioDia = LocalDateTime.now().with(LocalTime.MIN);
            LocalDateTime finDia = LocalDateTime.now().with(LocalTime.MAX);

            // Ventas del día
            BigDecimal ventasDelDia = saleService.calcularTotalVentasPorFecha(inicioDia, finDia);
            model.addAttribute("ventasDelDia", ventasDelDia);

            // Pedidos pendientes
            long pedidosPendientes = orderService.contarPorEstado(
                    com.miempresa.ecommerce.models.enums.EstadoPedido.PENDIENTE);
            model.addAttribute("pedidosPendientes", pedidosPendientes);

            // ========================================
            // ESTADÍSTICAS GENERALES
            // ========================================

            // Total de productos
            long totalProductos = productService.contarActivos();
            model.addAttribute("totalProductos", totalProductos);

            // Total de clientes
            long totalClientes = customerService.contarActivos();
            model.addAttribute("totalClientes", totalClientes);

            // Productos con stock bajo
            int productosStockBajo = productService.obtenerProductosStockBajo().size();
            model.addAttribute("productosStockBajo", productosStockBajo);

            // ========================================
            // CRÉDITOS Y CUOTAS
            // ========================================

            // Créditos activos
            long creditosActivos = creditService.contarActivos();
            model.addAttribute("creditosActivos", creditosActivos);

            // Deuda total pendiente
            BigDecimal deudaTotal = creditService.obtenerTotalDeudaPendiente();
            model.addAttribute("deudaTotal", deudaTotal);

            // Cuotas vencidas
            long cuotasVencidas = creditService.contarCuotasVencidas();
            model.addAttribute("cuotasVencidas", cuotasVencidas);

            // ========================================
            // LISTAS PARA WIDGETS
            // ========================================

            // Últimas ventas
            model.addAttribute("ultimasVentas", saleService.obtenerDelDia());

            // Últimos pedidos
            model.addAttribute("ultimosPedidos", orderService.obtenerPendientes());

            // Productos con stock bajo
            model.addAttribute("productosAlerta", productService.obtenerProductosStockBajo());

            // Usuario actual
            model.addAttribute("usuarioActual", SecurityUtils.getCurrentUsername());

            log.debug("Dashboard cargado con éxito");

        } catch (Exception e) {
            log.error("Error al cargar dashboard: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar las estadísticas");
        }

        return "admin/dashboard";
    }

    /**
     * Redirige /admin a /admin/dashboard
     */
    @GetMapping
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }
}

/**
 * EXPLICACIÓN:
 * 
 * 1. ¿Qué hace @RequestMapping("/admin")?
 * - Prefijo para todas las rutas de este controller
 * - /admin/dashboard, /admin/ventas, etc.
 * 
 * 2. ¿Qué son las dependencias inyectadas?
 * - SaleService, ProductService, etc.
 * - Spring las inyecta automáticamente
 * - @RequiredArgsConstructor de Lombok crea el constructor
 * 
 * 3. Flujo del método:
 * 
 * Usuario visita /admin/dashboard
 * ↓
 * Controller llama a varios services:
 * - saleService.calcularTotalVentasPorFecha()
 * - productService.contarActivos()
 * - customerService.contarActivos()
 * ↓
 * Services consultan la base de datos
 * ↓
 * Controller recibe los datos
 * ↓
 * Controller pasa los datos al Model
 * ↓
 * Vista (dashboard.html) recibe el Model
 * ↓
 * Thymeleaf renderiza con los datos
 * ↓
 * Browser muestra el dashboard
 * 
 * 4. Ejemplo de datos en la vista:
 * 
 * Controller:
 * model.addAttribute("ventasDelDia", 1500.50);
 * 
 * Vista (dashboard.html):
 * <h3 th:text="${ventasDelDia}">0</h3>
 * 
 * Resultado:
 * <h3>1500.50</h3>
 * 
 * 5. ¿Por qué try-catch?
 * - Si algún service falla, no rompe toda la página
 * - Se muestra un mensaje de error
 * - La aplicación sigue funcionando
 */