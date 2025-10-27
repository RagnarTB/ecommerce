package com.miempresa.ecommerce.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miempresa.ecommerce.models.Brand;
import com.miempresa.ecommerce.repositories.BrandRepository;
import com.miempresa.ecommerce.repositories.ProductRepository; // <<--- AÑADIR import

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository; // <<--- INYECTAR ProductRepository

    public Brand guardar(Brand brand) {
        log.info("Guardando marca: {}", brand.getNombre());

        // Validar nombre único al crear
        if (brand.getId() == null && brandRepository.existsByNombre(brand.getNombre())) {
            throw new RuntimeException("Ya existe una marca con ese nombre");
        }

        // Asegurar estado activo por defecto si es nueva
        if (brand.getId() == null && brand.getActivo() == null) {
            brand.setActivo(true);
        }

        return brandRepository.save(brand);
    }

    @Transactional(readOnly = true)
    public Optional<Brand> buscarPorId(Long id) {
        return brandRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Brand> obtenerTodas() {
        // Podrías ordenar por nombre aquí si quieres: return
        // brandRepository.findAllByOrderByNombreAsc();
        return brandRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Brand> obtenerActivas() {
        // Podrías ordenar por nombre aquí: return
        // brandRepository.findByActivoTrueOrderByNombreAsc();
        return brandRepository.findByActivoTrue();
    }

    public Brand actualizar(Long id, Brand brandActualizada) {
        Brand brand = brandRepository.findById(id) // Buscar original
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + id));

        // Validar nombre único si cambió
        if (!brand.getNombre().equalsIgnoreCase(brandActualizada.getNombre()) &&
                brandRepository.existsByNombre(brandActualizada.getNombre())) {
            throw new RuntimeException("Ya existe otra marca con el nombre: " + brandActualizada.getNombre());
        }

        brand.setNombre(brandActualizada.getNombre());
        brand.setDescripcion(brandActualizada.getDescripcion());
        // El estado 'activo' se maneja en cambiarEstado
        // brand.setActivo(brandActualizada.getActivo()); // No actualizar aquí

        log.info("Actualizando marca ID: {}", id);
        return brandRepository.save(brand);
    }

    // Corregido: Añadir validación de productos asociados al desactivar
    public Brand cambiarEstado(Long id, boolean activo) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + id));

        // Validar si se intenta DESACTIVAR y tiene productos activos
        if (!activo && brand.getActivo()) { // Solo validar si realmente se está desactivando
            log.info("Intentando desactivar marca ID: {}", id);
            long productCount = productRepository.countByMarcaIdAndActivoTrue(id); // <-- Usa método del repo de
                                                                                   // Productos
            if (productCount > 0) {
                log.warn("Intento de desactivar marca ID {} fallido: tiene {} productos activos.", id, productCount);
                throw new RuntimeException("No se puede desactivar la marca porque tiene " + productCount
                        + " productos activos asociados.");
            }
            log.info("Desactivando marca ID: {}", id);
        } else if (activo && !brand.getActivo()) {
            log.info("Activando marca ID: {}", id);
        } else {
            log.info("Estado de marca ID {} no cambió (ya estaba en {})", id, activo ? "activo" : "inactivo");
            return brand; // No guardar si no hay cambio
        }

        brand.setActivo(activo);
        return brandRepository.save(brand);
    }

    // La eliminación ahora es lógica a través de cambiarEstado
    public void eliminar(Long id) {
        log.warn("Llamando a eliminación lógica (cambiarEstado a false) para marca ID: {}", id);
        cambiarEstado(id, false); // Llama al método con la validación
    }

    @Transactional(readOnly = true)
    public long contarActivas() {
        return brandRepository.countByActivoTrue();
    }

    // Añadir método para validar nombre único (usado internamente y quizás en
    // controlador)
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return brandRepository.existsByNombre(nombre);
    }
}