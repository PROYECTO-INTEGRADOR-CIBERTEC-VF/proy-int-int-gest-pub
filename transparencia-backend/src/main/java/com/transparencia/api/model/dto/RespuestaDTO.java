package com.transparencia.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.transparencia.api.model.entity.Respuesta;

import java.util.List;

@Schema(description = "Respuesta de funcionario a solicitud SAIP")
public record RespuestaDTO(
    Long id,
    @Schema(description = "Tipo de respuesta", example = "ENTREGA_TOTAL", allowableValues = {"ENTREGA_TOTAL", "ENTREGA_PARCIAL", "DENEGACION_TOTAL", "SILENCIO_ADMINISTRATIVO"})
    String tipoRespuesta,
    String decision,
    String contenido,
    String causalDenegatoria,
    String fundamentoLegal,
    String fechaRespuesta,
    Long funcionarioId,
    String funcionarioNombre,
    List<DocumentoDTO> documentos
) {
    public static RespuestaDTO from(Respuesta r) {
        return new RespuestaDTO(
            r.getId(),
            r.getTipoRespuesta() != null ? r.getTipoRespuesta().name() : null,
            r.getDecision(),
            r.getContenido(),
            r.getCausalDenegatoria(),
            r.getFundamentoLegal(),
            r.getFechaRespuesta() != null ? r.getFechaRespuesta().toString() : null,
            r.getFuncionario() != null ? r.getFuncionario().getId() : null,
            r.getFuncionario() != null ? r.getFuncionario().getNombreCompleto() : null,
            r.getDocumentos() != null ? r.getDocumentos().stream().map(DocumentoDTO::from).toList() : List.of()
        );
    }
}
