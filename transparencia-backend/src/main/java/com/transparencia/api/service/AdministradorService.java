package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transparencia.api.model.entity.Administrador;
import com.transparencia.api.repository.AdministradorRepository;

import java.util.Optional;

@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;

    public AdministradorService(AdministradorRepository administradorRepository) {
        this.administradorRepository = administradorRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Administrador> obtenerAdministradorPorId(Long id) {
        return administradorRepository.findById(id);
    }
}
