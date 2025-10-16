package com.miempresa.ecommerce.controllers.admin;

import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.services.CategoryService;
import com.miempresa.ecommerce.services.BrandService;
import com.miempresa.ecommerce.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Optional;

/**
 * CONTROLLER: PRODUCTOS
 * 
 * Gestiona el CRUD de productos.
 * CRUD = Create, Read, Update, Delete
 */

@Controller
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    // ========================================
    // LISTAR PRODUCTOS
    // ========================================

    /**
     * Lista todos los productos
     * 
     * URL: GET /admin/productos
     * Vista: admin/productos/lista.html
     */
    @GetMapping
    public String listar(Model model) {
        log.debug("Listando productos");

        model.addAttribute("productos", productService.obtenerActivos());
        model.addAttribute("titulo", "Gestión de Productos");

        return "admin/productos/lista";
    }

    // ========================================
    // CREAR PRODUCTO
    // ========================================

    /**
     * Muestra el formulario para crear un producto
     * 
     * URL: GET /admin/productos/nuevo
     * Vista: admin/productos/form.html
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        log.debug("Mostrando formulario de nuevo producto");

        // Crear producto vacío
        model.addAttribute("producto", new Product());

        // Cargar categorías y marcas para los selectores
        model.addAttribute("categorias", categoryService.obtenerActivas());
        model.addAttribute("marcas", brandService.obtenerActivas());
        model.addAttribute("titulo", "Nuevo Producto");
        model.addAttribute("esNuevo", true);

        return "admin/productos/form";
    }

    /**
     * Procesa el formulario de creación
     * 
     * URL: POST /admin/productos/guardar
     * Redirige a: /admin/productos
     */
    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("producto") Product producto,
            BindingResult result,
            @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Guardando producto: {}", producto.getNombre());

        // Validar errores del formulario
        if (result.hasErrors()) {
            log.warn("Errores de validación en el formulario");
            model.addAttribute("categorias", categoryService.obtenerActivas());
            model.addAttribute("marcas", brandService.obtenerActivas());
            return "admin/productos/form";
        }

        try {
            // Guardar producto
            Product productoGuardado = productService.guardar(producto);

            // Subir imágenes si las hay
            if (imagenes != null && imagenes.length > 0) {
                for (int i = 0; i < imagenes.length && i < 5; i++) {
                    if (!imagenes[i].isEmpty()) {
                        boolean esPrincipal = (i == 0); // Primera imagen es principal
                        productService.subirImagen(productoGuardado.getId(), imagenes[i], esPrincipal);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success",
                    "Producto creado correctamente");
            log.info("Producto guardado con ID: {}", productoGuardado.getId());

        } catch (Exception e) {
            log.error("Error al guardar producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Error al guardar el producto: " + e.getMessage());
        }

        return "redirect:/admin/productos";
    }

    // ========================================
    // EDITAR PRODUCTO
    // ========================================

    /**
     * Muestra el formulario para editar un producto
     * 
     * URL: GET /admin/productos/editar/{id}
     * Vista: admin/productos/form.html
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        log.debug("Mostrando formulario de edición para producto ID: {}", id);

        Optional<Product> productoOpt = productService.buscarPorId(id);

        if (productoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/admin/productos";
        }

        model.addAttribute("producto", productoOpt.get());
        model.addAttribute("categorias", categoryService.obtenerActivas());
        model.addAttribute("marcas", brandService.obtenerActivas());
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("esNuevo", false);

        return "admin/productos/form";
    }

    /**
     * Procesa el formulario de edición
     * 
     * URL: POST /admin/productos/actualizar/{id}
     */
    @PostMapping("/actualizar/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("producto") Product producto,
            BindingResult result,
            @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Actualizando producto ID: {}", id);

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoryService.obtenerActivas());
            model.addAttribute("marcas", brandService.obtenerActivas());
            return "admin/productos/form";
        }

        try {
            productService.actualizar(id, producto);

            // Subir nuevas imágenes si las hay
            if (imagenes != null && imagenes.length > 0) {
                for (MultipartFile imagen : imagenes) {
                    if (!imagen.isEmpty()) {
                        productService.subirImagen(id, imagen, false);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success",
                    "Producto actualizado correctamente");
            log.info("Producto actualizado: {}", id);

        } catch (Exception e) {
            log.error("Error al actualizar producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Error al actualizar el producto: " + e.getMessage());
        }

        return "redirect:/admin/productos";
    }

    // ========================================
    // ELIMINAR PRODUCTO
    // ========================================

    /**
     * Elimina (desactiva) un producto
     * 
     * URL: GET /admin/productos/eliminar/{id}
     */
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Eliminando producto ID: {}", id);

        try {
            productService.eliminar(id);
            redirectAttributes.addFlashAttribute("success",
                    "Producto eliminado correctamente");

        } catch (Exception e) {
            log.error("Error al eliminar producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Error al eliminar el producto: " + e.getMessage());
        }

        return "redirect:/admin/productos";
    }

    // ========================================
    // VER DETALLE
    // ========================================

    /**
     * Muestra el detalle de un producto
     * 
     * URL: GET /admin/productos/ver/{id}
     * Vista: admin/productos/detalle.html
     */
    @GetMapping("/ver/{id}")
    public String verDetalle(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        log.debug("Mostrando detalle de producto ID: {}", id);

        Optional<Product> productoOpt = productService.buscarPorId(id);

        if (productoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/admin/productos";
        }

        model.addAttribute("producto", productoOpt.get());
        model.addAttribute("titulo", "Detalle del Producto");

        return "admin/productos/detalle";
    }
}

