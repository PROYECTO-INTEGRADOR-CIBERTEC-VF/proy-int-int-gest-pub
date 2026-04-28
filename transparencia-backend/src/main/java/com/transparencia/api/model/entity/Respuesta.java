package com.transparencia.api.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "respuestas", indexes = {
    @Index(name = "idx_respuesta_solicitud", columnList = "id_solicitud"),
    @Index(name = "idx_respuesta_funcionario", columnList = "id_funcionario")
})
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta")
    private Long idRespuesta;

    @NotNull(message = "La solicitud no puede ser nula")
    @OneToOne
    @JoinColumn(name = "id_solicitud", nullable = false, unique = true)
    private Solicitud solicitud;

    @NotNull(message = "El funcionario no puede ser nulo")
    @ManyToOne
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Funcionario funcionario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_respuesta", length = 30)
    private TipoRespuesta tipoRespuesta;

    @Column(length = 20)
    private String decision;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "causal_denegatoria", length = 500)
    private String causalDenegatoria;

    @Column(name = "fundamento_legal", columnDefinition = "TEXT")
    private String fundamentoLegal;

    @Column(name = "fecha_respuesta", nullable = false)
    private LocalDateTime fechaRespuesta;

    @OneToMany(mappedBy = "respuesta", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Documento> documentos;

    @PrePersist
    protected void onCreate() {
        if (fechaRespuesta == null) {
            fechaRespuesta = LocalDateTime.now();
        }
    }

    public enum TipoRespuesta {
        ENTREGA_TOTAL,
        ENTREGA_PARCIAL,
        DENEGACION_TOTAL,
        SILENCIO_ADMINISTRATIVO
    }

    public Long getId() {
        return this.idRespuesta;
    }
}
