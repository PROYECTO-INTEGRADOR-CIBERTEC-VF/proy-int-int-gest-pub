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
@Table(name = "resoluciones")
public class Resolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resolucion")
    private Long idResolucion;

    @ManyToOne
    @JoinColumn(name = "id_apelacion")
    private Apelacion apelacion;

    @ManyToOne
    @JoinColumn(name = "id_miembro_ttaip")
    private MiembroTTAIP miembroTTAIP;

    @Column(unique = true, length = 50)
    private String numeroResolucion;

    @Column(columnDefinition = "TEXT")
    private String fundamentos;

    @Column(columnDefinition = "TEXT")
    private String fundamentoLegal;

    private LocalDateTime fechaResolucion;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(length = 500)
    private String causalFundado;

    @Column(length = 20)
    private String resultado;

    private Boolean iniciarProcesoDisciplinario;

    @OneToMany(mappedBy = "resolucion")
    private List<Documento> documentos;

    @Enumerated(EnumType.STRING)
    private TipoResolucion tipoResolucion;

    @Enumerated(EnumType.STRING)
    private DecisionResolucion decision;

    public enum TipoResolucion {
        PRIMERA_CALIFICACION, SEGUNDA_CALIFICACION, RESOLUCION_FINAL
    }

    public enum DecisionResolucion {
        ADMISIBLE, INADMISIBLE, IMPROCEDENTE, ADMITIDO, TENER_POR_NO_PRESENTADO, 
        FUNDADO, FUNDADO_EN_PARTE, INFUNDADO, INFUNDADO_EN_PARTE, 
        CONCLUSION_SUSTRACCION_MATERIA, CONCLUSION_DESISTIMIENTO
    }

    public Long getId() {
        return this.idResolucion;
    }
}
