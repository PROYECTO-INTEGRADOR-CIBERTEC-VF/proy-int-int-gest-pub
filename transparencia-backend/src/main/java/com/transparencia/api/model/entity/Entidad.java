package com.transparencia.api.model.entity;

import com.transparencia.api.model.entity.Solicitud;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "entidades", indexes = {
    @Index(name = "idx_entidad_acronimo", columnList = "acronimo", unique = true)
})
public class Entidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entidad")
    private Long idEntidad;

    @NotBlank(message = "El nombre de la entidad no puede estar vacio")
    @Column(nullable = false, length = 200)
    private String nombre;

    @NotBlank(message = "El acronimo no puede estar vacio")
    @Column(nullable = false, unique = true, length = 20)
    private String acronimo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private Boolean activa;

    @Column(length = 300)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String estado;

    @Transient
    private long solicitudesAsignadas;

    @Transient
    private long pendientes;

    @Transient
    private long respondidas;

    @Transient
    private long vencidas;

    @Transient
    private int tasaCumplimiento;

    @OneToMany(mappedBy = "entidad", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Funcionario> funcionarios;

    @OneToMany(mappedBy = "entidad", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Solicitud> solicitudes;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (activa == null) {
            activa = true;
        }
        if (estado == null) {
            estado = "Activa";
        }
    }

    public Long getId() {
        return this.idEntidad;
    }
}
