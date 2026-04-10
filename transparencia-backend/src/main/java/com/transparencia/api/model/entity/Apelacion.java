package com.transparencia.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "apelaciones")
public class Apelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_apelacion")
    private Long idApelacion;

    @Column(length = 50)
    private String expediente;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private EstadoApelacion estado;

    @Column(length = 100)
    private String resultado;

    @Column(name = "fecha_apelacion")
    private LocalDateTime fechaApelacion;

    @Column(name = "fecha_subsanacion")
    private LocalDateTime fechaSubsanacion;

    @Column(name = "dias_subsanacion")
    private Integer diasSubsanacion;

    @OneToOne
    @JoinColumn(name = "id_solicitud")
    private Solicitud solicitud;

    @OneToMany(mappedBy = "apelacion")
    private List<Documento> documentos;

    public enum EstadoApelacion {
        PENDIENTE_ELEVACION,
        EN_CALIFICACION_1,
        EN_SUBSANACION,
        EN_CALIFICACION_2,
        EN_RESOLUCION,
        RESUELTO
    }

    public Long getId() {
        return this.idApelacion;
    }
}

//apleacion
