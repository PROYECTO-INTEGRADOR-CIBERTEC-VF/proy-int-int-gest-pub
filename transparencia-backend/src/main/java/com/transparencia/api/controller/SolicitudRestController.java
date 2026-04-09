package com.transparencia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.transparencia.api.model.dto.ActualizarEstadoSolicitudRequest;
import com.transparencia.api.model.dto.ActualizarSolicitudRequest;
import com.transparencia.api.model.dto.CrearSolicitudRequest;
import com.transparencia.api.model.dto.SolicitudDTO;
import com.transparencia.api.model.entity.Ciudadano;
import com.transparencia.api.model.entity.Entidad;
import com.transparencia.api.model.entity.Solicitud;
import com.transparencia.api.repository.CiudadanoRepository;
import com.transparencia.api.repository.EntidadRepository;
import com.transparencia.api.service.SolicitudService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/solicitudes")
public class SolicitudRestController {

    private final SolicitudService solicitudService;
    private final CiudadanoRepository ciudadanoRepository;
    private final EntidadRepository entidadRepository;

    public SolicitudRestController(
        SolicitudService solicitudService,
        CiudadanoRepository ciudadanoRepository,
        EntidadRepository entidadRepository
    ) {
        this.solicitudService = solicitudService;
        this.ciudadanoRepository = ciudadanoRepository;
        this.entidadRepository = entidadRepository;
    }

