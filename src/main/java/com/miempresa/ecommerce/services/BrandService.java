package com.miempresa.ecommerce.services;

// ============================================
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miempresa.ecommerce.models.Brand;
import com.miempresa.ecommerce.repositories.BrandRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SERVICE: MARCA
 * 
 * Gestiona las marcas de productos.
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;

    public Brand guardar(Brand brand) {
        log.info("Guardando marca: {}", brand.getNombre());

        if (brand.getId() == null && brandRepository.existsByNombre(brand.getNombre())) {
            throw new RuntimeException("Ya existe una marca con ese nombre");
        }

        return brandRepository.save(brand);
    }

    @Transactional(readOnly = true)
    public Optional<Brand> buscarPorId(Long id) {
        return brandRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Brand> obtenerTodas() {
        return brandRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Brand> obtenerActivas() {
        return brandRepository.findByActivoTrue();
    }

    public Brand actualizar(Long id, Brand brandActualizada) {
        Optional<Brand> brandOpt = buscarPorId(id);

        if (brandOpt.isEmpty()) {
            throw new RuntimeException("Marca no encontrada");
        }

        Brand brand = brandOpt.get();
        brand.setNombre(brandActualizada.getNombre());
        brand.setDescripcion(brandActualizada.getDescripcion());

        return brandRepository.save(brand);
    }

    public Brand cambiarEstado(Long id, boolean activo) {
        Optional<Brand> brandOpt = buscarPorId(id);

        if (brandOpt.isEmpty()) {
            throw new RuntimeException("Marca no encontrada");
        }

        Brand brand = brandOpt.get();
        brand.setActivo(activo);

        return brandRepository.save(brand);
    }

    public void eliminar(Long id) {
        cambiarEstado(id, false);
    }

    @Transactional(readOnly = true)
    public long contarActivas() {
        return brandRepository.countByActivoTrue();
    }
}