package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transparencia.api.model.entity.Resolucion;
import com.transparencia.api.repository.ResolucionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ResolucionService {

    private final ResolucionRepository resolucionRepository;

    public ResolucionService(ResolucionRepository resolucionRepository) {
        this.resolucionRepository = resolucionRepository;
    }

    @Transactional(readOnly = true)
    public List<Resolucion> obtenerTodasLasResoluciones() {
        return resolucionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Resolucion> obtenerResolucionPorId(Long id) {
        return resolucionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Resolucion> obtenerResolucionesPorApelacion(Long apelacionId) {
        return resolucionRepository.findByApelacion_IdApelacion(apelacionId);
    }

    @Transactional(readOnly = true)
    public List<Resolucion> obtenerResolucionesPorTipo(Resolucion.TipoResolucion tipo) {
        return resolucionRepository.findByTipoResolucion(tipo);
    }

    @Transactional
    public void guardarResolucion(Resolucion resolucion) {
        resolucionRepository.save(resolucion);
    }

    @Transactional
    public void save(Resolucion resolucion) {
        resolucionRepository.save(resolucion);
    }

    @Transactional
    public void eliminarResolucion(Long id) {
        resolucionRepository.deleteById(id);
    }
}
