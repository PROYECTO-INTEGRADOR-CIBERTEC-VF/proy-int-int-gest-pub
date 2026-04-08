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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.transparencia.api.util.DiasHabilesUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitudes", indexes = {
    @Index(name = "idx_solicitud_expediente", columnList = "expediente", unique = true),
    @Index(name = "idx_solicitud_ciudadano", columnList = "id_ciudadano"),
    @Index(name = "idx_solicitud_entidad", columnList = "id_entidad"),
    @Index(name = "idx_solicitud_estado", columnList = "estado")
})
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    @Column(nullable = false, unique = true, length = 50)
    private String expediente;

    @NotBlank(message = "El asunto no puede estar vacio")
    @Column(nullable = false, length = 500)
    private String asunto;

    @NotBlank(message = "La descripcion no puede estar vacia")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El ciudadano no puede ser nulo")
    @ManyToOne
    @JoinColumn(name = "id_ciudadano", nullable = false)
    private Ciudadano ciudadano;

    @NotNull(message = "La entidad no puede ser nula")
    @ManyToOne
    @JoinColumn(name = "id_entidad", nullable = false)
    private Entidad entidad;

    @Column(name = "fecha_presentacion", nullable = false)
    private LocalDateTime fechaPresentacion;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSolicitud estado;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Prioridad prioridad;

    @Column(name = "formato_digital")
    private Boolean formatoDigital;

    @Column(name = "formato_fisico")
    private Boolean formatoFisico;

    @Column(name = "copia_simple")
    private Boolean copiaSimple;

    @Column(name = "copia_certificada")
    private Boolean copiaCertificada;

    @Column(name = "tipo_informacion", length = 100)
    private String tipoInformacion;

    @OneToOne(mappedBy = "solicitud", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Respuesta respuesta;

    @OneToMany(mappedBy = "solicitud", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Documento> documentos;

    @OneToOne(mappedBy = "solicitud", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Apelacion apelacion;

    @PrePersist
    protected void onCreate() {
        fechaPresentacion = LocalDateTime.now();
        fechaLimite = DiasHabilesUtil.sumarDiasHabiles(LocalDate.now(), 10);
        if (estado == null) {
            estado = EstadoSolicitud.RECEPCIONADA;
        }
        if (prioridad == null) {
            prioridad = Prioridad.MEDIA;
        }
    }

    public enum EstadoSolicitud {
        RECEPCIONADA,
        EN_REVISION,
        PENDIENTE_INFORMACION,
        RESPONDIDA,
        DENEGADA,
        CONCLUIDA,
        VENCIDA
    }

    public enum Prioridad {
        BAJA,
        NORMAL,
        MEDIA,
        ALTA,
        URGENTE
    }

    public Long getId() {
        return this.idSolicitud;
    }

    public int getDiasRestantes() {
        if (fechaLimite != null) {
            return DiasHabilesUtil.diasHabilesRestantes(fechaLimite);
        }
        return 0;
    }

    public boolean isSilencioAdministrativo() {
        return getDiasRestantes() < 0
            && estado != EstadoSolicitud.RESPONDIDA
            && estado != EstadoSolicitud.DENEGADA
            && estado != EstadoSolicitud.CONCLUIDA
            && estado != EstadoSolicitud.VENCIDA;
    }

    public String getSemaforo() {
        int dias = getDiasRestantes();
        if (dias < 0) {
            return "NEGRO";
        }
        if (dias <= 1) {
            return "ROJO";
        }
        if (dias <= 4) {
            return "AMBAR";
        }
        return "VERDE";
    }
}
