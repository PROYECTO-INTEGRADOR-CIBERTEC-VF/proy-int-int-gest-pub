package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.repository.ApelacionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ApelacionService {

    private static final List<Apelacion.EstadoApelacion> ESTADOS_FINALES = List.of(
        Apelacion.EstadoApelacion.RESUELTO,
        Apelacion.EstadoApelacion.RESUELTO_FUNDADO,
        Apelacion.EstadoApelacion.RESUELTO_FUNDADO_EN_PARTE,
        Apelacion.EstadoApelacion.RESUELTO_INFUNDADO,
        Apelacion.EstadoApelacion.RESUELTO_INFUNDADO_EN_PARTE,
        Apelacion.EstadoApelacion.RESUELTO_IMPROCEDENTE,
        Apelacion.EstadoApelacion.TENER_POR_NO_PRESENTADO,
        Apelacion.EstadoApelacion.CONCLUSION_SUSTRACCION_MATERIA,
        Apelacion.EstadoApelacion.CONCLUSION_DESISTIMIENTO
    );

    private final ApelacionRepository apelacionRepository;

    public ApelacionService(ApelacionRepository apelacionRepository) {
        this.apelacionRepository = apelacionRepository;
    }

    @Transactional(readOnly = true)
    public List<Apelacion> obtenerTodasLasApelaciones() {
        return apelacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Apelacion> obtenerApelacionPorId(Long id) {
        return apelacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Apelacion> obtenerApelacionPorExpediente(String expediente) {
        return apelacionRepository.findByExpediente(expediente);
    }

    @Transactional(readOnly = true)
    public List<Apelacion> obtenerApelacionesPorEstado(Apelacion.EstadoApelacion estado) {
        return apelacionRepository.findByEstadoOrderByFechaApelacionDesc(estado);
    }

    @Transactional(readOnly = true)
    public long contarApelacionesPorEstado(Apelacion.EstadoApelacion estado) {
        return apelacionRepository.countByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Apelacion> findByCiudadanoId(Long ciudadanoId) {
        return apelacionRepository.findByCiudadano_IdUsuarioOrderByFechaApelacionDesc(ciudadanoId);
    }

    @Transactional(readOnly = true)
    public List<Apelacion> findPendientes() {
        return apelacionRepository.findPendientes(ESTADOS_FINALES);
    }

    @Transactional(readOnly = true)
    public List<Apelacion> findAll() {
        return apelacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Apelacion> findById(Long id) {
        return apelacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Apelacion> findByIdWithDetails(Long id) {
        return apelacionRepository.findByIdWithDetails(id);
    }

    @Transactional
    public Apelacion save(Apelacion apelacion) {
        return apelacionRepository.save(apelacion);
    }

    @Transactional(readOnly = true)
    public long count() {
        return apelacionRepository.count();
    }
}
