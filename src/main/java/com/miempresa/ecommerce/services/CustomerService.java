package com.miempresa.ecommerce.services;

import com.miempresa.ecommerce.models.Customer;
import com.miempresa.ecommerce.models.enums.TipoDocumento;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE INTERFACE: CLIENTE
 * 
 * Define operaciones para gestionar clientes.
 * Los clientes NO tienen login, solo se registran al comprar.
 */
public interface CustomerService {

    /**
     * Guarda un cliente
     */
    Customer guardar(Customer customer);

    /**
     * Busca un cliente por ID
     */
    Optional<Customer> buscarPorId(Long id);

    /**
     * Busca un cliente por número de documento
     * IMPORTANTE: Buscar aquí ANTES de llamar a la API
     */
    Optional<Customer> buscarPorDocumento(String numeroDocumento);

    /**
     * Obtiene todos los clientes
     */
    List<Customer> obtenerTodos();

    /**
     * Obtiene clientes activos
     */
    List<Customer> obtenerActivos();

    /**
     * Busca clientes por nombre o razón social
     */
    List<Customer> buscarPorNombre(String nombre);

    /**
     * Busca clientes por documento o nombre
     */
    List<Customer> buscarPorDocumentoONombre(String busqueda);

    /**
     * Verifica si existe un cliente con ese documento
     */
    boolean existePorDocumento(String numeroDocumento);

    /**
     * Obtiene o crea un cliente desde la API Decolecta
     * 
     * Flujo:
     * 1. Busca en BD local
     * 2. Si no existe, llama a API Decolecta
     * 3. Registra el cliente
     * 4. Retorna el cliente
     * 
     * @param numeroDocumento DNI o RUC
     * @return Cliente encontrado o creado
     */
    Customer obtenerOCrearDesdeApi(String numeroDocumento);

    /**
     * Actualiza un cliente
     */
    Customer actualizar(Long id, Customer customerActualizado);

    /**
     * Cambia el estado de un cliente
     */
    Customer cambiarEstado(Long id, boolean activo);

    /**
     * Elimina un cliente (lógicamente)
     */
    void eliminar(Long id);

    /**
     * Cuenta clientes activos
     */
    long contarActivos();
}