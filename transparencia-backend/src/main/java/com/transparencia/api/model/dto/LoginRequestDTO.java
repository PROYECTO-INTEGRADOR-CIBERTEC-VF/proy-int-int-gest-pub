package com.transparencia.api.model.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "El identificador es requerido")
    String identificador,

    @NotBlank(message = "La contrasena es requerida")
    String password
) {
}
