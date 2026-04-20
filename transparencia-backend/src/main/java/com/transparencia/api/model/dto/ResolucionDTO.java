package com.transparencia.api.model.dto;

import com.transparencia.api.model.entity.Resolucion;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ResolucionDTO {
    private Long idResolucion;
    private String numeroResolucion;
    private String tipoResolucion;
    private String decision;
    private String fundamentos;
    private String observaciones;
    private LocalDateTime fechaResolucion;
    private Long miembroId;
    private String miembroNombre;

    public static ResolucionDTO from(Resolucion resolucion) {
        ResolucionDTO dto = new ResolucionDTO();
        dto.setIdResolucion(resolucion.getIdResolucion());
        dto.setNumeroResolucion(resolucion.getNumeroResolucion());
        dto.setTipoResolucion(resolucion.getTipoResolucion() != null ? resolucion.getTipoResolucion().name() : null);
        dto.setDecision(resolucion.getDecision() != null ? resolucion.getDecision().name() : null);
        dto.setFundamentos(resolucion.getFundamentos());
        dto.setObservaciones(resolucion.getObservaciones());
        dto.setFechaResolucion(resolucion.getFechaResolucion());

        if (resolucion.getMiembroTTAIP() != null) {
            dto.setMiembroId(resolucion.getMiembroTTAIP().getIdUsuario());
            dto.setMiembroNombre(resolucion.getMiembroTTAIP().getNombreCompleto());
        }

        return dto;
    }
}