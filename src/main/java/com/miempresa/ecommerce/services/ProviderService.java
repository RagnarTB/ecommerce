package com.miempresa.ecommerce.services;

import com.miempresa.ecommerce.models.Provider;
import com.miempresa.ecommerce.repositories.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SERVICE: PROVEEDOR
 * 
 * Gestiona los proveedores de productos.
 */

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ProviderRepository providerRepository;

    // ========================================
    // OPERACIONES CRUD
    // ========================================

    public Provider guardar(Provider provider) {
        log.info("Guardando proveedor: {}", provider.getRazonSocial());

        // Validar RUC único
        if (provider.getId() == null && providerRepository.existsByRuc(provider.getRuc())) {
            throw new RuntimeException("Ya existe un proveedor con ese RUC");
        }

        return providerRepository.save(provider);
    }

    @Transactional(readOnly = true)
    public Optional<Provider> buscarPorId(Long id) {
        return providerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Provider> buscarPorRuc(String ruc) {
        return providerRepository.findByRuc(ruc);
    }

    @Transactional(readOnly = true)
    public List<Provider> obtenerTodos() {
        return providerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Provider> obtenerActivos() {
        return providerRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Provider> buscarPorRazonSocialORuc(String busqueda) {
        return providerRepository.buscarPorRazonSocialORuc(busqueda);
    }

    public Provider actualizar(Long id, Provider providerActualizado) {
        log.info("Actualizando proveedor ID: {}", id);

        Optional<Provider> providerOpt = buscarPorId(id);

        if (providerOpt.isEmpty()) {
            throw new RuntimeException("Proveedor no encontrado");
        }

        Provider provider = providerOpt.get();
        provider.setRazonSocial(providerActualizado.getRazonSocial());
        provider.setDireccion(providerActualizado.getDireccion());
        provider.setTelefono(providerActualizado.getTelefono());
        provider.setEmail(providerActualizado.getEmail());
        provider.setContactoNombre(providerActualizado.getContactoNombre());
        provider.setContactoTelefono(providerActualizado.getContactoTelefono());

        return providerRepository.save(provider);
    }

    public Provider cambiarEstado(Long id, boolean activo) {
        Optional<Provider> providerOpt = buscarPorId(id);

        if (providerOpt.isEmpty()) {
            throw new RuntimeException("Proveedor no encontrado");
        }

        Provider provider = providerOpt.get();
        provider.setActivo(activo);

        return providerRepository.save(provider);
    }

    public void eliminar(Long id) {
        cambiarEstado(id, false);
    }

    @Transactional(readOnly = true)
    public long contarActivos() {
        return providerRepository.countByActivoTrue();
    }

    @Transactional(readOnly = true)
    public boolean existePorRuc(String ruc) {
        return providerRepository.existsByRuc(ruc);
    }
}