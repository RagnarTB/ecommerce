package com.miempresa.ecommerce.repositories;

import com.miempresa.ecommerce.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: CATEGORÍA
 * 
 * Interface para acceder a la tabla 'categorias'.
 */

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Busca una categoría por su nombre
     */
    Optional<Category> findByNombre(String nombre);

    /**
     * Busca categorías activas ordenadas por 'orden'
     */
    List<Category> findByActivoTrueOrderByOrdenAsc();

    /**
     * Busca todas las categorías ordenadas
     */
    List<Category> findAllByOrderByOrdenAsc();

    /**
     * Verifica si existe una categoría con ese nombre
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca categorías cuyo nombre contenga un texto
     */
    List<Category> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Cuenta categorías activas
     */
    long countByActivoTrue();
}