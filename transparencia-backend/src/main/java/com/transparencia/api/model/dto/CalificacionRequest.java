package com.transparencia.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalificacionRequest {
    private String fundamentos;
    private String observaciones;
    private String detalles;
    private Integer diasSubsanacion;
}