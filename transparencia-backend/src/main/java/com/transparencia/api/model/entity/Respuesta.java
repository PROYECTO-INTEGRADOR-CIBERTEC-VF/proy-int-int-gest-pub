package com.transparencia.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "respuestas")
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta")
    private Long idRespuesta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_respuesta", length = 40)
    private TipoRespuesta tipoRespuesta;

    @Column(length = 50)
    private String decision;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "causal_denegatoria", length = 200)
    private String causalDenegatoria;

    @Column(name = "fundamento_legal", length = 300)
    private String fundamentoLegal;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @ManyToOne
    @JoinColumn(name = "id_funcionario")
    private Funcionario funcionario;

    @OneToOne
    @JoinColumn(name = "id_solicitud")
    private Solicitud solicitud;

    @OneToMany(mappedBy = "respuesta")
    private List<Documento> documentos;

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
