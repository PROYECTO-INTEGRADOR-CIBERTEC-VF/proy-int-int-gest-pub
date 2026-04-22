package com.transparencia.api.model.dto;

import com.transparencia.api.model.entity.Entidad;

public record EntidadDTO(
    Long idEntidad,
    String nombre,
    String acronimo,
    String descripcion,
    String direccion,
    String telefono,
    String email,
    String estado,
    Boolean activa
) {
    public static EntidadDTO from(Entidad entidad) {
        return new EntidadDTO(
            entidad.getIdEntidad(),
            entidad.getNombre(),
            entidad.getAcronimo(),
            entidad.getDescripcion(),
            entidad.getDireccion(),
            entidad.getTelefono(),
            entidad.getEmail(),
            entidad.getEstado(),
            entidad.getActiva()
        );
    }
}