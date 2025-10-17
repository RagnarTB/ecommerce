package com.miempresa.ecommerce.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miempresa.ecommerce.models.Customer;
import com.miempresa.ecommerce.models.enums.TipoDocumento;

/**
 * REPOSITORY: CLIENTE
 * 
 * Interface para acceder a la tabla 'clientes'.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

        /**
         * Busca un cliente por su número de documento.
         * IMPORTANTE: Primero buscar aquí antes de llamar a la API externa.
         */
        Optional<Customer> findByNumeroDocumento(String numeroDocumento);

        /**
         * Busca clientes por tipo de documento.
         */
        List<Customer> findByTipoDocumento(TipoDocumento tipoDocumento);

        /**
         * Busca clientes activos.
         */
        List<Customer> findByActivoTrue();

        /**
         * Verifica si existe un cliente con ese número de documento.
         */
        boolean existsByNumeroDocumento(String numeroDocumento);

        /**
         * Busca clientes cuyo nombre completo contenga un texto.
         * Busca en nombres, apellidos o razón social.
         */
        @Query("""
                        SELECT c FROM Customer c
                        WHERE LOWER(c.nombres) LIKE LOWER(CONCAT('%', :texto, '%'))
                           OR LOWER(c.apellidoPaterno) LIKE LOWER(CONCAT('%', :texto, '%'))
                           OR LOWER(c.apellidoMaterno) LIKE LOWER(CONCAT('%', :texto, '%'))
                           OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :texto, '%'))
                        """)
        List<Customer> buscarPorNombre(@Param("texto") String texto);

        /**
         * Busca clientes por documento o nombre.
         */
        @Query("""
                        SELECT c FROM Customer c
                        WHERE c.numeroDocumento LIKE CONCAT('%', :busqueda, '%')
                           OR LOWER(c.nombres) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                           OR LOWER(c.apellidoPaterno) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                           OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                        """)
        List<Customer> buscarPorDocumentoONombre(@Param("busqueda") String busqueda);

        /**
         * Obtiene los últimos clientes registrados.
         */
        List<Customer> findTop10ByOrderByFechaRegistroDesc();

        /**
         * Cuenta clientes activos.
         */
        long countByActivoTrue();
}
