package com.transparencia.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.transparencia.api.model.dto.EntidadDTO;
import com.transparencia.api.model.entity.Entidad;
import com.transparencia.api.service.EntidadService;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/entidades")
public class EntidadRestController {

    private final EntidadService entidadService;

    public EntidadRestController(EntidadService entidadService) {
        this.entidadService = entidadService;
    }

    @GetMapping
    public ResponseEntity<List<EntidadDTO>> listar() {
        List<EntidadDTO> entidades = entidadService.findActivas()
            .stream()
            .map(EntidadDTO::from)
            .toList();

        return ResponseEntity.ok(entidades);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntidadDTO> obtenerPorId(@PathVariable Long id) {
        Entidad entidad = entidadService.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Entidad no encontrada con ID: " + id));

        return ResponseEntity.ok(EntidadDTO.from(entidad));
    }
}