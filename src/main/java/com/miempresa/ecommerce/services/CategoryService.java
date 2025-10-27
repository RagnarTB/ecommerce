package com.miempresa.ecommerce.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miempresa.ecommerce.models.Category;
import com.miempresa.ecommerce.repositories.CategoryRepository;
import com.miempresa.ecommerce.repositories.ProductRepository; // <<--- AÑADIR import

import lombok.RequiredArgsConstructor; // Usar constructor injection
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor // Lombok genera constructor con final fields
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository; // <<--- INYECTAR ProductRepository

    // Corregido: Añadir lógica de orden por defecto
    public Category guardar(Category category) {
        log.info("Guardando categoría: {}", category.getNombre());

        // Validar nombre único al crear
        if (category.getId() == null && categoryRepository.existsByNombre(category.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        // Asignar orden por defecto si es NUEVA y no tiene orden o es <= 0
        if (category.getId() == null && (category.getOrden() == null || category.getOrden() <= 0)) {
            Integer maxOrden = categoryRepository.findMaxOrden().orElse(0); // <-- Usa el nuevo método
            category.setOrden(maxOrden + 1);
            log.info("Asignando orden por defecto {} a la nueva categoría '{}'", category.getOrden(),
                    category.getNombre());
        }

        // Si se especifica un orden al crear o actualizar, podríamos necesitar
        // reordenar otras
        // (Lógica de reordenamiento omitida por complejidad, se puede implementar
        // después si es necesario)

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Optional<Category> buscarPorId(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Category> obtenerTodas() {
        return categoryRepository.findAll();
    }

    // Nuevo/Corregido: Obtener todas ordenadas para la lista
    @Transactional(readOnly = true)
    public List<Category> obtenerTodasOrdenadas() {
        return categoryRepository.findAllByOrderByOrdenAsc();
    }

    @Transactional(readOnly = true)
    public List<Category> obtenerActivas() {
        return categoryRepository.findByActivoTrueOrderByOrdenAsc();
    }

    public Category actualizar(Long id, Category categoryActualizada) {
        Category category = categoryRepository.findById(id) // Buscar original
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // Validar nombre único si cambió
        if (!category.getNombre().equalsIgnoreCase(categoryActualizada.getNombre()) &&
                categoryRepository.existsByNombre(categoryActualizada.getNombre())) {
            throw new RuntimeException("Ya existe otra categoría con el nombre: " + categoryActualizada.getNombre());
        }

        category.setNombre(categoryActualizada.getNombre());
        category.setDescripcion(categoryActualizada.getDescripcion());

        // Solo actualizar orden si es diferente y positivo
        if (categoryActualizada.getOrden() != null && categoryActualizada.getOrden() >= 0 &&
                !categoryActualizada.getOrden().equals(category.getOrden())) {
            log.info("Actualizando orden de categoría ID {} a {}", id, categoryActualizada.getOrden());
            category.setOrden(categoryActualizada.getOrden());
            // Aquí iría la lógica compleja de reordenamiento si fuera necesaria
        }

        // El estado 'activo' se maneja en cambiarEstado
        // category.setActivo(categoryActualizada.getActivo()); // No actualizar aquí

        log.info("Actualizando categoría ID: {}", id);
        return categoryRepository.save(category);
    }

    // Corregido: Añadir validación de productos asociados al desactivar
    public Category cambiarEstado(Long id, boolean activo) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // Validar si se intenta DESACTIVAR y tiene productos activos
        if (!activo && category.getActivo()) { // Solo validar si realmente se está desactivando
            log.info("Intentando desactivar categoría ID: {}", id);
            long productCount = productRepository.countByCategoriaIdAndActivoTrue(id); // <-- Usa el nuevo método del
                                                                                       // repo
            if (productCount > 0) {
                log.warn("Intento de desactivar categoría ID {} fallido: tiene {} productos activos.", id,
                        productCount);
                throw new RuntimeException("No se puede desactivar la categoría porque tiene " + productCount
                        + " productos activos asociados.");
            }
            log.info("Desactivando categoría ID: {}", id);
        } else if (activo && !category.getActivo()) {
            log.info("Activando categoría ID: {}", id);
        } else {
            log.info("Estado de categoría ID {} no cambió (ya estaba en {})", id, activo ? "activo" : "inactivo");
            return category; // No guardar si no hay cambio
        }

        category.setActivo(activo);
        return categoryRepository.save(category);
    }

    // La eliminación ahora es lógica a través de cambiarEstado
    public void eliminar(Long id) {
        log.warn("Llamando a eliminación lógica (cambiarEstado a false) para categoría ID: {}", id);
        cambiarEstado(id, false); // Llama al método con la validación
    }

    @Transactional(readOnly = true)
    public long contarActivas() {
        return categoryRepository.countByActivoTrue();
    }

    // Añadir método para validar nombre único (usado internamente y quizás en
    // controlador)
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return categoryRepository.existsByNombre(nombre);
    }
}
