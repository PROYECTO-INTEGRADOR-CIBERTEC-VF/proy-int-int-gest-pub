package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.transparencia.api.model.entity.Solicitud;
import com.transparencia.api.repository.SolicitudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SolicitudService {

    private static final List<Solicitud.EstadoSolicitud> ESTADOS_FINALES = List.of(
        Solicitud.EstadoSolicitud.RESPONDIDA,
        Solicitud.EstadoSolicitud.DENEGADA,
        Solicitud.EstadoSolicitud.CONCLUIDA,
        Solicitud.EstadoSolicitud.VENCIDA
    );

    private final SolicitudRepository solicitudRepository;

    public SolicitudService(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public List<Solicitud> obtenerTodasLasSolicitudes() {
        return solicitudRepository.findAll();
    }

    public Optional<Solicitud> obtenerSolicitudPorId(Long id) {
        return solicitudRepository.findById(id);
    }

    public Optional<Solicitud> findByExpediente(String expediente) {
        return solicitudRepository.findByExpediente(expediente);
    }

    public List<Solicitud> findByCiudadanoId(Long ciudadanoId) {
        return solicitudRepository.findByCiudadano_IdUsuarioOrderByFechaPresentacionDesc(ciudadanoId);
    }

    public List<Solicitud> findByEntidadId(Long entidadId) {
        return solicitudRepository.findByEntidad_IdEntidadOrderByFechaPresentacionDesc(entidadId);
    }

    public List<Solicitud> obtenerSolicitudesPorEstado(Solicitud.EstadoSolicitud estado) {
        return solicitudRepository.findByEstado(estado);
    }

    public long contarSolicitudesPorEstado(Solicitud.EstadoSolicitud estado) {
        return solicitudRepository.countByEstado(estado);
    }

    public long countByEntidadId(Long entidadId) {
        return solicitudRepository.countByEntidad_IdEntidad(entidadId);
    }

    public long count() {
        return solicitudRepository.count();
    }

    public String generarExpediente() {
        int year = LocalDate.now().getYear();
        String prefix = "SAIP-" + year + "-";
        Integer maxNumber = solicitudRepository.findMaxExpedienteNumber(prefix + "%");
        int siguienteNumero = (maxNumber == null ? 0 : maxNumber) + 1;
        return prefix + String.format("%05d", siguienteNumero);
    }

    @Transactional
    public Solicitud save(Solicitud solicitud) {
        if (!StringUtils.hasText(solicitud.getExpediente())) {
            solicitud.setExpediente(generarExpediente());
        }
        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public int detectarSilencioAdministrativo() {
        List<Solicitud> vencidas = solicitudRepository.findVencidas(LocalDate.now(), ESTADOS_FINALES);
        for (Solicitud solicitud : vencidas) {
            solicitud.setEstado(Solicitud.EstadoSolicitud.VENCIDA);
        }
        if (!vencidas.isEmpty()) {
            solicitudRepository.saveAll(vencidas);
        }
        return vencidas.size();
    }

    @Transactional
    public void eliminarSolicitud(Long id) {
        solicitudRepository.deleteById(id);
    }
}
