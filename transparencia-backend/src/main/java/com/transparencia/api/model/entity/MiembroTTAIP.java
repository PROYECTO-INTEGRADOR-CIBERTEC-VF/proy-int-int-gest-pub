package com.transparencia.api.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "miembros_ttaip")
@Data
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "id_usuario")
public class MiembroTTAIP extends Usuario {
    private String nombreCompleto;
    private String cargo;
    private String sala;
    private String especialidad;
}