package com.transparencia.api.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequestDTO(
    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email invalido")
    String email,

    @NotBlank(message = "La contrasena es requerida")
    @Size(min = 6, max = 100, message = "La contrasena debe tener entre 6 y 100 caracteres")
    String password,

    @NotBlank(message = "El DNI es requerido")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 digitos numericos")
    String dni,

    @NotBlank(message = "El nombre completo es requerido")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    String nombreCompleto,

    @Size(max = 15, message = "El telefono no puede exceder 15 caracteres")
    String telefono,

    @Size(max = 300, message = "La direccion no puede exceder 300 caracteres")
    String direccion
) {
}
