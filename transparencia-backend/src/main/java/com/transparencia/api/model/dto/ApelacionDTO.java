package com.transparencia.api.model.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApelacionDTO {
    private Long idApelacion;
    private String expediente;
    private String estado;
    private LocalDateTime fechaApelacion;
    private String ciudadanoNombre;
    private String solicitudExpediente;
}