/**
 * EXPLICACIÓN DETALLADA:
 * 
 * 1. Anotaciones de mapeo:
 * 
 * @GetMapping - Peticiones GET (ver/obtener)
 * @PostMapping - Peticiones POST (crear/enviar)
 * @PathVariable - Variable en la URL (/editar/{id})
 * @RequestParam - Parámetro de la URL (?nombre=valor)
 * @ModelAttribute - Objeto del formulario
 * 
 *                 2. ¿Qué es @Valid y BindingResult?
 * @Valid - Valida el objeto según las anotaciones en la entidad
 *        BindingResult - Contiene los errores de validación
 * 
 *        En Product:
 * @NotBlank(message = "El nombre es obligatorio")
 *                   private String nombre;
 * 
 *                   Si nombre está vacío:
 *                   result.hasErrors() = true
 *                   Se retorna al formulario con los errores
 * 
 *                   3. ¿Qué es RedirectAttributes?
 *                   - Pasa mensajes entre redirecciones
 *                   - addFlashAttribute() - disponible en la siguiente petición
 * 
 *                   Ejemplo:
 *                   redirectAttributes.addFlashAttribute("success", "Guardado
 *                   OK");
 *                   return "redirect:/admin/productos";
 * 
 *                   En lista.html:
 *                   <div th:if="${success}" th:text="${success}"></div>
 * 
 *                   4. Flujo completo de creación:
 * 
 *                   1. Usuario hace clic en "Nuevo Producto"
 *                   2. GET /admin/productos/nuevo
 *                   3. Controller muestra formulario vacío
 *                   4. Usuario completa formulario y envía
 *                   5. POST /admin/productos/guardar
 *                   6. Controller valida datos
 *                   7. Si hay errores → vuelve al formulario
 *                   8. Si todo OK → guarda en BD
 *                   9. Sube imágenes si las hay
 *                   10. Redirige a lista con mensaje de éxito
 * 
 *                   5. ¿Qué es MultipartFile?
 *                   - Representa un archivo subido
 *                   - imagenes[0] = primera imagen
 *                   - imagen.isEmpty() = verifica si hay archivo
 *                   - Se pasa a productService.subirImagen()
 * 
 *                   6. Diferencia entre retornar vista vs redirect:
 * 
 *                   return "admin/productos/form";
 *                   - Muestra la vista directamente
 *                   - Mantiene los datos en el model
 *                   - URL no cambia
 * 
 *                   return "redirect:/admin/productos";
 *                   - Hace una redirección HTTP
 *                   - Nueva petición GET
 *                   - URL cambia
 *                   - Se pierde el model (usar RedirectAttributes)
 */