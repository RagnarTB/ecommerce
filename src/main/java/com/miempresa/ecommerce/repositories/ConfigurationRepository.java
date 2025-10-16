package com.miempresa.ecommerce.repositories;

import com.miempresa.ecommerce.models.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: CONFIGURACIÓN
 * 
 * Interface para acceder a la tabla 'configuraciones'.
 */

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    /**
     * Busca una configuración por su clave
     */
    Optional<Configuration> findByClave(String clave);

    /**
     * Busca configuraciones por categoría
     */
    List<Configuration> findByCategoria(String categoria);

    /**
     * Busca configuraciones por tipo
     */
    List<Configuration> findByTipo(String tipo);

    /**
     * Verifica si existe una configuración con esa clave
     */
    boolean existsByClave(String clave);

    /**
     * Busca todas las configuraciones ordenadas por categoría
     */
    List<Configuration> findAllByOrderByCategoriaAscClaveAsc();
}