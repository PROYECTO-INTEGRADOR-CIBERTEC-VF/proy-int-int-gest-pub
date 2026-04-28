package com.transparencia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.transparencia.api.exception.RecursoNoEncontradoException;
import com.transparencia.api.model.dto.CrearRespuestaRequest;
import com.transparencia.api.model.dto.RespuestaDTO;
import com.transparencia.api.model.entity.Respuesta;
import com.transparencia.api.model.entity.Solicitud;
import com.transparencia.api.service.RespuestaService;
import com.transparencia.api.service.SolicitudService;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/respuestas")
public class RespuestaRestController {

    private final RespuestaService respuestaService;
    private final SolicitudService solicitudService;

    public RespuestaRestController(
        RespuestaService respuestaService,
        SolicitudService solicitudService
    ) {
        this.respuestaService = respuestaService;
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public ResponseEntity<List<RespuestaDTO>> listar() {
        List<RespuestaDTO> respuestas = respuestaService.obtenerTodasLasRespuestas()
            .stream()
            .map(RespuestaDTO::from)
            .toList();
        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO> obtenerPorId(@PathVariable Long id) {
        Respuesta respuesta = respuestaService.obtenerRespuestaPorId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Respuesta no encontrada con ID: " + id));
        return ResponseEntity.ok(RespuestaDTO.from(respuesta));
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<RespuestaDTO> obtenerPorSolicitud(@PathVariable Long solicitudId) {
        Solicitud solicitud = solicitudService.obtenerSolicitudPorId(solicitudId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada con ID: " + solicitudId));

        if (solicitud.getRespuesta() == null) {
            throw new RecursoNoEncontradoException("La solicitud no tiene respuesta");
        }

        return ResponseEntity.ok(RespuestaDTO.from(solicitud.getRespuesta()));
    }

    @PostMapping
    public ResponseEntity<RespuestaDTO> crear(@Valid @RequestBody CrearRespuestaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(respuestaService.crearRespuesta(request));
    }

    @PostMapping("/aceptar")
    public ResponseEntity<RespuestaDTO> aceptarSolicitud(@Valid @RequestBody CrearRespuestaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(respuestaService.aceptarSolicitud(request));
    }

    @PostMapping("/denegar")
    public ResponseEntity<RespuestaDTO> denegarSolicitud(@Valid @RequestBody CrearRespuestaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(respuestaService.denegarSolicitud(request));
    }

    @GetMapping("/causales-denegacion")
    public ResponseEntity<List<String>> obtenerCausalesDenegacion() {
        List<String> causales = List.of(
            "Informacion Confidencial - Art. 15 Ley 27806",
            "Informacion Secreta - Art. 15-A Ley 27806",
            "Informacion Reservada - Art. 15-B Ley 27806",
            "Informacion que afecta la intimidad personal",
            "Informacion vinculada a investigaciones en tramite",
            "Secreto bancario, tributario, comercial o tecnologico",
            "Informacion elaborada como consejo o recomendacion",
            "Informacion que ponga en riesgo la vida o seguridad de personas",
            "La solicitud requiere una elaboracion de la informacion"
        );
        return ResponseEntity.ok(causales);
    }
}
