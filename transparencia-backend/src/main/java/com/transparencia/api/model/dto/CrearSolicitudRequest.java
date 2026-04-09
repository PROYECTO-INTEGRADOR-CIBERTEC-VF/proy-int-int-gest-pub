package com.transparencia.api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearSolicitudRequest(
    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 500, message = "El asunto no puede exceder 500 caracteres")
    String asunto,

    @NotBlank(message = "La descripcion es obligatoria")
    String descripcion,

    @NotNull(message = "El ciudadanoId es obligatorio")
    Long ciudadanoId,

    @NotNull(message = "El entidadId es obligatorio")
    Long entidadId,

    Boolean formatoDigital,
    Boolean formatoFisico,
    Boolean copiaSimple,
    Boolean copiaCertificada,

    @Size(max = 100, message = "El tipoInformacion no puede exceder 100 caracteres")
    String tipoInformacion
) {
}
