package com.transparencia.api.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ApelacionInfoDTO {
    private Long idApelacion;
    private String expediente;
    private String fundamentos;
    private LocalDateTime fechaApelacion;
    private String estado;
    private String resultado;
    private List<ResolucionDTO> resoluciones;
}