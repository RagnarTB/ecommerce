package com.miempresa.ecommerce.controllers.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.services.BrandService;
import com.miempresa.ecommerce.services.CategoryService;
import com.miempresa.ecommerce.services.ProductService;
import com.miempresa.ecommerce.utils.FileUploadUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CONTROLLER: PRODUCTOS
 * 
 * Gestiona el CRUD de productos.
 * Ahora usa FileUploadUtil para mantener coherencia.
 */

@Controller
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    // Configuración de validación de imágenes
    private static final int MAX_IMAGENES = 5;
    private static final long MAX_SIZE_MB = 5;
    private static final List<String> EXTENSIONES_PERMITIDAS = List.of("jpg", "jpeg", "png", "gif", "webp");

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

        model.addAttribute("producto", new Product());
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
            cargarDatosFormulario(model);
            model.addAttribute("esNuevo", true);
            return "admin/productos/form";
        }

        try {
            // ========================================
            // ✅ VALIDAR IMÁGENES usando FileUploadUtil
            // ========================================

            if (imagenes != null && imagenes.length > 0) {
                String errorValidacion = validarImagenes(imagenes);

                if (errorValidacion != null) {
                    model.addAttribute("error", errorValidacion);
                    cargarDatosFormulario(model);
                    model.addAttribute("esNuevo", true);
                    return "admin/productos/form";
                }
            }

            // ========================================
            // GUARDAR PRODUCTO
            // ========================================

            Product productoGuardado = productService.guardar(producto);

            // ========================================
            // SUBIR IMÁGENES
            // ========================================

            if (imagenes != null && imagenes.length > 0) {
                int imagenesSubidas = subirImagenesProducto(productoGuardado.getId(), imagenes);

                if (imagenesSubidas > 0) {
                    log.info("Se subieron {} imágenes correctamente", imagenesSubidas);
                }
            }

            redirectAttributes.addFlashAttribute("success", "Producto creado correctamente");
            log.info("Producto guardado con ID: {}", productoGuardado.getId());

        } catch (RuntimeException e) {
            log.error("Error de negocio al guardar producto: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al guardar producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al guardar el producto. Intente nuevamente.");
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
        cargarDatosFormulario(model);
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
            cargarDatosFormulario(model);
            return "admin/productos/form";
        }

        try {
            // ✅ Validar imágenes si hay
            if (imagenes != null && imagenes.length > 0) {
                String errorValidacion = validarImagenes(imagenes);

                if (errorValidacion != null) {
                    model.addAttribute("error", errorValidacion);
                    cargarDatosFormulario(model);
                    return "admin/productos/form";
                }
            }

            // Actualizar producto
            productService.actualizar(id, producto);

            // Subir nuevas imágenes si las hay
            if (imagenes != null && imagenes.length > 0) {
                int imagenesSubidas = subirImagenesProducto(id, imagenes);
                log.info("Se actualizaron {} imágenes", imagenesSubidas);
            }

            redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente");
            log.info("Producto actualizado: {}", id);

        } catch (Exception e) {
            log.error("Error al actualizar producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el producto: " + e.getMessage());
        }

        return "redirect:/admin/productos";
    }

    // ========================================
    // CAMBIAR ESTADO PRODUCTO
    // ========================================

    /**
     * Cambia el estado de un producto (activo/inactivo)
     * 
     * URL: GET /admin/productos/cambiar-estado/{id}
     */
    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo,
            RedirectAttributes redirectAttributes) {

        log.info("Cambiando estado de producto ID: {} a {}", id, activo ? "ACTIVO" : "INACTIVO");

        try {
            Optional<Product> productoOpt = productService.buscarPorId(id);

            if (productoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/admin/productos";
            }

            Product producto = productoOpt.get();
            producto.setActivo(activo);
            productService.actualizar(id, producto);

            String mensaje = activo ? "Producto activado correctamente" : "Producto desactivado correctamente";
            redirectAttributes.addFlashAttribute("success", mensaje);

            log.info("Estado actualizado exitosamente para producto ID: {}", id);

        } catch (Exception e) {
            log.error("Error al cambiar estado del producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al cambiar el estado: " + e.getMessage());
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
            redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente");

        } catch (Exception e) {
            log.error("Error al eliminar producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto: " + e.getMessage());
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

    // ========================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ========================================

    /**
     * Carga los datos necesarios para el formulario
     */
    private void cargarDatosFormulario(Model model) {
        model.addAttribute("categorias", categoryService.obtenerActivas());
        model.addAttribute("marcas", brandService.obtenerActivas());
    }

    /**
     * Valida las imágenes usando FileUploadUtil
     * 
     * @param imagenes Array de archivos a validar
     * @return Mensaje de error o null si todo está correcto
     */
    private String validarImagenes(MultipartFile[] imagenes) {
        // Validar cantidad máxima
        if (imagenes.length > MAX_IMAGENES) {
            return "Solo se permiten máximo " + MAX_IMAGENES + " imágenes";
        }

        // Validar cada imagen
        for (MultipartFile imagen : imagenes) {
            if (!imagen.isEmpty()) {
                // ✅ Validar que sea una imagen usando FileUploadUtil
                if (!FileUploadUtil.isImageFile(imagen)) {
                    return "Solo se permiten archivos de imagen (JPG, PNG, GIF, WEBP)";
                }

                // ✅ Validar tamaño usando FileUploadUtil
                if (!FileUploadUtil.isValidFileSize(imagen, MAX_SIZE_MB)) {
                    return "Cada imagen no puede superar " + MAX_SIZE_MB + "MB de tamaño";
                }

                // Validar extensión
                String filename = imagen.getOriginalFilename();
                if (filename != null) {
                    String extension = obtenerExtension(filename).toLowerCase();
                    if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
                        return "Extensión no permitida. Use: "
                                + String.join(", ", EXTENSIONES_PERMITIDAS).toUpperCase();
                    }
                }
            }
        }

        return null; // Todo correcto
    }

    /**
     * Sube las imágenes de un producto
     * 
     * @param productoId ID del producto
     * @param imagenes   Array de imágenes a subir
     * @return Cantidad de imágenes subidas exitosamente
     */
    private int subirImagenesProducto(Long productoId, MultipartFile[] imagenes) {
        int imagenesSubidas = 0;

        for (int i = 0; i < imagenes.length && i < MAX_IMAGENES; i++) {
            if (!imagenes[i].isEmpty()) {
                try {
                    boolean esPrincipal = (i == 0); // Primera imagen es principal
                    productService.subirImagen(productoId, imagenes[i], esPrincipal);
                    imagenesSubidas++;
                } catch (Exception e) {
                    log.error("Error al subir imagen {}: {}", i, e.getMessage());
                    // Continuar con las demás imágenes
                }
            }
        }

        return imagenesSubidas;
    }

    /**
     * Obtiene la extensión de un archivo
     * 
     * @param filename Nombre del archivo
     * @return Extensión sin el punto
     */
    private String obtenerExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }
}

/**
 * MEJORAS IMPLEMENTADAS:
 * 
 * ✅ Usa FileUploadUtil.isImageFile() en lugar de validación manual
 * ✅ Usa FileUploadUtil.isValidFileSize() para validar tamaño
 * ✅ Extraídas validaciones a método privado validarImagenes()
 * ✅ Extraída subida de imágenes a método privado subirImagenesProducto()
 * ✅ Método auxiliar cargarDatosFormulario() para DRY
 * ✅ Constantes configurables al inicio de la clase
 * ✅ Eliminadas 60+ líneas de código duplicado
 * ✅ Más fácil de mantener y testear
 * ✅ Coherencia total con FileUploadUtil
 * 
 * ANTES: 40+ líneas de validación manual duplicadas
 * DESPUÉS: 2 llamadas a FileUploadUtil + validación de extensión
 * 
 * Resultado: Código más limpio, mantenible y coherente
 */