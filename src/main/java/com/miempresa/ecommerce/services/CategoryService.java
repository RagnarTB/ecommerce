package com.miempresa.ecommerce.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miempresa.ecommerce.models.Category;
import com.miempresa.ecommerce.repositories.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SERVICE: CATEGORÍA
 * 
 * Gestiona las categorías de productos.
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category guardar(Category category) {
        log.info("Guardando categoría: {}", category.getNombre());

        if (category.getId() == null && categoryRepository.existsByNombre(category.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

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

    @Transactional(readOnly = true)
    public List<Category> obtenerActivas() {
        return categoryRepository.findByActivoTrueOrderByOrdenAsc();
    }

    public Category actualizar(Long id, Category categoryActualizada) {
        Optional<Category> categoryOpt = buscarPorId(id);

        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Categoría no encontrada");
        }

        Category category = categoryOpt.get();
        category.setNombre(categoryActualizada.getNombre());
        category.setDescripcion(categoryActualizada.getDescripcion());
        category.setOrden(categoryActualizada.getOrden());

        return categoryRepository.save(category);
    }

    public Category cambiarEstado(Long id, boolean activo) {
        Optional<Category> categoryOpt = buscarPorId(id);

        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Categoría no encontrada");
        }

        Category category = categoryOpt.get();
        category.setActivo(activo);

        return categoryRepository.save(category);
    }

    public void eliminar(Long id) {
        cambiarEstado(id, false);
    }

    @Transactional(readOnly = true)
    public long contarActivas() {
        return categoryRepository.countByActivoTrue();
    }
}
