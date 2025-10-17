package com.miempresa.ecommerce.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.Permission;

/**
 * REPOSITORY: PERMISO
 * 
 * Interface para acceder a la tabla 'permisos'.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Busca un permiso por su código.
     * Ejemplo: "MODULO_PRODUCTOS", "MODULO_VENTAS".
     */
    Optional<Permission> findByCodigo(String codigo);

    /**
     * Busca permisos activos.
     */
    List<Permission> findByActivoTrue();

    /**
     * Busca todos los permisos ordenados por el campo 'orden'.
     */
    List<Permission> findAllByOrderByOrdenAsc();

    /**
     * Verifica si existe un permiso con ese código.
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca permisos activos ordenados por 'orden'.
     */
    List<Permission> findByActivoTrueOrderByOrdenAsc();
}
