package com.transparencia.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.transparencia.api.model.entity.Solicitud;

import java.util.List;

@Schema(description = "Solicitud de Acceso a la Informacion Publica (SAIP)")
public record SolicitudDTO(
    Long id,
    @Schema(description = "Numero de solicitud", example = "SAIP-2025-00512")
    String expediente,
    @Schema(description = "Asunto de la solicitud")
    String asunto,
    String descripcion,
    @Schema(description = "Estado actual", example = "RECEPCIONADA", allowableValues = {"RECEPCIONADA", "EN_REVISION", "PENDIENTE_INFORMACION", "RESPONDIDA", "DENEGADA", "CONCLUIDA", "VENCIDA"})
    String estado,
    @Schema(description = "Nivel de prioridad", example = "MEDIA", allowableValues = {"BAJA", "NORMAL", "MEDIA", "ALTA", "URGENTE"})
    String prioridad,
    @Schema(description = "Semaforo de plazo", example = "VERDE", allowableValues = {"VERDE", "AMBAR", "ROJO", "NEGRO"})
    String semaforo,
    String fechaPresentacion,
    @Schema(description = "Fecha limite (10 dias habiles)")
    String fechaLimite,
    @Schema(description = "Dias habiles restantes")
    Integer diasRestantes,
    Long ciudadanoId,
    String ciudadanoNombre,
    String ciudadanoDni,
    Long entidadId,
    String entidadNombre,
    String entidadAcronimo,
    Boolean formatoDigital,
    Boolean formatoFisico,
    Boolean copiaSimple,
    Boolean copiaCertificada,
    String tipoInformacion,
    RespuestaDTO respuesta,
    List<DocumentoDTO> documentos
) {
    public static SolicitudDTO from(Solicitud s) {
        return new SolicitudDTO(
            s.getId(),
            s.getExpediente(),
            s.getAsunto(),
            s.getDescripcion(),
            s.getEstado() != null ? s.getEstado().name() : null,
            s.getPrioridad() != null ? s.getPrioridad().name() : null,
            s.getSemaforo(),
            s.getFechaPresentacion() != null ? s.getFechaPresentacion().toString() : null,
            s.getFechaLimite() != null ? s.getFechaLimite().toString() : null,
            s.getDiasRestantes(),
            s.getCiudadano() != null ? s.getCiudadano().getId() : null,
            s.getCiudadano() != null ? s.getCiudadano().getNombreCompleto() : null,
            s.getCiudadano() != null ? s.getCiudadano().getDni() : null,
            s.getEntidad() != null ? s.getEntidad().getId() : null,
            s.getEntidad() != null ? s.getEntidad().getNombre() : null,
            s.getEntidad() != null ? s.getEntidad().getAcronimo() : null,
            s.getFormatoDigital(),
            s.getFormatoFisico(),
            s.getCopiaSimple(),
            s.getCopiaCertificada(),
            s.getTipoInformacion(),
            s.getRespuesta() != null ? RespuestaDTO.from(s.getRespuesta()) : null,
            s.getDocumentos() != null ? s.getDocumentos().stream().map(DocumentoDTO::from).toList() : List.of()
        );
    }
}
