package com.transparencia.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "miembros_ttaip")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class MiembroTTAIP extends Usuario {

    @NotBlank(message = "El nombre completo no puede estar vacio")
    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    @Column(length = 100)
    private String cargo;

    @Column(length = 30)
    private String sala;

    @Column(length = 500)
    private String especialidad;

    public Long getId() {
        return this.getIdUsuario();
    }
}
