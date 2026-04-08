package com.transparencia.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.transparencia.api.model.entity.Documento;

@Schema(description = "Documento adjunto a solicitud, respuesta, apelacion o resolucion")
public record DocumentoDTO(
    Long id,
    String nombreArchivo,
    String rutaArchivo,
    String tipoArchivo,
    Long tamanio,
    String tipoDocumento,
    String fechaSubida
) {
    public static DocumentoDTO from(Documento d) {
        return new DocumentoDTO(
            d.getId(),
            d.getNombreArchivo(),
            d.getRutaArchivo(),
            d.getTipoArchivo(),
            d.getTamanio(),
            d.getTipoDocumento() != null ? d.getTipoDocumento().name() : null,
            d.getFechaSubida() != null ? d.getFechaSubida().toString() : null
        );
    }
}
