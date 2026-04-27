package com.transparencia.api.model.dto;

import com.transparencia.api.model.entity.Apelacion;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApelacionDTO {
    private Long idApelacion;
    private String expediente;
    private String estado;
    private String resultado;
    private LocalDateTime fechaApelacion;
    private LocalDateTime fechaSubsanacion;
    private Integer diasSubsanacion;
    private Long ciudadanoId;
    private String ciudadanoNombre;
    private Long solicitudId;
    private String solicitudExpediente;
    private String entidadNombre;

    public static ApelacionDTO from(Apelacion apelacion) {
        ApelacionDTO dto = new ApelacionDTO();
        dto.setIdApelacion(apelacion.getIdApelacion());
        dto.setExpediente(apelacion.getExpediente());
        dto.setEstado(apelacion.getEstado() != null ? apelacion.getEstado().name() : null);
        dto.setResultado(apelacion.getResultado());
        dto.setFechaApelacion(apelacion.getFechaApelacion());
        dto.setFechaSubsanacion(apelacion.getFechaSubsanacion());
        dto.setDiasSubsanacion(apelacion.getDiasSubsanacion());

        // Mapeo del Ciudadano
        if (apelacion.getCiudadano() != null) {
            dto.setCiudadanoId(apelacion.getCiudadano().getIdUsuario());
            dto.setCiudadanoNombre(apelacion.getCiudadano().getNombreCompleto());
        }

        // Mapeo de la Solicitud
        if (apelacion.getSolicitud() != null) {
            dto.setSolicitudId(apelacion.getSolicitud().getIdSolicitud());
            dto.setSolicitudExpediente(apelacion.getSolicitud().getExpediente());
        }

        // --- LÓGICA PARA LA ENTIDAD ---
        if (apelacion.getEntidad() != null) {
            dto.setEntidadNombre(apelacion.getEntidad().getNombre());
        } else {
            dto.setEntidadNombre("Sin Entidad");
        }
        // ------------------------------------

        return dto;
    }
}