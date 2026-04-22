package com.transparencia.api.model.dto;

import jakarta.validation.constraints.Size;

public record ActualizarSolicitudRequest(
    @Size(max = 500, message = "El asunto no puede exceder 500 caracteres")
    String asunto,
    String descripcion,

    @Size(max = 100, message = "El tipoInformacion no puede exceder 100 caracteres")
    String tipoInformacion,

    String prioridad,
    Boolean formatoDigital,
    Boolean formatoFisico,
    Boolean copiaSimple,
    Boolean copiaCertificada
) {
}
