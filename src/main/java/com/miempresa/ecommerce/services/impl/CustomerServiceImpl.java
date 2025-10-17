package com.miempresa.ecommerce.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.miempresa.ecommerce.models.Customer;
import com.miempresa.ecommerce.models.enums.TipoDocumento;
import com.miempresa.ecommerce.repositories.CustomerRepository;
import com.miempresa.ecommerce.services.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SERVICE IMPLEMENTATION: CLIENTE
 * 
 * Integra con API Decolecta para obtener datos de DNI/RUC
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // Configuración de API Decolecta desde application.properties
    @Value("${api.decolecta.token}")
    private String apiToken;

    @Value("${api.decolecta.dni-url}")
    private String dniUrl;

    @Value("${api.decolecta.ruc-url}")
    private String rucUrl;

    @Override
    public Customer guardar(Customer customer) {
        log.info("Guardando cliente: {}", customer.getNumeroDocumento());

        // Validar documento único
        if (customer.getId() == null && existePorDocumento(customer.getNumeroDocumento())) {
            throw new RuntimeException("Ya existe un cliente con ese documento");
        }

        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> buscarPorId(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> buscarPorDocumento(String numeroDocumento) {
        return customerRepository.findByNumeroDocumento(numeroDocumento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> obtenerTodos() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> obtenerActivos() {
        return customerRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> buscarPorNombre(String nombre) {
        return customerRepository.buscarPorNombre(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> buscarPorDocumentoONombre(String busqueda) {
        return customerRepository.buscarPorDocumentoONombre(busqueda);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorDocumento(String numeroDocumento) {
        return customerRepository.existsByNumeroDocumento(numeroDocumento);
    }

    @Override
    public Customer obtenerOCrearDesdeApi(String numeroDocumento) {
        log.info("Obteniendo o creando cliente con documento: {}", numeroDocumento);

        // PASO 1: Buscar en base de datos local
        Optional<Customer> clienteExistente = buscarPorDocumento(numeroDocumento);

        if (clienteExistente.isPresent()) {
            log.info("Cliente encontrado en BD local");
            return clienteExistente.get();
        }

        // PASO 2: No existe, llamar a API Decolecta
        log.info("Cliente no existe, consultando API Decolecta...");

        TipoDocumento tipoDocumento = TipoDocumento.obtenerPorNumero(numeroDocumento);

        if (tipoDocumento == null) {
            throw new RuntimeException("Número de documento inválido");
        }

        Customer nuevoCliente;

        try {
            if (tipoDocumento == TipoDocumento.DNI) {
                nuevoCliente = consultarDNI(numeroDocumento);
            } else {
                nuevoCliente = consultarRUC(numeroDocumento);
            }

            // Guardar en base de datos
            return guardar(nuevoCliente);

        } catch (Exception e) {
            log.error("Error al consultar API Decolecta: {}", e.getMessage());
            throw new RuntimeException("Error al consultar documento: " + e.getMessage());
        }
    }

    private Customer consultarDNI(String dni) {
        log.info("Consultando DNI en API: {}", dni);

        String url = dniUrl + "?numero=" + dni;

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.set("Content-Type", "application/json");

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        try {
            // Hacer petición a la API
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class);

            // ✅ Validar respuesta exitosa
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error al consultar DNI: código " + response.getStatusCode());
            }

            // ✅ Validar que el body no esté vacío
            if (response.getBody() == null || response.getBody().isEmpty()) {
                throw new RuntimeException("La API no devolvió datos");
            }

            // Parsear respuesta JSON
            JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();

            // ✅ Validar que contenga los campos necesarios
            if (!json.has("document_number")) {
                throw new RuntimeException("Respuesta de API incompleta: falta número de documento");
            }
            if (!json.has("first_name")) {
                throw new RuntimeException("Respuesta de API incompleta: falta nombre");
            }
            if (!json.has("first_last_name")) {
                throw new RuntimeException("Respuesta de API incompleta: falta apellido paterno");
            }

            // Crear cliente con los datos
            return Customer.builder()
                    .tipoDocumento(TipoDocumento.DNI)
                    .numeroDocumento(json.get("document_number").getAsString())
                    .nombres(json.get("first_name").getAsString())
                    .apellidoPaterno(json.get("first_last_name").getAsString())
                    .apellidoMaterno(json.has("second_last_name")
                            ? json.get("second_last_name").getAsString()
                            : "")
                    .activo(true)
                    .build();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // ✅ Manejar errores HTTP específicos
            log.error("Error HTTP al consultar DNI: {} - {}", e.getStatusCode(), e.getMessage());
            if (e.getStatusCode().value() == 404) {
                throw new RuntimeException("DNI no encontrado en RENIEC");
            } else if (e.getStatusCode().value() == 401) {
                throw new RuntimeException("Token de API inválido o expirado");
            } else if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("Límite de consultas excedido. Intente más tarde");
            }

            throw new RuntimeException("Error al consultar DNI: " + e.getMessage());

        } catch (org.springframework.web.client.ResourceAccessException e) {
            // ✅ Manejar errores de conexión
            log.error("Error de conexión al consultar DNI: {}", e.getMessage());
            throw new RuntimeException("No se pudo conectar con el servicio de consulta de DNI");

        } catch (com.google.gson.JsonSyntaxException e) {
            // ✅ Manejar errores de parseo JSON
            log.error("Error al parsear respuesta JSON: {}", e.getMessage());
            throw new RuntimeException("Respuesta inválida del servicio de consulta");

        } catch (RuntimeException e) {
            // ✅ Re-lanzar excepciones de negocio
            throw e;

        } catch (Exception e) {
            // ✅ Manejar cualquier otro error inesperado
            log.error("Error inesperado al consultar DNI: {}", e.getMessage(), e);
            throw new RuntimeException("Error al consultar documento: " + e.getMessage());
        }
    }

    private Customer consultarRUC(String ruc) {
        log.info("Consultando RUC en API: {}", ruc);
        String url = rucUrl + "?numero=" + ruc;
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.set("Content-Type", "application/json");
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
        try {
            // Hacer petición a la API
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class);
            // ✅ Validar respuesta exitosa
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error al consultar RUC: código " + response.getStatusCode());
            }

            // ✅ Validar que el body no esté vacío
            if (response.getBody() == null || response.getBody().isEmpty()) {
                throw new RuntimeException("La API no devolvió datos");
            }

            // Parsear respuesta JSON
            JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();

            // ✅ Validar que contenga los campos necesarios
            if (!json.has("numero_documento")) {
                throw new RuntimeException("Respuesta de API incompleta: falta número de documento");
            }
            if (!json.has("razon_social")) {
                throw new RuntimeException("Respuesta de API incompleta: falta razón social");
            }

            return Customer.builder()
                    .tipoDocumento(TipoDocumento.RUC)
                    .numeroDocumento(json.get("numero_documento").getAsString())
                    .razonSocial(json.get("razon_social").getAsString())
                    .direccion(json.has("direccion") ? json.get("direccion").getAsString() : null)
                    .distrito(json.has("distrito") ? json.get("distrito").getAsString() : null)
                    .provincia(json.has("provincia") ? json.get("provincia").getAsString() : null)
                    .departamento(json.has("departamento") ? json.get("departamento").getAsString() : null)
                    .activo(true)
                    .build();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // ✅ Manejar errores HTTP específicos
            log.error("Error HTTP al consultar RUC: {} - {}", e.getStatusCode(), e.getMessage());
            if (e.getStatusCode().value() == 404) {
                throw new RuntimeException("RUC no encontrado en SUNAT");
            } else if (e.getStatusCode().value() == 401) {
                throw new RuntimeException("Token de API inválido o expirado");
            } else if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("Límite de consultas excedido. Intente más tarde");
            }

            throw new RuntimeException("Error al consultar RUC: " + e.getMessage());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // ✅ Manejar errores de conexión
            log.error("Error de conexión al consultar RUC: {}", e.getMessage());
            throw new RuntimeException("No se pudo conectar con el servicio de consulta de RUC");
        } catch (com.google.gson.JsonSyntaxException e) {
            // ✅ Manejar errores de parseo JSON
            log.error("Error al parsear respuesta JSON: {}", e.getMessage());
            throw new RuntimeException("Respuesta inválida del servicio de consulta");
        } catch (RuntimeException e) {
            // ✅ Re-lanzar excepciones de negocio
            throw e;
        } catch (Exception e) {
            // ✅ Manejar cualquier otro error inesperado
            log.error("Error inesperado al consultar RUC: {}", e.getMessage(), e);
            throw new RuntimeException("Error al consultar documento: " + e.getMessage());
        }
    }

    @Override
    public Customer actualizar(Long id, Customer customerActualizado) {
        Optional<Customer> customerOpt = buscarPorId(id);

        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }

        Customer customer = customerOpt.get();

        // Actualizar campos editables
        customer.setTelefono(customerActualizado.getTelefono());
        customer.setEmail(customerActualizado.getEmail());
        customer.setDireccion(customerActualizado.getDireccion());

        return customerRepository.save(customer);
    }

    @Override
    public Customer cambiarEstado(Long id, boolean activo) {
        Optional<Customer> customerOpt = buscarPorId(id);

        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }

        Customer customer = customerOpt.get();
        customer.setActivo(activo);

        return customerRepository.save(customer);
    }

    @Override
    public void eliminar(Long id) {
        cambiarEstado(id, false);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarActivos() {
        return customerRepository.countByActivoTrue();
    }
}

/**
 * EXPLICACIÓN DE LA INTEGRACIÓN CON API:
 * 
 * 1. RestTemplate:
 * - Clase de Spring para hacer peticiones HTTP
 * - Similar a fetch() en JavaScript o requests en Python
 * 
 * 2. Flujo de obtenerOCrearDesdeApi():
 * - Busca en BD → Si existe, retorna
 * - No existe → Llama API → Crea registro → Retorna
 * 
 * 3. Headers de autenticación:
 * - Authorization: Bearer {token}
 * - Necesario para que la API acepte la petición
 * 
 * 4. Parseo JSON:
 * - Gson convierte String JSON a objeto Java
 * - Accedes a campos: json.get("nombre").getAsString()
 */