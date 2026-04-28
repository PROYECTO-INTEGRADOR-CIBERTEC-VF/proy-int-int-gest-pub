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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Long idDocumento;

    @NotBlank(message = "El nombre del archivo no puede estar vacio")
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Column(name = "tipo_archivo", length = 50)
    private String tipoArchivo;

    @Column(name = "tamanio")
    private Long tamanio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 30)
    private TipoDocumento tipoDocumento;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    @ManyToOne
    @JoinColumn(name = "id_solicitud")
    private Solicitud solicitud;

    @ManyToOne
    @JoinColumn(name = "id_respuesta")
    private Respuesta respuesta;

    @ManyToOne
    @JoinColumn(name = "id_apelacion")
    private Apelacion apelacion;

    @ManyToOne
    @JoinColumn(name = "id_resolucion")
    private Resolucion resolucion;

    @PrePersist
    protected void onCreate() {
        fechaSubida = LocalDateTime.now();
    }

    public enum TipoDocumento {
        SOLICITUD_ORIGINAL,
        RESPUESTA_ENTIDAD,
        RECURSO_APELACION,
        RESOLUCION_TTAIP,
        DOCUMENTO_ADJUNTO,
        SUBSANACION
    }

    public String getTamanioLegible() {
        if (tamanio == null) {
            return "N/D";
        }

        double size = tamanio.doubleValue();
        if (size < 1024) {
            return tamanio + " B";
        }

        size /= 1024.0;
        if (size < 1024) {
            return String.format(Locale.US, "%.1f KB", size);
        }

        size /= 1024.0;
        return String.format(Locale.US, "%.1f MB", size);
    }

    public Long getId() {
        return this.idDocumento;
    }
}
