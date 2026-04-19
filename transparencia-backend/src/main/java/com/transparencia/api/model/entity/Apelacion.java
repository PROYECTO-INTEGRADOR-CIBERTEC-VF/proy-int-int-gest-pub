package com.transparencia.api.model.entity;

import jakarta.persistence.*;
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

    @Column(length = 50, unique = true)
    private String expediente;

    @OneToOne
    @JoinColumn(name = "id_solicitud", unique = true)
    private Solicitud solicitud;

    @ManyToOne
    @JoinColumn(name = "id_ciudadano")
    private Ciudadano ciudadano;

    @Column(columnDefinition = "TEXT")
    private String fundamentos;

    @OneToMany(mappedBy = "apelacion")
    private List<Documento> documentos;

    @OneToMany(mappedBy = "apelacion")
    private List<Resolucion> resoluciones;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private EstadoApelacion estado;

    @Enumerated(EnumType.STRING)
    private Calificacion calificacionPrimera;

    @Enumerated(EnumType.STRING)
    private Calificacion calificacionSegunda;

    private String resultado;
    private LocalDateTime fechaApelacion;
    private LocalDateTime fechaSubsanacion;
    private Integer diasSubsanacion;

    public enum EstadoApelacion {
        PENDIENTE_ELEVACION, EN_CALIFICACION_1, EN_SUBSANACION, EN_CALIFICACION_2, 
        NOTIFICACION_SEGUNDA_CALIFICACION, EN_RESOLUCION, RESUELTO, TENER_POR_NO_PRESENTADO, 
        CONCLUSION_SUSTRACCION_MATERIA, CONCLUSION_DESISTIMIENTO, RESUELTO_FUNDADO, 
        RESUELTO_FUNDADO_EN_PARTE, RESUELTO_INFUNDADO, RESUELTO_INFUNDADO_EN_PARTE, 
        RESUELTO_IMPROCEDENTE
    }

    public enum Calificacion {
        ADMISIBLE, INADMISIBLE, IMPROCEDENTE, ADMITIDO
    }

    @PrePersist
    protected void onCreate() {
        this.fechaApelacion = LocalDateTime.now();
        if (this.estado == null) this.estado = EstadoApelacion.PENDIENTE_ELEVACION;
    }

    public boolean isResuelta() {
        return this.estado != null && (this.estado == EstadoApelacion.RESUELTO || 
               this.estado.name().startsWith("RESUELTO_"));
    }

    public Long getId() {
        return this.idApelacion;
    }
}
