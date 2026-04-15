package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transparencia.api.model.entity.Ciudadano;
import com.transparencia.api.repository.CiudadanoRepository;

import java.util.Optional;

@Service
public class CiudadanoService {

    private final CiudadanoRepository ciudadanoRepository;

    public CiudadanoService(CiudadanoRepository ciudadanoRepository) {
        this.ciudadanoRepository = ciudadanoRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Ciudadano> obtenerCiudadanoPorDni(String dni) {
        return ciudadanoRepository.findByDni(dni);
    }

    @Transactional(readOnly = true)
    public Optional<Ciudadano> findById(Long id) {
        return ciudadanoRepository.findById(id);
    }

    @Transactional
    public Ciudadano guardarCiudadano(Ciudadano ciudadano) {
        return ciudadanoRepository.save(ciudadano);
    }
}
