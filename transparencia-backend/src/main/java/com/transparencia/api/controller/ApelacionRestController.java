package com.transparencia.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.EstadoApelacion;
import com.transparencia.api.service.ApelacionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apelaciones")
@RequiredArgsConstructor
public class ApelacionRestController {

    private final ApelacionService apelacionService;

    @GetMapping("/")
    public ResponseEntity<List<Apelacion>> findAll() {
        return ResponseEntity.ok(apelacionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Apelacion> findById(@PathVariable Long id) {
        return ResponseEntity.ok(apelacionService.findById(id));
    }

    @GetMapping("/expediente/{exp}")
    public ResponseEntity<Apelacion> findByExpediente(@PathVariable String exp) {
        return ResponseEntity.ok(apelacionService.findByExpediente(exp));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Apelacion>> findByEstado(@PathVariable EstadoApelacion estado) {
        return ResponseEntity.ok(apelacionService.findByEstado(estado));
    }

    @GetMapping("/ciudadano/{id}")
    public ResponseEntity<List<Apelacion>> findByCiudadanoId(@PathVariable Long id) {
        return ResponseEntity.ok(apelacionService.findByCiudadanoId(id));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Long>> getEstadisticas() {
        return ResponseEntity.ok(apelacionService.getEstadisticas());
    }

    @PostMapping("/")
    public ResponseEntity<Apelacion> crearApelacion(@RequestBody Apelacion apelacion) {
        return new ResponseEntity<>(apelacionService.save(apelacion), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Apelacion> actualizarApelacion(@PathVariable Long id, @RequestBody Apelacion apelacion) {
        return ResponseEntity.ok(apelacionService.actualizar(id, apelacion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Apelacion> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        EstadoApelacion nuevoEstado = EstadoApelacion.valueOf(body.get("estado"));
        return ResponseEntity.ok(apelacionService.cambiarEstado(id, nuevoEstado));
    }

    @PostMapping("/{id}/subsanacion")
    public ResponseEntity<Apelacion> subsanarApelacion(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String fundamentos = body.get("fundamentosAdicionales");
        return ResponseEntity.ok(apelacionService.subsanar(id, fundamentos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarApelacion(@PathVariable Long id) {
        apelacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}