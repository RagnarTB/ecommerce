package com.miempresa.ecommerce.controllers.api;

import java.math.BigDecimal;
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
import org.springframework.web.multipart.MultipartFile;

import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.models.ProductImage;
import com.miempresa.ecommerce.services.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST API: PRODUCTOS
 * 
 * Endpoints para gestión de productos desde frontend/mobile
 * Base URL: /api/productos
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductRestController {

    private final ProductService productService;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    /**
     * GET /api/productos
     * Obtiene todos los productos activos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodos(
            @RequestParam(required = false) Boolean activos) {
        try {
            List<Product> productos = activos != null && activos
                    ? productService.obtenerActivos()
                    : productService.obtenerTodos();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", productos);
            response.put("total", productos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener productos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/productos/{id}
     * Obtiene un producto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        try {
            Optional<Product> producto = productService.buscarPorId(id);

            if (producto.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(crearErrorResponse("Producto no encontrado"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", producto.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/productos
     * Crea un nuevo producto
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(@RequestBody Product producto) {
        try {
            Product productoGuardado = productService.guardar(producto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto creado exitosamente");
            response.put("data", productoGuardado);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al crear producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al crear producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al crear producto"));
        }
    }

    /**
     * PUT /api/productos/{id}
     * Actualiza un producto existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Long id,
            @RequestBody Product producto) {
        try {
            Product productoActualizado = productService.actualizar(id, producto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto actualizado exitosamente");
            response.put("data", productoActualizado);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al actualizar producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al actualizar producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al actualizar producto"));
        }
    }

    /**
     * DELETE /api/productos/{id}
     * Elimina (desactiva) un producto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        try {
            productService.eliminar(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto eliminado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al eliminar producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al eliminar producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al eliminar producto"));
        }
    }

    // ========================================
    // BÚSQUEDAS
    // ========================================

    /**
     * GET /api/productos/buscar
     * Busca productos por nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarPorNombre(
            @RequestParam String nombre) {
        try {
            List<Product> productos = productService.buscarPorNombre(nombre);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", productos);
            response.put("total", productos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al buscar productos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/productos/categoria/{categoriaId}
     * Obtiene productos por categoría
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<Map<String, Object>> obtenerPorCategoria(
            @PathVariable Long categoriaId) {
        try {
            List<Product> productos = productService.buscarPorCategoria(categoriaId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", productos);
            response.put("total", productos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener productos por categoría: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/productos/marca/{marcaId}
     * Obtiene productos por marca
     */
    @GetMapping("/marca/{marcaId}")
    public ResponseEntity<Map<String, Object>> obtenerPorMarca(
            @PathVariable Long marcaId) {
        try {
            List<Product> productos = productService.buscarPorMarca(marcaId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", productos);
            response.put("total", productos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener productos por marca: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/productos/destacados
     * Obtiene productos destacados
     */
    @GetMapping("/destacados")
    public ResponseEntity<Map<String, Object>> obtenerDestacados() {
        try {
            List<Product> productos = productService.obtenerDestacados();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", productos);
            response.put("total", productos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener productos destacados: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/productos/ofertas
     * Obtiene productos con oferta
     */
    @GetMapping("/ofertas")
    public ResponseEntity<Map<String, Object>> obtenerConOferta() {
        try {
            List<Product> productos = productService.obtenerProductosConOferta();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", productos);
            response.put("total", productos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener productos con oferta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/productos/filtrar
     * Búsqueda avanzada con filtros
     */
    @GetMapping("/filtrar")
    public ResponseEntity<Map<String, Object>> buscarConFiltros(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long marcaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax) {
        try {
            List<Product> productos = productService.buscarConFiltros(
                    nombre, categoriaId, marcaId, precioMin, precioMax);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", productos);
            response.put("total", productos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al filtrar productos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(e.getMessage()));
        }
    }

    // ========================================
    // GESTIÓN DE STOCK
    // ========================================

    /**
     * PUT /api/productos/{id}/stock/aumentar
     * Aumenta el stock de un producto
     */
    @PutMapping("/{id}/stock/aumentar")
    public ResponseEntity<Map<String, Object>> aumentarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        try {
            Product producto = productService.aumentarStock(id, cantidad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stock aumentado exitosamente");
            response.put("data", producto);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al aumentar stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al aumentar stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al aumentar stock"));
        }
    }

    /**
     * PUT /api/productos/{id}/stock/disminuir
     * Disminuye el stock de un producto
     */
    @PutMapping("/{id}/stock/disminuir")
    public ResponseEntity<Map<String, Object>> disminuirStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        try {
            Product producto = productService.disminuirStock(id, cantidad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stock disminuido exitosamente");
            response.put("data", producto);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al disminuir stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al disminuir stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al disminuir stock"));
        }
    }

    /**
     * GET /api/productos/stock/bajo
     * Obtiene productos con stock bajo
     */
    @GetMapping("/stock/bajo")
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
     * GET /api/productos/stock/sin-stock
     * Obtiene productos sin stock
     */
    @GetMapping("/stock/sin-stock")
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
    // GESTIÓN DE IMÁGENES
    // ========================================

    /**
     * POST /api/productos/{id}/imagenes
     * Sube una imagen para un producto
     */
    @PostMapping("/{id}/imagenes")
    public ResponseEntity<Map<String, Object>> subirImagen(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean esPrincipal) {
        try {
            ProductImage imagen = productService.subirImagen(id, file, esPrincipal);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagen subida exitosamente");
            response.put("data", imagen);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Error al subir imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al subir imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al subir imagen"));
        }
    }

    /**
     * DELETE /api/productos/imagenes/{imagenId}
     * Elimina una imagen
     */
    @DeleteMapping("/imagenes/{imagenId}")
    public ResponseEntity<Map<String, Object>> eliminarImagen(@PathVariable Long imagenId) {
        try {
            productService.eliminarImagen(imagenId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagen eliminada exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al eliminar imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado al eliminar imagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al eliminar imagen"));
        }
    }

    /**
     * PUT /api/productos/imagenes/{imagenId}/principal
     * Establece una imagen como principal
     */
    @PutMapping("/imagenes/{imagenId}/principal")
    public ResponseEntity<Map<String, Object>> establecerImagenPrincipal(
            @PathVariable Long imagenId) {
        try {
            productService.establecerImagenPrincipal(imagenId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagen principal actualizada");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error al establecer imagen principal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse("Error al establecer imagen principal"));
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