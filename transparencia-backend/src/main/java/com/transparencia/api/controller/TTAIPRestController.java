package com.transparencia.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.transparencia.api.model.dto.ApelacionDTO;
import com.transparencia.api.model.dto.CalificacionRequest;
import com.transparencia.api.model.dto.SegundaCalificacionDTO;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.service.ApelacionService;
import com.transparencia.api.service.TTAIPService;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/ttaip")
public class TTAIPRestController {

    private final TTAIPService ttaipService;
    private final ApelacionService apelacionService; // <-- Agregamos tu servicio

    public TTAIPRestController(TTAIPService ttaipService, ApelacionService apelacionService) {
        this.ttaipService = ttaipService;
        this.apelacionService = apelacionService;
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        return ResponseEntity.ok(ttaipService.obtenerEstadisticas());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<ApelacionDTO>> listarPendientes() {
        return ResponseEntity.ok(ttaipService.listarPendientes());
    }

    @GetMapping("/en-calificacion")
    public ResponseEntity<List<ApelacionDTO>> listarEnCalificacion() {
        return ResponseEntity.ok(ttaipService.listarPorEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_1));
    }

    @GetMapping("/subsanacion")
    public ResponseEntity<List<ApelacionDTO>> listarSubsanacion() {
        return ResponseEntity.ok(ttaipService.listarPorEstado(Apelacion.EstadoApelacion.EN_SUBSANACION));
    }

    @GetMapping("/segunda-calificacion")
    public ResponseEntity<List<ApelacionDTO>> listarSegundaCalificacion() {
        return ResponseEntity.ok(ttaipService.listarPorEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_2));
    }

    @GetMapping("/en-resolucion")
    public ResponseEntity<List<ApelacionDTO>> listarEnResolucion() {
        return ResponseEntity.ok(ttaipService.listarPorEstado(Apelacion.EstadoApelacion.EN_RESOLUCION));
    }

    @GetMapping("/resueltas")
    public ResponseEntity<List<ApelacionDTO>> listarResueltas() {
        return ResponseEntity.ok(ttaipService.listarResueltas());
    }

    @PostMapping("/calificacion/{apelacionId}/admitir")
    public ResponseEntity<ApelacionDTO> admitirApelacion(
            @PathVariable Long apelacionId,
            @Valid @RequestBody CalificacionRequest request
    ) {
        return ResponseEntity.ok(ttaipService.admitirApelacion(apelacionId, request));
    }

    @PostMapping("/calificacion/{apelacionId}/subsanar")
    public ResponseEntity<ApelacionDTO> requerirSubsanacion(
            @PathVariable Long apelacionId,
            @Valid @RequestBody CalificacionRequest request
    ) {
        return ResponseEntity.ok(ttaipService.requerirSubsanacion(apelacionId, request));
    }

    @PostMapping("/calificacion/{apelacionId}/inadmitir")
    public ResponseEntity<ApelacionDTO> inadmitirApelacion(
            @PathVariable Long apelacionId,
            @Valid @RequestBody CalificacionRequest request
    ) {
        return ResponseEntity.ok(ttaipService.inadmitirApelacion(apelacionId, request));
    }

    @PostMapping("/calificacion/{apelacionId}/no-presentado")
    public ResponseEntity<ApelacionDTO> declararNoPresentado(
            @PathVariable Long apelacionId,
            @Valid @RequestBody CalificacionRequest request
    ) {
        return ResponseEntity.ok(ttaipService.declararTenerPorNoPresentado(apelacionId, request));
    }

    // MÉTODO NUEVO PARA HU-07 (Segunda Calificación)

    @PostMapping(value = "/segunda-calificacion/{id}/notificar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> notificarSegundaCalificacion(
            @PathVariable("id") Long id,
            @RequestPart("datos") SegundaCalificacionDTO datos,
            @RequestPart("archivo") MultipartFile archivo) {

        try {
            Apelacion apelacionActualizada = apelacionService.procesarSegundaCalificacion(id, datos, archivo);
            return ResponseEntity.ok(apelacionActualizada);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno al procesar la calificación."));
        }
    }
}
