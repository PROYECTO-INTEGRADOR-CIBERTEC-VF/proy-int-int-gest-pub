package com.transparencia.api.service;

import com.transparencia.api.exception.RecursoNoEncontradoException;
import com.transparencia.api.model.entity.Solicitud;
import com.transparencia.api.repository.ApelacionRepository;
import com.transparencia.api.repository.CiudadanoRepository;
import com.transparencia.api.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApelacionService {

    private final ApelacionRepository apelacionRepository;
    private final CiudadanoRepository ciudadanoRepository;
    private final SolicitudRepository solicitudRepository;

    @Transactional(readOnly = true)
    public void validarNuevaApelacion(Long idCiudadano, Long idSolicitud) {
        
        // 1. Validar que el ciudadano exista
        ciudadanoRepository.findById(idCiudadano)
            .orElseThrow(() -> new RecursoNoEncontradoException("Error: El ciudadano con ID " + idCiudadano + " no se encuentra registrado."));

        // 2. Validar que la solicitud exista en la base de datos
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
            .orElseThrow(() -> new RecursoNoEncontradoException("Error: La solicitud con ID " + idSolicitud + " no existe."));

        // 3. Validar que la solicitud NO tenga una apelación previa
        if (apelacionRepository.existsBySolicitud_IdSolicitud(idSolicitud)) {
            throw new IllegalArgumentException("La solicitud con expediente " + solicitud.getExpediente() + " ya cuenta con una apelación en curso.");
        }

        
    }
}