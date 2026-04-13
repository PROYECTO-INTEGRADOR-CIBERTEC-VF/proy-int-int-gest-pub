package com.transparencia.api.model.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ResolucionDTO {
    private Long idResolucion;
    private String numeroResolucion;
    private String tipoResolucion;
    private String decision;
    private LocalDateTime fechaResolucion;
    private String miembroNombre;
}