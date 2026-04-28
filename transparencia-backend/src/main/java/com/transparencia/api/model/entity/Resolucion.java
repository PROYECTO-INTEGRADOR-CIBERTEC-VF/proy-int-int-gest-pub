package com.transparencia.api.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resoluciones", indexes = {
    @Index(name = "idx_resolucion_apelacion", columnList = "id_apelacion"),
    @Index(name = "idx_resolucion_miembro", columnList = "id_miembro_ttaip")
})
public class Resolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resolucion")
    private Long idResolucion;

    @NotNull(message = "La apelación no puede ser nula")
    @ManyToOne
    @JoinColumn(name = "id_apelacion", nullable = false)
    private Apelacion apelacion;

    @ManyToOne
    @JoinColumn(name = "id_miembro_ttaip")
    private MiembroTTAIP miembroTTAIP;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_resolucion", nullable = false, length = 30)
    private TipoResolucion tipoResolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DecisionResolucion decision;

    @NotBlank(message = "Los fundamentos no pueden estar vacíos")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String fundamentos;

    @Column(name = "fundamento_legal", columnDefinition = "TEXT")
    private String fundamentoLegal;

    @Column(name = "fecha_resolucion", nullable = false)
    private LocalDateTime fechaResolucion;

    @Column(name = "numero_resolucion", unique = true, length = 50)
    private String numeroResolucion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "causal_fundado", length = 500)
    private String causalFundado;

    @Column(length = 20)
    private String resultado;

    @Column(name = "iniciar_proceso_disciplinario")
    private Boolean iniciarProcesoDisciplinario;

    @OneToMany(mappedBy = "resolucion", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Documento> documentos;

    @PrePersist
    protected void onCreate() {
        fechaResolucion = LocalDateTime.now();
    }

    public enum TipoResolucion {
        PRIMERA_CALIFICACION,
        SEGUNDA_CALIFICACION,
        RESOLUCION_FINAL
    }

    public enum DecisionResolucion {
        ADMISIBLE,
        INADMISIBLE,
        IMPROCEDENTE,
        ADMITIDO,
        TENER_POR_NO_PRESENTADO,
        FUNDADO,
        FUNDADO_EN_PARTE,
        INFUNDADO,
        INFUNDADO_EN_PARTE,
        CONCLUSION_SUSTRACCION_MATERIA,
        CONCLUSION_DESISTIMIENTO
    }

    public Long getId() {
        return this.idResolucion;
    }
}
