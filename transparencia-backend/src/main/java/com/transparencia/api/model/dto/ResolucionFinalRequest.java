package com.transparencia.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResolucionFinalRequest {
    private String decision;
    private String fundamentos;
    private Boolean iniciarProcesoDisciplinario;
}