package com.miempresa.ecommerce.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.Brand;

/**
 * REPOSITORY: MARCA
 * 
 * Interface para acceder a la tabla 'marcas'.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Busca una marca por su nombre
     */
    Optional<Brand> findByNombre(String nombre);

    /**
     * Busca marcas activas
     */
    List<Brand> findByActivoTrue();

    /**
     * Verifica si existe una marca con ese nombre
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca marcas cuyo nombre contenga un texto
     */
    List<Brand> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Cuenta marcas activas
     */
    long countByActivoTrue();
}
