package com.transparencia.api.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.transparencia.api.model.entity.EstadoApelacion;
import com.transparencia.api.model.entity.Calificacion;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "apelaciones", indexes = {
    @Index(name = "idx_apelacion_expediente", columnList = "expediente", unique = true),
    @Index(name = "idx_apelacion_solicitud", columnList = "id_solicitud"),
    @Index(name = "idx_apelacion_ciudadano", columnList = "id_ciudadano"),
    @Index(name = "idx_apelacion_estado", columnList = "estado")
})
public class Apelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_apelacion")
    private Long idApelacion;

    @Column(nullable = false, unique = true, length = 50)
    private String expediente;

    @NotNull(message = "La solicitud no puede ser nula")
    @OneToOne
    @JoinColumn(name = "id_solicitud", nullable = false, unique = true)
    private Solicitud solicitud;

    @NotNull(message = "El ciudadano no puede ser nulo")
    @ManyToOne
    @JoinColumn(name = "id_ciudadano", nullable = false)
    private Ciudadano ciudadano;

    @NotBlank(message = "Los fundamentos no pueden estar vacíos")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String fundamentos;

    @Column(name = "fecha_apelacion", nullable = false)
    private LocalDateTime fechaApelacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoApelacion estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "calificacion_primera", length = 20)
    private Calificacion calificacionPrimera;

    @Enumerated(EnumType.STRING)
    @Column(name = "calificacion_segunda", length = 20)
    private Calificacion calificacionSegunda;

    @Column(name = "fecha_subsanacion")
    private LocalDate fechaSubsanacion;

    @Column(name = "dias_subsanacion")
    private Integer diasSubsanacion;

    @Column(length = 20)
    private String resultado;

    @OneToMany(mappedBy = "apelacion", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Documento> documentos;

    @OneToMany(mappedBy = "apelacion", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Resolucion> resoluciones;

    @PrePersist
    protected void onCreate() {
        fechaApelacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoApelacion.PENDIENTE_ELEVACION;
        }
    }

    // EstadoApelacion and Calificacion are defined in separate enum classes under the same package

    public Long getId() {
        return this.idApelacion;
    }

    public Resolucion getResolucion() {
        if (resoluciones != null && !resoluciones.isEmpty()) {
            return resoluciones.stream()
                    .filter(r -> r.getTipoResolucion() == Resolucion.TipoResolucion.RESOLUCION_FINAL)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public Resolucion getResolucionPrimeraCalificacion() {
        if (resoluciones != null && !resoluciones.isEmpty()) {
            return resoluciones.stream()
                    .filter(r -> r.getTipoResolucion() == Resolucion.TipoResolucion.PRIMERA_CALIFICACION)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public Resolucion getResolucionSegundaCalificacion() {
        if (resoluciones != null && !resoluciones.isEmpty()) {
            return resoluciones.stream()
                    .filter(r -> r.getTipoResolucion() == Resolucion.TipoResolucion.SEGUNDA_CALIFICACION)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Verifica si la apelación está en un estado resuelto.
     */
    public boolean isResuelta() {
        return estado == EstadoApelacion.RESUELTO ||
               estado == EstadoApelacion.RESUELTO_FUNDADO ||
               estado == EstadoApelacion.RESUELTO_FUNDADO_EN_PARTE ||
               estado == EstadoApelacion.RESUELTO_INFUNDADO ||
               estado == EstadoApelacion.RESUELTO_INFUNDADO_EN_PARTE ||
               estado == EstadoApelacion.RESUELTO_IMPROCEDENTE ||
               estado == EstadoApelacion.TENER_POR_NO_PRESENTADO ||
               estado == EstadoApelacion.CONCLUSION_SUSTRACCION_MATERIA ||
               estado == EstadoApelacion.CONCLUSION_DESISTIMIENTO;
    }
}