    @GetMapping
    public ResponseEntity<List<SolicitudDTO>> listar() {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerTodasLasSolicitudes()
            .stream()
            .map(SolicitudDTO::from)
            .toList();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudDTO> obtenerPorId(@PathVariable Long id) {
        Solicitud solicitud = solicitudService.obtenerSolicitudPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada con ID: " + id));
        return ResponseEntity.ok(SolicitudDTO.from(solicitud));
    }

    @GetMapping("/expediente/{expediente}")
    public ResponseEntity<SolicitudDTO> obtenerPorExpediente(@PathVariable String expediente) {
        Solicitud solicitud = solicitudService.findByExpediente(expediente)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Solicitud no encontrada con expediente: " + expediente
            ));
        return ResponseEntity.ok(SolicitudDTO.from(solicitud));
    }

    @GetMapping("/ciudadano/{ciudadanoId}")
    public ResponseEntity<List<SolicitudDTO>> listarPorCiudadano(@PathVariable Long ciudadanoId) {
        List<SolicitudDTO> solicitudes = solicitudService.findByCiudadanoId(ciudadanoId)
            .stream()
            .map(SolicitudDTO::from)
            .toList();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/entidad/{entidadId}")
    public ResponseEntity<List<SolicitudDTO>> listarPorEntidad(@PathVariable Long entidadId) {
        List<SolicitudDTO> solicitudes = solicitudService.findByEntidadId(entidadId)
            .stream()
            .map(SolicitudDTO::from)
            .toList();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SolicitudDTO>> listarPorEstado(@PathVariable String estado) {
        Solicitud.EstadoSolicitud estadoEnum = parseEstado(estado);
        List<SolicitudDTO> solicitudes = solicitudService.obtenerSolicitudesPorEstado(estadoEnum)
            .stream()
            .map(SolicitudDTO::from)
            .toList();
        return ResponseEntity.ok(solicitudes);
    }

    @PostMapping
    public ResponseEntity<SolicitudDTO> crear(@Valid @RequestBody CrearSolicitudRequest request) {
        Ciudadano ciudadano = ciudadanoRepository.findById(request.ciudadanoId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Ciudadano no encontrado con ID: " + request.ciudadanoId()
            ));

        Entidad entidad = entidadRepository.findById(request.entidadId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Entidad no encontrada con ID: " + request.entidadId()
            ));

        Solicitud solicitud = new Solicitud();
        solicitud.setAsunto(request.asunto().trim());
        solicitud.setDescripcion(request.descripcion().trim());
        solicitud.setTipoInformacion(trimToNull(request.tipoInformacion()));
        solicitud.setFormatoDigital(Boolean.TRUE.equals(request.formatoDigital()));
        solicitud.setFormatoFisico(Boolean.TRUE.equals(request.formatoFisico()));
        solicitud.setCopiaSimple(Boolean.TRUE.equals(request.copiaSimple()));
        solicitud.setCopiaCertificada(Boolean.TRUE.equals(request.copiaCertificada()));
        solicitud.setCiudadano(ciudadano);
        solicitud.setEntidad(entidad);
        solicitud.setEstado(Solicitud.EstadoSolicitud.RECEPCIONADA);
        solicitud.setPrioridad(Solicitud.Prioridad.MEDIA);

        Solicitud creada = solicitudService.save(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(SolicitudDTO.from(creada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolicitudDTO> actualizar(
        @PathVariable Long id,
        @Valid @RequestBody ActualizarSolicitudRequest request
    ) {
        Solicitud solicitud = solicitudService.obtenerSolicitudPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada con ID: " + id));

        if (request.asunto() != null) {
            if (!StringUtils.hasText(request.asunto())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El asunto no puede estar vacio");
            }
            solicitud.setAsunto(request.asunto().trim());
        }

        if (request.descripcion() != null) {
            if (!StringUtils.hasText(request.descripcion())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripcion no puede estar vacia");
            }
            solicitud.setDescripcion(request.descripcion().trim());
        }

        if (request.tipoInformacion() != null) {
            solicitud.setTipoInformacion(trimToNull(request.tipoInformacion()));
        }

        if (request.prioridad() != null) {
            solicitud.setPrioridad(parsePrioridad(request.prioridad()));
        }

        if (request.formatoDigital() != null) {
            solicitud.setFormatoDigital(request.formatoDigital());
        }

        if (request.formatoFisico() != null) {
            solicitud.setFormatoFisico(request.formatoFisico());
        }

        if (request.copiaSimple() != null) {
            solicitud.setCopiaSimple(request.copiaSimple());
        }

        if (request.copiaCertificada() != null) {
            solicitud.setCopiaCertificada(request.copiaCertificada());
        }

        Solicitud actualizada = solicitudService.save(solicitud);
        return ResponseEntity.ok(SolicitudDTO.from(actualizada));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<SolicitudDTO> cambiarEstado(
        @PathVariable Long id,
        @Valid @RequestBody ActualizarEstadoSolicitudRequest request
    ) {
        Solicitud solicitud = solicitudService.obtenerSolicitudPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada con ID: " + id));

        solicitud.setEstado(parseEstado(request.estado()));
        Solicitud actualizada = solicitudService.save(solicitud);
        return ResponseEntity.ok(SolicitudDTO.from(actualizada));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", solicitudService.count());
        stats.put("recepcionadas", solicitudService.contarSolicitudesPorEstado(Solicitud.EstadoSolicitud.RECEPCIONADA));
        stats.put("enRevision", solicitudService.contarSolicitudesPorEstado(Solicitud.EstadoSolicitud.EN_REVISION));
        stats.put("pendienteInformacion", solicitudService.contarSolicitudesPorEstado(Solicitud.EstadoSolicitud.PENDIENTE_INFORMACION));
        stats.put("respondidas", solicitudService.contarSolicitudesPorEstado(Solicitud.EstadoSolicitud.RESPONDIDA));
        stats.put("denegadas", solicitudService.contarSolicitudesPorEstado(Solicitud.EstadoSolicitud.DENEGADA));
        stats.put("concluidas", solicitudService.contarSolicitudesPorEstado(Solicitud.EstadoSolicitud.CONCLUIDA));
        stats.put("vencidas", solicitudService.contarSolicitudesPorEstado(Solicitud.EstadoSolicitud.VENCIDA));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/ciudadano/{ciudadanoId}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorCiudadano(@PathVariable Long ciudadanoId) {
        List<Solicitud> solicitudes = solicitudService.findByCiudadanoId(ciudadanoId);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", solicitudes.size());
        stats.put("recepcionadas", solicitudes.stream().filter(s -> Solicitud.EstadoSolicitud.RECEPCIONADA.equals(s.getEstado())).count());
        stats.put("enRevision", solicitudes.stream().filter(s -> Solicitud.EstadoSolicitud.EN_REVISION.equals(s.getEstado())).count());
        stats.put("respondidas", solicitudes.stream().filter(s -> Solicitud.EstadoSolicitud.RESPONDIDA.equals(s.getEstado())).count());
        stats.put("apeladas", solicitudes.stream().filter(s -> s.getApelacion() != null).count());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/entidad/{entidadId}/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorEntidad(@PathVariable Long entidadId) {
        List<Solicitud> solicitudes = solicitudService.findByEntidadId(entidadId);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", solicitudes.size());
        stats.put("recepcionadas", solicitudes.stream().filter(s -> Solicitud.EstadoSolicitud.RECEPCIONADA.equals(s.getEstado())).count());
        stats.put("enRevision", solicitudes.stream().filter(s -> Solicitud.EstadoSolicitud.EN_REVISION.equals(s.getEstado())).count());
        stats.put("respondidas", solicitudes.stream().filter(s -> Solicitud.EstadoSolicitud.RESPONDIDA.equals(s.getEstado())).count());
        stats.put("vencidas", solicitudes.stream().filter(s -> Solicitud.EstadoSolicitud.VENCIDA.equals(s.getEstado())).count());
        stats.put("urgentes", solicitudes.stream().filter(s -> s.getDiasRestantes() >= 0 && s.getDiasRestantes() <= 3).count());

        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        solicitudService.obtenerSolicitudPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada con ID: " + id));

        solicitudService.eliminarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    private Solicitud.EstadoSolicitud parseEstado(String estado) {
        try {
            return Solicitud.EstadoSolicitud.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no valido: " + estado);
        }
    }

    private Solicitud.Prioridad parsePrioridad(String prioridad) {
        try {
            return Solicitud.Prioridad.valueOf(prioridad.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prioridad no valida: " + prioridad);
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
