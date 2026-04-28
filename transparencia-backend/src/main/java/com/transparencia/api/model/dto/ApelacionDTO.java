package com.transparencia.api.model.dto;

import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.Resolucion;
import java.time.LocalDateTime;
import java.util.List;
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
    private String solicitudAsunto;
    private String entidadNombre;
    private String fundamentos;
    private String calificacionPrimera;
    private String calificacionSegunda;
    private ResolucionDTO resolucionPrimeraCalificacion;
    private ResolucionDTO resolucionSegundaCalificacion;
    private ResolucionDTO resolucion;

    public static ApelacionDTO from(Apelacion apelacion) {
        ApelacionDTO dto = new ApelacionDTO();
        dto.setIdApelacion(apelacion.getIdApelacion());
        dto.setExpediente(apelacion.getExpediente());
        dto.setEstado(apelacion.getEstado() != null ? apelacion.getEstado().name() : null);
        dto.setResultado(apelacion.getResultado());
        dto.setFechaApelacion(apelacion.getFechaApelacion());
        dto.setFechaSubsanacion(apelacion.getFechaSubsanacion());
        dto.setDiasSubsanacion(apelacion.getDiasSubsanacion());
        dto.setFundamentos(apelacion.getFundamentos());
        dto.setCalificacionPrimera(apelacion.getCalificacionPrimera() != null ? apelacion.getCalificacionPrimera().name() : null);
        dto.setCalificacionSegunda(apelacion.getCalificacionSegunda() != null ? apelacion.getCalificacionSegunda().name() : null);

        if (apelacion.getCiudadano() != null) {
            dto.setCiudadanoId(apelacion.getCiudadano().getIdUsuario());
            dto.setCiudadanoNombre(apelacion.getCiudadano().getNombreCompleto());
        }

        if (apelacion.getSolicitud() != null) {
            dto.setSolicitudId(apelacion.getSolicitud().getIdSolicitud());
            dto.setSolicitudExpediente(apelacion.getSolicitud().getExpediente());
            dto.setSolicitudAsunto(apelacion.getSolicitud().getAsunto());

            if (apelacion.getSolicitud().getEntidad() != null) {
                dto.setEntidadNombre(apelacion.getSolicitud().getEntidad().getNombre());
            }
        }

        // Mapear resoluciones por tipo
        if (apelacion.getResoluciones() != null) {
            for (Resolucion resolucion : apelacion.getResoluciones()) {
                if (resolucion.getTipoResolucion() == Resolucion.TipoResolucion.PRIMERA_CALIFICACION) {
                    dto.setResolucionPrimeraCalificacion(ResolucionDTO.from(resolucion));
                } else if (resolucion.getTipoResolucion() == Resolucion.TipoResolucion.SEGUNDA_CALIFICACION) {
                    dto.setResolucionSegundaCalificacion(ResolucionDTO.from(resolucion));
                } else if (resolucion.getTipoResolucion() == Resolucion.TipoResolucion.RESOLUCION_FINAL) {
                    dto.setResolucion(ResolucionDTO.from(resolucion));
                }
            }
        }

        return dto;
    }
}