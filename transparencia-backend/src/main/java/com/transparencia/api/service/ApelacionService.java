package com.transparencia.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.EstadoApelacion;
import com.transparencia.api.repository.ApelacionRepository;
import com.transparencia.api.repository.CiudadanoRepository;
import com.transparencia.api.repository.SolicitudRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApelacionService {

    private final ApelacionRepository apelacionRepository;
    private final CiudadanoRepository ciudadanoRepository;
    private final SolicitudRepository solicitudRepository;

    @Transactional(readOnly = true)
    public List<Apelacion> findAll() {
        return apelacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Apelacion findById(Long id) {
        return apelacionRepository.findById(id).orElseThrow(() -> new RuntimeException("Apelacion no encontrada"));
    }

    @Transactional(readOnly = true)
    public Apelacion findByExpediente(String expediente) {
        return apelacionRepository.findByExpediente(expediente).orElseThrow(() -> new RuntimeException("Apelacion no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Apelacion> findByEstado(EstadoApelacion estado) {
        return apelacionRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Apelacion> findByCiudadanoId(Long ciudadanoId) {
        return apelacionRepository.findByCiudadano_IdUsuario(ciudadanoId);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getEstadisticas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", apelacionRepository.count());
        return stats;
    }

    @Transactional
    public Apelacion save(Apelacion apelacion) {
        validarNuevaApelacion(apelacion);
        if (apelacion.getExpediente() == null || apelacion.getExpediente().isEmpty()) {
            apelacion.setExpediente(generarExpediente());
        }
        return apelacionRepository.save(apelacion);
    }

    @Transactional
    public Apelacion actualizar(Long id, Apelacion apelacionDetalles) {
        Apelacion apelacion = findById(id);
        apelacion.setFundamentos(apelacionDetalles.getFundamentos());
        return apelacionRepository.save(apelacion);
    }

    @Transactional
    public Apelacion cambiarEstado(Long id, EstadoApelacion nuevoEstado) {
        Apelacion apelacion = findById(id);
        apelacion.setEstado(nuevoEstado);
        return apelacionRepository.save(apelacion);
    }

    @Transactional
    public Apelacion subsanar(Long id, String fundamentosAdicionales) {
        Apelacion apelacion = findById(id);
        if (apelacion.getEstado() != EstadoApelacion.EN_SUBSANACION) {
            throw new RuntimeException("La apelacion no se encuentra en estado de subsanacion");
        }
        String nuevosFundamentos = apelacion.getFundamentos() + "\n\n--- SUBSANACIÓN ---\n" + fundamentosAdicionales;
        apelacion.setFundamentos(nuevosFundamentos);
        apelacion.setEstado(EstadoApelacion.EN_CALIFICACION_2);
        return apelacionRepository.save(apelacion);
    }

    @Transactional
    public void eliminar(Long id) {
        apelacionRepository.deleteById(id);
    }

    private void validarNuevaApelacion(Apelacion apelacion) {
        if (apelacion.getSolicitud() != null && apelacion.getSolicitud().getIdSolicitud() != null) {
            boolean existePrevia = apelacionRepository.existsBySolicitud_IdSolicitud(apelacion.getSolicitud().getIdSolicitud());
            if (existePrevia) {
                throw new RuntimeException("La solicitud ya tiene una apelación en curso.");
            }
        }
        if (apelacion.getCiudadano() != null && apelacion.getCiudadano().getIdUsuario() != null) {
            ciudadanoRepository.findById(apelacion.getCiudadano().getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("El ciudadano no existe en el sistema."));
        }
    }

    private String generarExpediente() {
        long count = apelacionRepository.count();
        int year = LocalDate.now().getYear();
        return String.format("%05d-%d-JUS-TTAIP", count + 1, year);
    }
}