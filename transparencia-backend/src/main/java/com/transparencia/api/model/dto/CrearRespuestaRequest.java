package com.transparencia.api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearRespuestaRequest(
    @NotNull(message = "El ID de la solicitud es obligatorio")
    Long solicitudId,

    @NotNull(message = "El ID del funcionario es obligatorio")
    Long funcionarioId,

    @NotBlank(message = "El tipo de respuesta es obligatorio")
    String tipoRespuesta,

    @NotBlank(message = "El contenido es obligatorio")
    @Size(max = 5000, message = "El contenido no puede exceder 5000 caracteres")
    String contenido,

    @Size(max = 500, message = "La causal denegatoria no puede exceder 500 caracteres")
    String causalDenegatoria,

    @Size(max = 2000, message = "El fundamento legal no puede exceder 2000 caracteres")
    String fundamentoLegal,

    @Size(max = 100, message = "El formato de entrega no puede exceder 100 caracteres")
    String formatoEntrega,

    @Positive(message = "El plazo de entrega debe ser mayor a cero")
    Integer plazoEntrega
) {
}
