package com.miempresa.ecommerce.repositories;

import com.miempresa.ecommerce.models.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: PROVEEDOR
 * 
 * Interface para acceder a la tabla 'proveedores'.
 */

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    /**
     * Busca un proveedor por su RUC
     */
    Optional<Provider> findByRuc(String ruc);

    /**
     * Busca proveedores activos
     */
    List<Provider> findByActivoTrue();

    /**
     * Verifica si existe un proveedor con ese RUC
     */
    boolean existsByRuc(String ruc);

    /**
     * Busca proveedores por razón social o RUC
     */
    @Query("SELECT p FROM Provider p WHERE " +
            "LOWER(p.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "p.ruc LIKE CONCAT('%', :busqueda, '%')")
    List<Provider> buscarPorRazonSocialORuc(@Param("busqueda") String busqueda);

    /**
     * Busca proveedores cuya razón social contenga un texto
     */
    List<Provider> findByRazonSocialContainingIgnoreCase(String razonSocial);

    /**
     * Cuenta proveedores activos
     */
    long countByActivoTrue();

    /**
     * Obtiene los últimos proveedores registrados
     */
    List<Provider> findTop10ByOrderByFechaRegistroDesc();
}