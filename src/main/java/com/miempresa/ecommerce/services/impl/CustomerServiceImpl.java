package com.miempresa.ecommerce.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod; // <<--- AÑADIDO
import org.springframework.http.ResponseEntity; // <<--- AÑADIDO
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <<--- AÑADIDO
import org.springframework.web.client.HttpClientErrorException; // <<--- AÑADIDO
import org.springframework.web.client.ResourceAccessException; // <<--- AÑADIDO
import org.springframework.web.client.RestTemplate; // <<--- AÑADIDO

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException; // <<--- AÑADIDO
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
    private final RestTemplate restTemplate = new RestTemplate(); // Definido aquí

    private static final Long ID_CLIENTE_GENERICO = 1L; // ID esperado para 'Cliente Varios'

    // Configuración de API Decolecta desde application.properties
    @Value("${api.decolecta.token}")
    private String apiToken;

    @Value("${api.decolecta.dni-url}")
    private String dniUrl;

    @Value("${api.decolecta.ruc-url}")
    private String rucUrl;

    // Corregido: Lógica robusta para obtener cliente genérico
    @Override
    @Transactional(readOnly = true)
    public Customer obtenerClienteGenerico() {
        // Intenta buscar por ID (más eficiente)
        Optional<Customer> clienteOpt = customerRepository.findById(ID_CLIENTE_GENERICO);

        // Si no se encuentra por ID, intenta buscar por DNI '00000000' como respaldo
        if (clienteOpt.isEmpty()) {
            log.warn("Cliente genérico con ID {} no encontrado, buscando por DNI 00000000...", ID_CLIENTE_GENERICO);
            clienteOpt = customerRepository.findByNumeroDocumento("00000000");
        }

        // Si aún no se encuentra, lanzar error crítico
        if (clienteOpt.isEmpty()) {
            log.error(
                    "¡ERROR CRÍTICO! No se encontró el registro para 'Cliente Varios' (ID: {} o DNI: 00000000). La base de datos necesita este registro.",
                    ID_CLIENTE_GENERICO);
            // Asegúrate de tener este cliente en tu data.sql o tabla
            throw new RuntimeException("Cliente genérico no configurado/encontrado en la base de datos.");
        }

        log.debug("Cliente genérico encontrado: ID {}", clienteOpt.get().getId());
        return clienteOpt.get();
    }

    @Override
    public Customer guardar(Customer customer) {
        log.info("Guardando cliente: {}", customer.getNumeroDocumento());

        // Validar documento único al crear (si no tiene ID)
        if (customer.getId() == null && customer.getNumeroDocumento() != null
                && existePorDocumento(customer.getNumeroDocumento())) {
            // Podrías lanzar excepción o buscar y devolver el existente
            log.warn("Intento de guardar cliente duplicado con documento {}", customer.getNumeroDocumento());
            // Devolver el existente en lugar de lanzar error podría ser mejor UX
            return customerRepository.findByNumeroDocumento(customer.getNumeroDocumento())
                    .orElseThrow(() -> new RuntimeException("Error inesperado al buscar cliente duplicado."));
            // throw new RuntimeException("Ya existe un cliente con el documento " +
            // customer.getNumeroDocumento());
        }

        // Validar tipo de documento vs número
        if (customer.getTipoDocumento() != null && customer.getNumeroDocumento() != null) {
            TipoDocumento tipoDetectado = TipoDocumento.obtenerPorNumero(customer.getNumeroDocumento());
            if (tipoDetectado != customer.getTipoDocumento()) {
                throw new RuntimeException("El tipo de documento (" + customer.getTipoDocumento()
                        + ") no coincide con la longitud del número (" + customer.getNumeroDocumento().length()
                        + " dígitos).");
            }
        } else if (customer.getNumeroDocumento() != null) {
            // Si solo viene el número, inferir tipo
            customer.setTipoDocumento(TipoDocumento.obtenerPorNumero(customer.getNumeroDocumento()));
            if (customer.getTipoDocumento() == null) {
                throw new RuntimeException("Número de documento inválido (longitud incorrecta).");
            }
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

    // Corregido: Manejo de errores API más detallado
    @Override
    public Customer obtenerOCrearDesdeApi(String numeroDocumento) {
        log.info("Obteniendo o creando cliente con documento: {}", numeroDocumento);

        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new RuntimeException("El número de documento no puede estar vacío.");
        }

        // Limpiar espacios
        numeroDocumento = numeroDocumento.trim();

        // PASO 1: Buscar en base de datos local
        Optional<Customer> clienteExistente = buscarPorDocumento(numeroDocumento);

        if (clienteExistente.isPresent()) {
            log.info("Cliente {} encontrado en BD local", numeroDocumento);
            return clienteExistente.get();
        }

        // PASO 2: No existe, llamar a API Decolecta
        log.info("Cliente {} no existe en BD local, consultando API Decolecta...", numeroDocumento);

        TipoDocumento tipoDocumento = TipoDocumento.obtenerPorNumero(numeroDocumento);

        if (tipoDocumento == null) {
            throw new RuntimeException("Número de documento '" + numeroDocumento + "' inválido (longitud incorrecta).");
        }

        Customer nuevoCliente;

        try {
            if (tipoDocumento == TipoDocumento.DNI) {
                nuevoCliente = consultarDNI(numeroDocumento);
            } else { // RUC
                nuevoCliente = consultarRUC(numeroDocumento);
            }

            // Guardar en base de datos
            log.info("Datos obtenidos de API para {}, guardando en BD...", numeroDocumento);
            return guardar(nuevoCliente); // guardar maneja duplicados si ocurren por concurrencia

        } catch (HttpClientErrorException e) {
            log.error("Error HTTP {} al consultar API Decolecta para {}: {}", e.getStatusCode(), numeroDocumento,
                    e.getResponseBodyAsString());
            String errorMsg = "Error al consultar documento: ";
            if (e.getStatusCode().value() == 404) {
                errorMsg += (tipoDocumento == TipoDocumento.DNI ? "DNI" : "RUC") + " no encontrado.";
            } else if (e.getStatusCode().value() == 401) {
                errorMsg += "Token de API inválido o expirado.";
            } else if (e.getStatusCode().value() == 429) {
                errorMsg += "Límite de consultas API excedido. Intente más tarde.";
            } else {
                errorMsg += e.getStatusCode() + " - " + e.getStatusText();
            }
            throw new RuntimeException(errorMsg);
        } catch (ResourceAccessException e) {
            log.error("Error de conexión al consultar API Decolecta para {}: {}", numeroDocumento, e.getMessage());
            throw new RuntimeException("No se pudo conectar con el servicio de consulta de documentos.");
        } catch (JsonSyntaxException e) {
            log.error("Error al parsear respuesta JSON de API Decolecta para {}: {}", numeroDocumento, e.getMessage());
            throw new RuntimeException("Respuesta inválida del servicio de consulta de documentos.");
        } catch (RuntimeException e) { // Relanzar excepciones específicas de consultarDNI/consultarRUC
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al consultar API Decolecta para {}: {}", numeroDocumento, e.getMessage(), e);
            throw new RuntimeException("Error inesperado al consultar documento: " + e.getMessage());
        }
    }

    // Corregido: Manejo de errores API más detallado
    private Customer consultarDNI(String dni) {
        log.info("Consultando DNI {} en API...", dni);
        String url = dniUrl + "?numero=" + dni;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.set("Accept", "application/json"); // Especificar que aceptamos JSON
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException(
                    "Error al consultar DNI: Respuesta no exitosa o cuerpo vacío. Status: " + response.getStatusCode());
        }

        JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();

        // Validar campos esenciales en la respuesta
        if (!json.has("document_number") || !json.has("first_name") || !json.has("first_last_name")) {
            log.error("Respuesta JSON incompleta de API DNI: {}", response.getBody());
            throw new RuntimeException("Respuesta de API DNI incompleta.");
        }

        log.info("Datos DNI {} obtenidos de API.", dni);
        return Customer.builder()
                .tipoDocumento(TipoDocumento.DNI)
                .numeroDocumento(json.get("document_number").getAsString())
                .nombres(json.get("first_name").getAsString())
                .apellidoPaterno(json.get("first_last_name").getAsString())
                .apellidoMaterno(json.has("second_last_name") && !json.get("second_last_name").isJsonNull()
                        ? json.get("second_last_name").getAsString()
                        : "") // Manejar nulos
                .activo(true)
                .build();
    }

    // Corregido: Manejo de errores API más detallado
    private Customer consultarRUC(String ruc) {
        log.info("Consultando RUC {} en API...", ruc);
        String url = rucUrl + "?numero=" + ruc;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.set("Accept", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException(
                    "Error al consultar RUC: Respuesta no exitosa o cuerpo vacío. Status: " + response.getStatusCode());
        }

        JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();

        // Validar campos esenciales
        if (!json.has("numero_documento") || !json.has("razon_social")) {
            log.error("Respuesta JSON incompleta de API RUC: {}", response.getBody());
            throw new RuntimeException("Respuesta de API RUC incompleta.");
        }

        log.info("Datos RUC {} obtenidos de API.", ruc);
        return Customer.builder()
                .tipoDocumento(TipoDocumento.RUC)
                .numeroDocumento(json.get("numero_documento").getAsString())
                .razonSocial(json.get("razon_social").getAsString())
                // Añadir manejo de nulos para campos opcionales
                .direccion(json.has("direccion") && !json.get("direccion").isJsonNull()
                        ? json.get("direccion").getAsString()
                        : null)
                .distrito(json.has("distrito") && !json.get("distrito").isJsonNull()
                        ? json.get("distrito").getAsString()
                        : null)
                .provincia(json.has("provincia") && !json.get("provincia").isJsonNull()
                        ? json.get("provincia").getAsString()
                        : null)
                .departamento(json.has("departamento") && !json.get("departamento").isJsonNull()
                        ? json.get("departamento").getAsString()
                        : null)
                .activo(true) // O verificar estado en API si existe
                .build();
    }

    @Override
    public Customer actualizar(Long id, Customer customerActualizado) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        log.info("Actualizando cliente ID: {}", id);

        // Actualizar solo campos editables (ej. contacto, dirección)
        // No permitir cambiar documento, nombres/razón social (a menos que se quiera
        // re-consultar API)
        customer.setTelefono(customerActualizado.getTelefono());
        customer.setEmail(customerActualizado.getEmail());
        customer.setDireccion(customerActualizado.getDireccion());
        customer.setDistrito(customerActualizado.getDistrito());
        customer.setProvincia(customerActualizado.getProvincia());
        customer.setDepartamento(customerActualizado.getDepartamento());
        // No actualizamos 'activo' aquí, usar cambiarEstado

        return customerRepository.save(customer);
    }

    @Override
    public Customer cambiarEstado(Long id, boolean activo) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        if (customer.getActivo() == activo) {
            log.info("Estado del cliente ID {} no cambió (ya estaba {})", id, activo ? "activo" : "inactivo");
            return customer; // No guardar si no hay cambio
        }

        customer.setActivo(activo);
        log.info("Cambiando estado del cliente ID: {} a {}", id, activo ? "ACTIVO" : "INACTIVO");
        return customerRepository.save(customer);
    }

    @Override
    public void eliminar(Long id) {
        log.warn("Llamando a eliminación lógica (cambiarEstado a false) para cliente ID: {}", id);
        cambiarEstado(id, false);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarActivos() {
        return customerRepository.countByActivoTrue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Customer obtenerOCrearDesdeWeb(String documento, String nombres, String apellidos,
            String telefono, String email) {
        log.info("Obteniendo o creando cliente desde web: documento={}", documento);

        // Buscar cliente existente por documento
        Optional<Customer> existente = buscarPorDocumento(documento);
        if (existente.isPresent()) {
            log.info("Cliente encontrado en BD: {}", existente.get().getNombreCompleto());
            return existente.get();
        }

        // Crear nuevo cliente con los datos proporcionados
        Customer nuevoCliente = Customer.builder()
                .tipoDocumento(documento.length() == 8 ? TipoDocumento.DNI : TipoDocumento.RUC)
                .numeroDocumento(documento)
                .nombres(nombres)
                .apellidoPaterno(apellidos != null && apellidos.contains(" ")
                        ? apellidos.split(" ")[0]
                        : apellidos)
                .apellidoMaterno(apellidos != null && apellidos.contains(" ")
                        ? apellidos.substring(apellidos.indexOf(" ") + 1)
                        : "")
                .telefono(telefono)
                .email(email)
                .activo(true)
                .build();

        Customer clienteGuardado = customerRepository.save(nuevoCliente);
        log.info("Nuevo cliente creado desde web: {}", clienteGuardado.getNombreCompleto());

        return clienteGuardado;
    }
}
