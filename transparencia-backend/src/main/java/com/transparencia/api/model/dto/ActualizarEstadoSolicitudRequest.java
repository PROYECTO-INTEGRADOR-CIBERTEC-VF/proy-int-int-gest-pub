package com.transparencia.api.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarEstadoSolicitudRequest(
    @NotBlank(message = "El estado es obligatorio")
    String estado
) {
}
