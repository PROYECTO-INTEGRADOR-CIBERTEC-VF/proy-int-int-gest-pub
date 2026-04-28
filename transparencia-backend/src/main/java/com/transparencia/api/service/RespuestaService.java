package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.transparencia.api.exception.RecursoNoEncontradoException;
import com.transparencia.api.model.dto.CrearRespuestaRequest;
import com.transparencia.api.model.dto.RespuestaDTO;
import com.transparencia.api.model.entity.Funcionario;
import com.transparencia.api.model.entity.Respuesta;
import com.transparencia.api.model.entity.Solicitud;
import com.transparencia.api.repository.RespuestaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final SolicitudService solicitudService;
    private final FuncionarioService funcionarioService;

    public RespuestaService(
        RespuestaRepository respuestaRepository,
        SolicitudService solicitudService,
        FuncionarioService funcionarioService
    ) {
        this.respuestaRepository = respuestaRepository;
        this.solicitudService = solicitudService;
        this.funcionarioService = funcionarioService;
    }

    @Transactional(readOnly = true)
    public List<Respuesta> obtenerTodasLasRespuestas() {
        return respuestaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Respuesta> obtenerRespuestaPorId(Long id) {
        return respuestaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Respuesta> obtenerRespuestaPorSolicitud(Long solicitudId) {
        return respuestaRepository.findBySolicitud_IdSolicitud(solicitudId);
    }

    @Transactional
    public RespuestaDTO crearRespuesta(CrearRespuestaRequest request) {
        Solicitud solicitud = buscarSolicitud(request.solicitudId());
        validarSinRespuesta(solicitud);
        Funcionario funcionario = buscarFuncionario(request.funcionarioId());

        Respuesta respuesta = new Respuesta();
        respuesta.setSolicitud(solicitud);
        respuesta.setFuncionario(funcionario);
        respuesta.setContenido(request.contenido().trim());
        respuesta.setFechaRespuesta(LocalDateTime.now());

        aplicarTipoRespuesta(respuesta, solicitud, request);
        persistirRespuestaYSolicitud(solicitud, respuesta);
        return RespuestaDTO.from(respuesta);
    }

    @Transactional
    public RespuestaDTO aceptarSolicitud(CrearRespuestaRequest request) {
        Solicitud solicitud = buscarSolicitud(request.solicitudId());
        validarSinRespuesta(solicitud);
        Funcionario funcionario = buscarFuncionario(request.funcionarioId());

        Respuesta respuesta = new Respuesta();
        respuesta.setSolicitud(solicitud);
        respuesta.setFuncionario(funcionario);
        respuesta.setTipoRespuesta(Respuesta.TipoRespuesta.ENTREGA_TOTAL);
        respuesta.setDecision("aceptar");
        respuesta.setContenido(construirContenidoAceptacion(request));
        respuesta.setFechaRespuesta(LocalDateTime.now());

        solicitud.setEstado(Solicitud.EstadoSolicitud.RESPONDIDA);
        persistirRespuestaYSolicitud(solicitud, respuesta);
        return RespuestaDTO.from(respuesta);
    }

    @Transactional
    public RespuestaDTO denegarSolicitud(CrearRespuestaRequest request) {
        Solicitud solicitud = buscarSolicitud(request.solicitudId());
        validarSinRespuesta(solicitud);
        Funcionario funcionario = buscarFuncionario(request.funcionarioId());
        validarDenegacion(request);

        Respuesta respuesta = new Respuesta();
        respuesta.setSolicitud(solicitud);
        respuesta.setFuncionario(funcionario);
        respuesta.setTipoRespuesta(Respuesta.TipoRespuesta.DENEGACION_TOTAL);
        respuesta.setDecision("denegar");
        respuesta.setContenido(request.contenido().trim());
        respuesta.setCausalDenegatoria(request.causalDenegatoria().trim());
        respuesta.setFundamentoLegal(trimToNull(request.fundamentoLegal()));
        respuesta.setFechaRespuesta(LocalDateTime.now());

        solicitud.setEstado(Solicitud.EstadoSolicitud.DENEGADA);
        persistirRespuestaYSolicitud(solicitud, respuesta);
        return RespuestaDTO.from(respuesta);
    }

    private void aplicarTipoRespuesta(Respuesta respuesta, Solicitud solicitud, CrearRespuestaRequest request) {
        String tipo = request.tipoRespuesta().trim().toUpperCase(Locale.ROOT);
        switch (tipo) {
            case "SILENCIO_ADMINISTRATIVO" -> throw new IllegalArgumentException(
                "Silencio administrativo se aplica automaticamente y no se registra manualmente"
            );
            case "ENTREGA_TOTAL", "ACEPTADA" -> {
                respuesta.setTipoRespuesta(Respuesta.TipoRespuesta.ENTREGA_TOTAL);
                respuesta.setDecision("aceptar");
                solicitud.setEstado(Solicitud.EstadoSolicitud.RESPONDIDA);
            }
            case "ENTREGA_PARCIAL" -> {
                respuesta.setTipoRespuesta(Respuesta.TipoRespuesta.ENTREGA_PARCIAL);
                respuesta.setDecision("aceptar");
                solicitud.setEstado(Solicitud.EstadoSolicitud.RESPONDIDA);
            }
            case "DENEGACION_TOTAL", "DENEGADA" -> {
                validarDenegacion(request);
                respuesta.setTipoRespuesta(Respuesta.TipoRespuesta.DENEGACION_TOTAL);
                respuesta.setDecision("denegar");
                respuesta.setCausalDenegatoria(request.causalDenegatoria().trim());
                respuesta.setFundamentoLegal(trimToNull(request.fundamentoLegal()));
                solicitud.setEstado(Solicitud.EstadoSolicitud.DENEGADA);
            }
            default -> throw new IllegalArgumentException("Tipo de respuesta no valido: " + request.tipoRespuesta());
        }
    }

    private Solicitud buscarSolicitud(Long solicitudId) {
        return solicitudService.obtenerSolicitudPorId(solicitudId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada con ID: " + solicitudId));
    }

    private Funcionario buscarFuncionario(Long funcionarioId) {
        return funcionarioService.obtenerFuncionarioPorId(funcionarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Funcionario no encontrado con ID: " + funcionarioId));
    }

    private void validarSinRespuesta(Solicitud solicitud) {
        if (solicitud.getRespuesta() != null) {
            throw new IllegalArgumentException("La solicitud ya tiene una respuesta");
        }
    }

    private void validarDenegacion(CrearRespuestaRequest request) {
        if (!StringUtils.hasText(request.causalDenegatoria())) {
            throw new IllegalArgumentException("La causal denegatoria es obligatoria para denegacion total");
        }
    }

    private void persistirRespuestaYSolicitud(Solicitud solicitud, Respuesta respuesta) {
        respuestaRepository.save(respuesta);
        solicitud.setRespuesta(respuesta);
        solicitudService.save(solicitud);
    }

    private String construirContenidoAceptacion(CrearRespuestaRequest request) {
        StringBuilder contenido = new StringBuilder(request.contenido().trim());
        if (StringUtils.hasText(request.formatoEntrega())) {
            contenido.append("\n\nFormato de entrega: ").append(request.formatoEntrega().trim());
        }
        if (request.plazoEntrega() != null) {
            contenido.append("\nPlazo estimado: ").append(request.plazoEntrega()).append(" dias habiles");
        }
        return contenido.toString();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
