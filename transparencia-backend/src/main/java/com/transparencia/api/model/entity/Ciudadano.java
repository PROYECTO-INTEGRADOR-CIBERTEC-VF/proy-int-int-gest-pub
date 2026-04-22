package com.transparencia.api.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ciudadanos")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Ciudadano extends Usuario {

    @NotBlank(message = "El DNI no puede estar vacio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @NotBlank(message = "El nombre completo no puede estar vacio")
    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    @Column(length = 15)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @OneToMany(mappedBy = "ciudadano", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Solicitud> solicitudes;

    @Column(length = 200)
    private String email;

    public Long getId() {
        return this.getIdUsuario();
    }

    public String getNombres() {
        return this.nombreCompleto;
    }
}
