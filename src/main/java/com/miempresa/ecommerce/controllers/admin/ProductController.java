package com.miempresa.ecommerce.controllers.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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

@Controller
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    private static final int MAX_IMAGENES = 5;
    private static final long MAX_SIZE_MB = 5;
    private static final List<String> EXTENSIONES_PERMITIDAS = List.of("jpg", "jpeg", "png", "gif", "webp");

    // ========================================
    // ✅ CRÍTICO: Evitar binding del campo 'imagenes'
    // ========================================
    @InitBinder("producto")
    public void initBinder(WebDataBinder binder) {
        // Evita que Spring intente bindear el campo 'imagenes'
        // porque lo manejamos por separado con MultipartFile[]
        binder.setDisallowedFields("imagenes");
    }

    // ========================================
    // LISTAR PRODUCTOS
    // ========================================

    @GetMapping
    public String listar(Model model) {
        log.debug("Listando productos");
        model.addAttribute("productos", productService.obtenerTodos());
        model.addAttribute("titulo", "Gestión de Productos");
        return "admin/productos/lista";
    }

    // ========================================
    // CREAR PRODUCTO
    // ========================================

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        log.debug("Mostrando formulario de nuevo producto");

        Product producto = new Product();
        producto.setActivo(true);
        producto.setStockMinimo(10);

        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoryService.obtenerActivas());
        model.addAttribute("marcas", brandService.obtenerActivas());
        model.addAttribute("titulo", "Nuevo Producto");
        model.addAttribute("esNuevo", true);

        return "admin/productos/form";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("producto") Product producto,
            BindingResult result,
            @RequestParam(value = "imagenesFile", required = false) MultipartFile[] imagenesFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Guardando producto: {}", producto.getNombre());

        if (result.hasErrors()) {
            log.warn("Errores de validación: {}", result.getAllErrors());
            cargarDatosFormulario(model);
            model.addAttribute("esNuevo", true);
            return "admin/productos/form";
        }

        try {
            // Validar imágenes
            if (imagenesFile != null && imagenesFile.length > 0) {
                String errorValidacion = validarImagenes(imagenesFile);
                if (errorValidacion != null) {
                    model.addAttribute("error", errorValidacion);
                    cargarDatosFormulario(model);
                    model.addAttribute("esNuevo", true);
                    return "admin/productos/form";
                }
            }

            // Guardar producto
            Product productoGuardado = productService.guardar(producto);
            log.info("Producto guardado con ID: {}", productoGuardado.getId());

            // Subir imágenes
            if (imagenesFile != null && imagenesFile.length > 0) {
                int imagenesSubidas = subirImagenesProducto(productoGuardado.getId(), imagenesFile);
                log.info("Se subieron {} imágenes correctamente", imagenesSubidas);
            }

            redirectAttributes.addFlashAttribute("success", "Producto creado correctamente");

        } catch (RuntimeException e) {
            log.error("Error al guardar producto: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            cargarDatosFormulario(model);
            model.addAttribute("esNuevo", true);
            return "admin/productos/form";

        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al guardar el producto");
            cargarDatosFormulario(model);
            model.addAttribute("esNuevo", true);
            return "admin/productos/form";
        }

        return "redirect:/admin/productos";
    }

    // ========================================
    // EDITAR PRODUCTO
    // ========================================

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

    @PostMapping("/actualizar/{id}")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("producto") Product producto,
            BindingResult result,
            @RequestParam(value = "imagenesFile", required = false) MultipartFile[] imagenesFile,
            @RequestParam(value = "imagenesAEliminar", required = false) String imagenesAEliminar, // <-- Nuevo
                                                                                                   // parámetro
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Actualizando producto ID: {}", id);
        log.info("IDs de imágenes a eliminar: {}", imagenesAEliminar); // Log para depurar

        // ... (validación de errores de BindingResult como antes) ...
        if (result.hasErrors()) {
            // ... (código existente para manejar errores de validación) ...
            cargarDatosFormulario(model); // Asegúrate de que esta línea esté presente
            model.addAttribute("esNuevo", false);
            // Recargar producto original para mostrar imágenes actuales
            productService.buscarPorId(id).ifPresent(p -> model.addAttribute("producto", p));
            return "admin/productos/form";
        }

        try {
            // 1. Eliminar imágenes marcadas (ANTES de actualizar y subir nuevas)
            if (imagenesAEliminar != null && !imagenesAEliminar.isEmpty()) {
                productService.eliminarImagenesPorIds(imagenesAEliminar); // Llamar al nuevo método del servicio
            }

            // 2. Validar nuevas imágenes
            if (imagenesFile != null && imagenesFile.length > 0 && !imagenesFile[0].isEmpty()) { // Verificar que no
                                                                                                 // esté vacío
                String errorValidacion = validarImagenes(imagenesFile);
                if (errorValidacion != null) {
                    model.addAttribute("error", errorValidacion);
                    // Recargar datos y producto original
                    Optional<Product> productoOpt = productService.buscarPorId(id);
                    productoOpt.ifPresent(p -> model.addAttribute("producto", p));
                    cargarDatosFormulario(model);
                    model.addAttribute("esNuevo", false);
                    return "admin/productos/form";
                }
            }

            // 3. Actualizar datos del producto
            Product productoActualizado = productService.actualizar(id, producto);
            log.info("Producto actualizado: {}", id);

            // 4. Subir nuevas imágenes (si las hay)
            if (imagenesFile != null && imagenesFile.length > 0 && !imagenesFile[0].isEmpty()) { // Verificar que no
                                                                                                 // esté vacío
                int imagenesSubidas = subirImagenesProducto(id, imagenesFile);
                log.info("Se subieron {} imágenes nuevas", imagenesSubidas);
            }

            redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente");

        } catch (RuntimeException e) {
            log.error("Error al actualizar: {}", e.getMessage(), e); // Añadir stack trace al log
            model.addAttribute("error", e.getMessage());
            // Recargar datos y producto original
            Optional<Product> productoOpt = productService.buscarPorId(id);
            productoOpt.ifPresent(p -> model.addAttribute("producto", p));
            cargarDatosFormulario(model);
            model.addAttribute("esNuevo", false);
            return "admin/productos/form";

        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al actualizar el producto: " + e.getMessage()); // Mostrar mensaje más
                                                                                               // específico
            // Recargar datos y producto original
            Optional<Product> productoOpt = productService.buscarPorId(id);
            productoOpt.ifPresent(p -> model.addAttribute("producto", p));
            cargarDatosFormulario(model);
            model.addAttribute("esNuevo", false);
            return "admin/productos/form";
        }

        return "redirect:/admin/productos";
    }

    // ========================================
    // OTROS MÉTODOS (sin cambios)
    // ========================================

    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo,
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

            String mensaje = activo ? "Producto activado" : "Producto desactivado";
            redirectAttributes.addFlashAttribute("success", mensaje);

        } catch (Exception e) {
            log.error("Error al cambiar estado: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al cambiar el estado");
        }

        return "redirect:/admin/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Eliminando producto ID: {}", id);

        try {
            productService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente");
        } catch (Exception e) {
            log.error("Error al eliminar: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto");
        }

        return "redirect:/admin/productos";
    }

    @GetMapping("/ver/{id}")
    public String verDetalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
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
    // MÉTODOS AUXILIARES
    // ========================================

    private void cargarDatosFormulario(Model model) {
        model.addAttribute("categorias", categoryService.obtenerActivas());
        model.addAttribute("marcas", brandService.obtenerActivas());
    }

    private String validarImagenes(MultipartFile[] imagenes) {
        if (imagenes.length > MAX_IMAGENES) {
            return "Solo se permiten máximo " + MAX_IMAGENES + " imágenes";
        }

        for (MultipartFile imagen : imagenes) {
            if (!imagen.isEmpty()) {
                if (!FileUploadUtil.isImageFile(imagen)) {
                    return "Solo se permiten archivos de imagen (JPG, PNG, GIF, WEBP)";
                }

                if (!FileUploadUtil.isValidFileSize(imagen, MAX_SIZE_MB)) {
                    return "Cada imagen no puede superar " + MAX_SIZE_MB + "MB";
                }

                String filename = imagen.getOriginalFilename();
                if (filename != null) {
                    String extension = obtenerExtension(filename).toLowerCase();
                    if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
                        return "Extensión no permitida. Use: " +
                                String.join(", ", EXTENSIONES_PERMITIDAS).toUpperCase();
                    }
                }
            }
        }

        return null;
    }

    private int subirImagenesProducto(Long productoId, MultipartFile[] imagenes) {
        int imagenesSubidas = 0;

        for (int i = 0; i < imagenes.length && i < MAX_IMAGENES; i++) {
            if (!imagenes[i].isEmpty()) {
                try {
                    boolean esPrincipal = (i == 0);
                    productService.subirImagen(productoId, imagenes[i], esPrincipal);
                    imagenesSubidas++;
                } catch (Exception e) {
                    log.error("Error al subir imagen {}: {}", i, e.getMessage());
                }
            }
        }

        return imagenesSubidas;
    }

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