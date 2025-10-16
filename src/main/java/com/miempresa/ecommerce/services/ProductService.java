package com.miempresa.ecommerce.services;

import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.models.ProductImage;
import com.miempresa.ecommerce.repositories.ProductImageRepository;
import com.miempresa.ecommerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SERVICE: PRODUCTO
 * 
 * Gestiona productos con sus imágenes y stock.
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    // Directorio donde se guardan las imágenes
    private static final String UPLOAD_DIR = "uploads/productos/";

    // ========================================
    // OPERACIONES CRUD
    // ========================================

    public Product guardar(Product product) {
        log.info("Guardando producto: {}", product.getNombre());

        // Validar SKU único
        if (product.getId() == null && product.getCodigoSku() != null) {
            if (productRepository.existsByCodigoSku(product.getCodigoSku())) {
                throw new RuntimeException("Ya existe un producto con ese código SKU");
            }
        }

        // Generar SKU si no existe
        if (product.getCodigoSku() == null) {
            product.setCodigoSku(generarSKU());
        }

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Optional<Product> buscarPorId(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> obtenerTodos() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> obtenerActivos() {
        return productRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> obtenerDestacados() {
        return productRepository.findByEsDestacadoTrueAndActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> buscarPorNombre(String nombre) {
        return productRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    @Transactional(readOnly = true)
    public List<Product> buscarPorCategoria(Long categoriaId) {
        return productRepository.findByCategoriaIdAndActivoTrue(categoriaId);
    }

    @Transactional(readOnly = true)
    public List<Product> buscarPorMarca(Long marcaId) {
        return productRepository.findByMarcaIdAndActivoTrue(marcaId);
    }

    public Product actualizar(Long id, Product productActualizado) {
        Optional<Product> productOpt = buscarPorId(id);

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Product product = productOpt.get();
        product.setNombre(productActualizado.getNombre());
        product.setDescripcion(productActualizado.getDescripcion());
        product.setPrecioBase(productActualizado.getPrecioBase());
        product.setPrecioOferta(productActualizado.getPrecioOferta());
        product.setStockMinimo(productActualizado.getStockMinimo());
        product.setCategoria(productActualizado.getCategoria());
        product.setMarca(productActualizado.getMarca());
        product.setEsDestacado(productActualizado.getEsDestacado());

        return productRepository.save(product);
    }

    public Product cambiarEstado(Long id, boolean activo) {
        Optional<Product> productOpt = buscarPorId(id);

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Product product = productOpt.get();
        product.setActivo(activo);

        return productRepository.save(product);
    }

    public void eliminar(Long id) {
        cambiarEstado(id, false);
    }

    // ========================================
    // GESTIÓN DE STOCK
    // ========================================

    public Product aumentarStock(Long id, Integer cantidad) {
        log.info("Aumentando stock del producto ID: {} en {} unidades", id, cantidad);

        Optional<Product> productOpt = buscarPorId(id);

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Product product = productOpt.get();
        product.aumentarStock(cantidad);

        return productRepository.save(product);
    }

    public Product disminuirStock(Long id, Integer cantidad) {
        log.info("Disminuyendo stock del producto ID: {} en {} unidades", id, cantidad);

        Optional<Product> productOpt = buscarPorId(id);

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Product product = productOpt.get();

        if (!product.disminuirStock(cantidad)) {
            throw new RuntimeException("Stock insuficiente");
        }

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> obtenerProductosStockBajo() {
        return productRepository.obtenerProductosStockBajo();
    }

    @Transactional(readOnly = true)
    public List<Product> obtenerProductosSinStock() {
        return productRepository.obtenerProductosSinStock();
    }

    // ========================================
    // BÚSQUEDAS AVANZADAS
    // ========================================

    @Transactional(readOnly = true)
    public List<Product> buscarConFiltros(String nombre, Long categoriaId, Long marcaId,
            BigDecimal precioMin, BigDecimal precioMax) {
        return productRepository.buscarConFiltros(nombre, categoriaId, marcaId, precioMin, precioMax);
    }

    @Transactional(readOnly = true)
    public List<Product> obtenerProductosConOferta() {
        return productRepository.obtenerProductosConOferta();
    }

    // ========================================
    // GESTIÓN DE IMÁGENES
    // ========================================

    /**
     * Sube una imagen para un producto
     */
    public ProductImage subirImagen(Long productoId, MultipartFile file, boolean esPrincipal) {
        log.info("Subiendo imagen para producto ID: {}", productoId);

        Optional<Product> productOpt = buscarPorId(productoId);

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Product product = productOpt.get();

        // Validar que no tenga más de 5 imágenes
        if (product.getImagenes().size() >= 5) {
            throw new RuntimeException("El producto ya tiene el máximo de 5 imágenes");
        }

        try {
            // Guardar archivo físico
            String nombreArchivo = guardarArchivo(file);

            // Si es principal, desmarcar otras
            if (esPrincipal) {
                product.getImagenes().forEach(img -> img.setEsPrincipal(false));
            }

            // Si no hay imágenes, esta será la principal
            boolean seraPrincipal = esPrincipal || product.getImagenes().isEmpty();

            // Crear registro de imagen
            ProductImage imagen = ProductImage.builder()
                    .producto(product)
                    .url("productos/" + nombreArchivo)
                    .orden(product.getImagenes().size() + 1)
                    .esPrincipal(seraPrincipal)
                    .build();

            product.agregarImagen(imagen);
            productRepository.save(product);

            log.info("Imagen subida exitosamente: {}", nombreArchivo);
            return imagen;

        } catch (IOException e) {
            log.error("Error al subir imagen: {}", e.getMessage());
            throw new RuntimeException("Error al subir imagen: " + e.getMessage());
        }
    }

    /**
     * Elimina una imagen de un producto
     */
    public void eliminarImagen(Long imagenId) {
        log.info("Eliminando imagen ID: {}", imagenId);

        Optional<ProductImage> imagenOpt = productImageRepository.findById(imagenId);

        if (imagenOpt.isEmpty()) {
            throw new RuntimeException("Imagen no encontrada");
        }

        ProductImage imagen = imagenOpt.get();

        // Eliminar archivo físico
        eliminarArchivo(imagen.getUrl());

        // Eliminar registro
        productImageRepository.delete(imagen);
    }

    /**
     * Establece una imagen como principal
     */
    public void establecerImagenPrincipal(Long imagenId) {
        Optional<ProductImage> imagenOpt = productImageRepository.findById(imagenId);

        if (imagenOpt.isEmpty()) {
            throw new RuntimeException("Imagen no encontrada");
        }

        ProductImage imagen = imagenOpt.get();
        Product product = imagen.getProducto();

        // Desmarcar todas como principal
        product.getImagenes().forEach(img -> img.setEsPrincipal(false));

        // Marcar esta como principal
        imagen.setEsPrincipal(true);

        productRepository.save(product);
    }

    // ========================================
    // UTILIDADES PRIVADAS
    // ========================================

    /**
     * Guarda un archivo en el servidor
     */
    private String guardarArchivo(MultipartFile file) throws IOException {
        // Crear directorio si no existe
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único
        String extension = obtenerExtension(file.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path filePath = uploadPath.resolve(nombreArchivo);
        Files.copy(file.getInputStream(), filePath);

        return nombreArchivo;
    }

    /**
     * Elimina un archivo del servidor
     */
    private void eliminarArchivo(String url) {
        try {
            // Extraer nombre del archivo de la URL
            String nombreArchivo = url.substring(url.lastIndexOf("/") + 1);
            Path filePath = Paths.get(UPLOAD_DIR + nombreArchivo);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Archivo eliminado: {}", nombreArchivo);
            }
        } catch (IOException e) {
            log.error("Error al eliminar archivo: {}", e.getMessage());
        }
    }

    /**
     * Obtiene la extensión de un archivo
     */
    private String obtenerExtension(String filename) {
        if (filename == null)
            return "";
        int lastDot = filename.lastIndexOf(".");
        return (lastDot == -1) ? "" : filename.substring(lastDot);
    }

    /**
     * Genera un código SKU único
     */
    private String generarSKU() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return productRepository.countByActivoTrue();
    }
}

/**
 * EXPLICACIÓN ADICIONAL:
 * 
 * 1. Gestión de archivos:
 * - MultipartFile: archivo subido desde formulario
 * - Se guarda en /uploads/productos/
 * - Nombre único con UUID para evitar duplicados
 * 
 * 2. Máximo 5 imágenes:
 * - Validación antes de subir
 * - Primera imagen es automáticamente principal
 * 
 * 3. Imagen principal:
 * - Solo puede haber UNA principal
 * - Al marcar una nueva, las demás se desmarcan
 * 
 * 4. Stock:
 * - aumentarStock(): para compras/entradas
 * - disminuirStock(): para ventas/salidas
 * - Valida que no quede negativo
 */