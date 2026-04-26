package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.Documento;
import com.transparencia.api.model.dto.SegundaCalificacionDTO;
import com.transparencia.api.repository.ApelacionRepository;
import com.transparencia.api.repository.DocumentoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ApelacionService {

    private static final List<Apelacion.EstadoApelacion> ESTADOS_FINALES = List.of(
            Apelacion.EstadoApelacion.RESUELTO,
            Apelacion.EstadoApelacion.RESUELTO_FUNDADO,
            Apelacion.EstadoApelacion.RESUELTO_FUNDADO_EN_PARTE,
            Apelacion.EstadoApelacion.RESUELTO_INFUNDADO,
            Apelacion.EstadoApelacion.RESUELTO_INFUNDADO_EN_PARTE,
            Apelacion.EstadoApelacion.RESUELTO_IMPROCEDENTE,
            Apelacion.EstadoApelacion.TENER_POR_NO_PRESENTADO,
            Apelacion.EstadoApelacion.CONCLUSION_SUSTRACCION_MATERIA,
            Apelacion.EstadoApelacion.CONCLUSION_DESISTIMIENTO
    );

    private final ApelacionRepository apelacionRepository;
    private final DocumentoRepository documentoRepository;
    private final FileStorageService fileStorageService;

    //  inyecciones para soportar archivos
    public ApelacionService(ApelacionRepository apelacionRepository,
                            DocumentoRepository documentoRepository,
                            FileStorageService fileStorageService) {
        this.apelacionRepository = apelacionRepository;
        this.documentoRepository = documentoRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<Apelacion> obtenerTodasLasApelaciones() {
        return apelacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Apelacion> obtenerApelacionPorId(Long id) {
        return apelacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Apelacion> obtenerApelacionPorExpediente(String expediente) {
        return apelacionRepository.findByExpediente(expediente);
    }

    @Transactional(readOnly = true)
    public List<Apelacion> obtenerApelacionesPorEstado(Apelacion.EstadoApelacion estado) {
        return apelacionRepository.findByEstadoOrderByFechaApelacionDesc(estado);
    }

    @Transactional(readOnly = true)
    public long contarApelacionesPorEstado(Apelacion.EstadoApelacion estado) {
        return apelacionRepository.countByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Apelacion> findByCiudadanoId(Long ciudadanoId) {
        return apelacionRepository.findByCiudadano_IdUsuarioOrderByFechaApelacionDesc(ciudadanoId);
    }

    @Transactional(readOnly = true)
    public List<Apelacion> findPendientes() {
        return apelacionRepository.findPendientes(ESTADOS_FINALES);
    }

    @Transactional(readOnly = true)
    public List<Apelacion> findAll() {
        return apelacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Apelacion> findById(Long id) {
        return apelacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Apelacion> findByIdWithDetails(Long id) {
        return apelacionRepository.findByIdWithDetails(id);
    }

    @Transactional
    public Apelacion save(Apelacion apelacion) {
        return apelacionRepository.save(apelacion);
    }

    @Transactional(readOnly = true)
    public long count() {
        return apelacionRepository.count();
    }

    // MÉTODO PARA HU-07 (Segunda Calificación)

    @Transactional
    public Apelacion procesarSegundaCalificacion(Long idApelacion, SegundaCalificacionDTO request, MultipartFile archivoAdjunto) {
        Apelacion apelacion = apelacionRepository.findById(idApelacion)
                .orElseThrow(() -> new RuntimeException("Apelación no encontrada con ID: " + idApelacion));

        // La apelación debe estar en Segunda Calificación
        if (apelacion.getEstado() != Apelacion.EstadoApelacion.EN_CALIFICACION_2) {
            throw new IllegalStateException("La apelación no se encuentra en Segunda Calificación.");
        }

        // El archivo de resolución es obligatorio
        if (archivoAdjunto == null || archivoAdjunto.isEmpty()) {
            throw new IllegalArgumentException("Debe cargar el documento de resolución firmada para notificar.");
        }

        // Guardar el documento físicamente
        String rutaArchivoFisico = fileStorageService.storeFile(archivoAdjunto, "resoluciones");
        Documento documento = new Documento();
        documento.setNombreArchivo(archivoAdjunto.getOriginalFilename());
        documento.setRutaArchivo(rutaArchivoFisico);
        documento.setTipoDocumento(Documento.TipoDocumento.RESOLUCION_TTAIP);
        documento.setApelacion(apelacion);

        documentoRepository.save(documento);

        // Flujo parametrizado según decisión (HU-07 BE-02)
        if ("ADMISIBLE".equalsIgnoreCase(request.getDecision()) || "ADMITIDO".equalsIgnoreCase(request.getDecision())) {
            // Pasa a Calificación Final (En Resolución)
            apelacion.setEstado(Apelacion.EstadoApelacion.EN_RESOLUCION);
            apelacion.setResultado("ADMITIDO EN SEGUNDA CALIFICACIÓN");
            apelacion.setCalificacionSegunda(Apelacion.Calificacion.ADMISIBLE);

        } else if ("TENER_POR_NO_PRESENTADO".equalsIgnoreCase(request.getDecision()) || "NO_PRESENTADO".equalsIgnoreCase(request.getDecision())) {
            // El ciudadano no subsanó a tiempo
            apelacion.setEstado(Apelacion.EstadoApelacion.TENER_POR_NO_PRESENTADO);
            apelacion.setResultado("TENER POR NO PRESENTADO POR FALTA DE SUBSANACIÓN");

        } else if ("IMPROCEDENTE".equalsIgnoreCase(request.getDecision())) {
            // Cierre definitivo como Improcedente
            apelacion.setEstado(Apelacion.EstadoApelacion.RESUELTO_IMPROCEDENTE);
            apelacion.setResultado("RECHAZO DEFINITIVO - IMPROCEDENTE");
            apelacion.setCalificacionSegunda(Apelacion.Calificacion.IMPROCEDENTE);

        } else {
            throw new IllegalArgumentException("Decisión no válida. Solo se acepta ADMISIBLE, TENER_POR_NO_PRESENTADO o IMPROCEDENTE.");
        }

        // Guardar los fundamentos sin importar qué decisión se tomó
        apelacion.setFundamentos(request.getFundamentos());

        return apelacionRepository.save(apelacion);
    }
}