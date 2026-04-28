package com.transparencia.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.transparencia.api.model.dto.ApelacionDTO;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.service.ApelacionService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/apelaciones")
public class ApelacionRestController {

    private final ApelacionService apelacionService;

    public ApelacionRestController(ApelacionService apelacionService) {
        this.apelacionService = apelacionService;
    }

    @GetMapping
    public ResponseEntity<List<ApelacionDTO>> listar() {
        List<ApelacionDTO> apelaciones = apelacionService.obtenerTodasLasApelaciones()
            .stream()
            .map(ApelacionDTO::from)
            .toList();
        return ResponseEntity.ok(apelaciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApelacionDTO> obtenerPorId(@PathVariable Long id) {
        Apelacion apelacion = apelacionService.obtenerApelacionPorId(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Apelacion no encontrada con ID: " + id));
        return ResponseEntity.ok(ApelacionDTO.from(apelacion));
    }

    @GetMapping("/expediente/{expediente}")
    public ResponseEntity<ApelacionDTO> obtenerPorExpediente(@PathVariable String expediente) {
        Apelacion apelacion = apelacionService.obtenerApelacionPorExpediente(expediente)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Apelacion no encontrada con expediente: " + expediente
            ));
        return ResponseEntity.ok(ApelacionDTO.from(apelacion));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ApelacionDTO>> listarPorEstado(@PathVariable String estado) {
        Apelacion.EstadoApelacion estadoEnum;
        try {
            estadoEnum = Apelacion.EstadoApelacion.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no valido: " + estado);
        }

        List<ApelacionDTO> apelaciones = apelacionService.obtenerApelacionesPorEstado(estadoEnum)
            .stream()
            .map(ApelacionDTO::from)
            .toList();
        return ResponseEntity.ok(apelaciones);
    }

    @GetMapping("/ciudadano/{ciudadanoId}")
    public ResponseEntity<List<ApelacionDTO>> listarPorCiudadano(@PathVariable Long ciudadanoId) {
        List<ApelacionDTO> apelaciones = apelacionService.findByCiudadanoId(ciudadanoId)
            .stream()
            .map(ApelacionDTO::from)
            .toList();
        return ResponseEntity.ok(apelaciones);
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", apelacionService.count());
        stats.put("enCalificacion", apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_1)
            + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_2));
        stats.put("enSubsanacion", apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.EN_SUBSANACION));
        stats.put("enResolucion", apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.EN_RESOLUCION));
        return ResponseEntity.ok(stats);
    }

    public record CrearApelacionRequest(
        @NotNull(message = "El ID de solicitud es obligatorio")
        Long solicitudId,

        @NotNull(message = "El ID de ciudadano es obligatorio")
        Long ciudadanoId,

        @NotBlank(message = "Los fundamentos son obligatorios")
        @Size(max = 5000, message = "Los fundamentos no pueden exceder 5000 caracteres")
        String fundamentos
    ) {}

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CrearApelacionRequest request) {
        try {
            Apelacion apelacion = apelacionService.crearApelacion(
                request.solicitudId(),
                request.ciudadanoId(),
                request.fundamentos().trim()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(ApelacionDTO.from(apelacion));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    public record SubsanacionRequest(
        @NotBlank(message = "Los fundamentos adicionales son obligatorios")
        @Size(max = 5000)
        String fundamentosAdicionales
    ) {}

    @PostMapping("/{id}/subsanacion")
    public ResponseEntity<?> subsanar(
        @PathVariable Long id,
        @Valid @RequestBody SubsanacionRequest request
    ) {
        try {
            Apelacion apelacion = apelacionService.subsanar(id, request.fundamentosAdicionales().trim());
            return ResponseEntity.ok(ApelacionDTO.from(apelacion));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
