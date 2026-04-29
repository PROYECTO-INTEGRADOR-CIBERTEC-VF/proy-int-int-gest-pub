package com.transparencia.api.service;

import com.transparencia.api.exception.RecursoNoEncontradoException;
import com.transparencia.api.util.DiasHabilesUtil;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.Ciudadano;
import com.transparencia.api.model.entity.Documento;
import com.transparencia.api.model.entity.Solicitud;
import com.transparencia.api.model.dto.SegundaCalificacionDTO;
import com.transparencia.api.repository.ApelacionRepository;
import com.transparencia.api.repository.DocumentoRepository;
import com.transparencia.api.repository.CiudadanoRepository;
import com.transparencia.api.repository.SolicitudRepository;

import java.time.LocalDateTime;
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
    private final CiudadanoRepository ciudadanoRepository;
    private final SolicitudRepository solicitudRepository;

    public ApelacionService(ApelacionRepository apelacionRepository,
                            DocumentoRepository documentoRepository,
                            FileStorageService fileStorageService,
                            CiudadanoRepository ciudadanoRepository,
                            SolicitudRepository solicitudRepository) {
        this.apelacionRepository = apelacionRepository;
        this.documentoRepository = documentoRepository;
        this.fileStorageService = fileStorageService;
        this.ciudadanoRepository = ciudadanoRepository;
        this.solicitudRepository = solicitudRepository;
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
        if (!StringUtils.hasText(apelacion.getExpediente())) {
            apelacion.setExpediente(generarExpedienteTTAIP());
        }
        return apelacionRepository.save(apelacion);
    }

    @Transactional(readOnly = true)
    public long count() {
        return apelacionRepository.count();
    }

    /**
     * Genera un expediente TTAIP unico con formato NNNNN-AAAA-JUS/TTAIP
     */
    public String generarExpedienteTTAIP() {
        int year = LocalDate.now().getYear();
        String suffix = "%-" + year + "-JUS/TTAIP";
        Integer maxNumber = apelacionRepository.findMaxExpedienteNumber(suffix);
        int siguienteNumero = (maxNumber == null ? 0 : maxNumber) + 1;
        return String.format("%05d", siguienteNumero) + "-" + year + "-JUS/TTAIP";
    }

    /**
     * Crea una apelacion desde solicitud. Valida que la solicitud tenga respuesta
     * y que no exista ya una apelacion para ella.
     */
    @Transactional
    public Apelacion crearApelacion(Long solicitudId, Long ciudadanoId, String fundamentos) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada con ID: " + solicitudId));

        // Validar que la solicitud tenga una respuesta (respondida, denegada o vencida)
        boolean puedeApelar = solicitud.getEstado() == Solicitud.EstadoSolicitud.RESPONDIDA
            || solicitud.getEstado() == Solicitud.EstadoSolicitud.DENEGADA
            || solicitud.getEstado() == Solicitud.EstadoSolicitud.VENCIDA;
        if (!puedeApelar) {
            throw new IllegalArgumentException(
                "Solo se puede apelar una solicitud con estado RESPONDIDA, DENEGADA o VENCIDA. Estado actual: " + solicitud.getEstado()
            );
        }

        // Validar que no exista ya una apelacion para esta solicitud
        if (apelacionRepository.findBySolicitud_IdSolicitud(solicitudId).isPresent()) {
            throw new IllegalArgumentException("Ya existe una apelacion para esta solicitud");
        }

        Ciudadano ciudadano = ciudadanoRepository.findById(ciudadanoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ciudadano no encontrado con ID: " + ciudadanoId));

        Apelacion apelacion = new Apelacion();
        apelacion.setSolicitud(solicitud);
        apelacion.setCiudadano(ciudadano);
        apelacion.setFundamentos(fundamentos);
        apelacion.setEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_1);
        apelacion.setFechaApelacion(LocalDateTime.now());

        return save(apelacion);
    }

    /**
     * Subsanar una apelacion que esta en EN_SUBSANACION.
     * Valida plazo de 2 dias habiles.
     */
    @Transactional
    public Apelacion subsanar(Long apelacionId, String fundamentosAdicionales) {
        Apelacion apelacion = apelacionRepository.findById(apelacionId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Apelacion no encontrada con ID: " + apelacionId));

        if (apelacion.getEstado() != Apelacion.EstadoApelacion.EN_SUBSANACION) {
            throw new IllegalStateException(
                "La apelacion debe estar en estado EN_SUBSANACION para poder subsanar. Estado actual: " + apelacion.getEstado()
            );
        }

        // Validar plazo de subsanacion (2 dias habiles por BR-TTAIP-004)
        if (apelacion.getFechaSubsanacion() != null) {
            int diasSubsanacionPermitidos = apelacion.getDiasSubsanacion() != null ? apelacion.getDiasSubsanacion() : 2;
            int diasTranscurridos = DiasHabilesUtil.contarDiasHabiles(
                apelacion.getFechaSubsanacion().toLocalDate(),
                LocalDate.now()
            );
            if (diasTranscurridos > diasSubsanacionPermitidos) {
                throw new IllegalStateException(
                    "El plazo de " + diasSubsanacionPermitidos + " dias habiles para subsanar ha vencido"
                );
            }
        }

        // Actualizar con los fundamentos adicionales y pasar a segunda calificacion
        String fundamentosOriginales = apelacion.getFundamentos() != null ? apelacion.getFundamentos() : "";
        apelacion.setFundamentos(fundamentosOriginales + "\n\n--- SUBSANACION ---\n" + fundamentosAdicionales);
        apelacion.setEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_2);

        return apelacionRepository.save(apelacion);
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

        // Validar plazo de 7 días hábiles
        LocalDate fechaInicio = apelacion.getFechaSubsanacion() != null
                ? apelacion.getFechaSubsanacion().toLocalDate()
                : apelacion.getFechaApelacion().toLocalDate();

        int diasTranscurridos = DiasHabilesUtil.contarDiasHabiles(fechaInicio, LocalDate.now());

        // Si se pasó del plazo, se crea una etiqueta de advertencia para la auditoría
        String sufijoPlazo = "";
        if (diasTranscurridos > 7) {
            sufijoPlazo = " (ALERTA: FUERA DE PLAZO REGLAMENTARIO - " + diasTranscurridos + " días)";
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

        // Flujo parametrizado según decisión
        if ("ADMISIBLE".equalsIgnoreCase(request.getDecision()) || "ADMITIDO".equalsIgnoreCase(request.getDecision())) {
            // Pasa a estado de Notificación
            apelacion.setEstado(Apelacion.EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION);
            apelacion.setResultado("ADMITIDO EN SEGUNDA CALIFICACIÓN" + sufijoPlazo);
            apelacion.setCalificacionSegunda(Apelacion.Calificacion.ADMISIBLE);

        } else if ("TENER_POR_NO_PRESENTADO".equalsIgnoreCase(request.getDecision()) || "NO_PRESENTADO".equalsIgnoreCase(request.getDecision())) {
            // El ciudadano no subsanó a tiempo
            apelacion.setEstado(Apelacion.EstadoApelacion.TENER_POR_NO_PRESENTADO);
            apelacion.setResultado("TENER POR NO PRESENTADO POR FALTA DE SUBSANACIÓN" + sufijoPlazo);

        } else if ("IMPROCEDENTE".equalsIgnoreCase(request.getDecision())) {
            // Cierre definitivo como Improcedente
            apelacion.setEstado(Apelacion.EstadoApelacion.RESUELTO_IMPROCEDENTE);
            apelacion.setResultado("RECHAZO DEFINITIVO - IMPROCEDENTE" + sufijoPlazo);
            apelacion.setCalificacionSegunda(Apelacion.Calificacion.IMPROCEDENTE);

        } else {
            throw new IllegalArgumentException("Decisión no válida. Solo se acepta ADMISIBLE, TENER_POR_NO_PRESENTADO o IMPROCEDENTE.");
        }

        // Guardar los fundamentos sin importar qué decisión se tomó
        apelacion.setFundamentos(request.getFundamentos());

        return apelacionRepository.save(apelacion);
    }

    // MÉTODO EXTRA: Para confirmar la notificación y pasar a Resolución Final (BE-02)
    @Transactional
    public Apelacion confirmarNotificacionSegundaCalificacion(Long idApelacion) {
        Apelacion apelacion = apelacionRepository.findById(idApelacion)
                .orElseThrow(() -> new RuntimeException("Apelación no encontrada con ID: " + idApelacion));

        if (apelacion.getEstado() != Apelacion.EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION) {
            throw new IllegalStateException("La apelación no está pendiente de notificación.");
        }

        // Avanza la etapa de resolución
        apelacion.setEstado(Apelacion.EstadoApelacion.EN_RESOLUCION);
        return apelacionRepository.save(apelacion);
    }

    // --- MÉTODO PARA HU-08: EMITIR FALLO FINAL CON 7 DECISIONES ---
    @Transactional
    public Apelacion emitirResolucionFinal(Long idApelacion, com.transparencia.api.model.dto.ResolucionFinalRequest request, MultipartFile archivoAdjunto) {
        Apelacion apelacion = apelacionRepository.findById(idApelacion)
                .orElseThrow(() -> new RuntimeException("Apelación no encontrada con ID: " + idApelacion));

        if (apelacion.getEstado() != Apelacion.EstadoApelacion.EN_RESOLUCION) {
            throw new IllegalStateException("La apelación debe estar en etapa de EN_RESOLUCION para emitir un fallo.");
        }

        if (archivoAdjunto == null || archivoAdjunto.isEmpty()) {
            throw new IllegalArgumentException("Debe cargar el documento de resolución firmada (PDF).");
        }

        // --- BE-02: Validar plazo máximo de 10 días hábiles ---
        LocalDate fechaInicio = apelacion.getFechaApelacion().toLocalDate();
        if (apelacion.getFechaSubsanacion() != null) {
            fechaInicio = apelacion.getFechaSubsanacion().toLocalDate();
        }

        int diasTranscurridos = DiasHabilesUtil.contarDiasHabiles(fechaInicio, LocalDate.now());
        String sufijoPlazo = "";
        if (diasTranscurridos > 10) {
            sufijoPlazo = " (ALERTA: FUERA DE PLAZO - Emitido " + diasTranscurridos + " días después)";
        }

        // 1. Guardar el documento físico
        String rutaArchivoFisico = fileStorageService.storeFile(archivoAdjunto, "resoluciones_finales");
        Documento documento = new Documento();
        documento.setNombreArchivo(archivoAdjunto.getOriginalFilename());
        documento.setRutaArchivo(rutaArchivoFisico);
        documento.setTipoDocumento(Documento.TipoDocumento.RESOLUCION_TTAIP);
        documento.setApelacion(apelacion);
        documentoRepository.save(documento);

        // 2. Mapear las 7 decisiones y sus ESTADOS EXACTOS (Para cumplir GitHub BE-03)
        String decisionNormalizada = request.getDecision().toLowerCase();
        String textoResultado = "";

        switch (decisionNormalizada) {
            case "fundado":
                apelacion.setEstado(Apelacion.EstadoApelacion.RESUELTO_FUNDADO);
                textoResultado = "RESUELTO - FUNDADO";
                break;
            case "fundado_en_parte":
                apelacion.setEstado(Apelacion.EstadoApelacion.RESUELTO_FUNDADO_EN_PARTE);
                textoResultado = "RESUELTO - FUNDADO EN PARTE";
                break;
            case "infundado":
                apelacion.setEstado(Apelacion.EstadoApelacion.RESUELTO_INFUNDADO);
                textoResultado = "RESUELTO - INFUNDADO";
                break;
            case "infundado_en_parte":
                apelacion.setEstado(Apelacion.EstadoApelacion.RESUELTO_INFUNDADO_EN_PARTE);
                textoResultado = "RESUELTO - INFUNDADO EN PARTE";
                break;
            case "improcedente":
                apelacion.setEstado(Apelacion.EstadoApelacion.RESUELTO_IMPROCEDENTE);
                textoResultado = "RESUELTO - IMPROCEDENTE";
                break;
            case "sustraccion_materia":
                apelacion.setEstado(Apelacion.EstadoApelacion.CONCLUSION_SUSTRACCION_MATERIA);
                textoResultado = "CONCLUSIÓN POR SUSTRACCIÓN DE MATERIA";
                break;
            case "desistimiento":
                apelacion.setEstado(Apelacion.EstadoApelacion.CONCLUSION_DESISTIMIENTO);
                textoResultado = "CONCLUSIÓN POR DESISTIMIENTO";
                break;
            default:
                throw new IllegalArgumentException("Tipo de resolución no válido: " + request.getDecision());
        }

        // --- BE-03: El resultado queda guardado (Cumpliendo el escenario 3 de GitHub) ---
        if (Boolean.TRUE.equals(request.getIniciarProcesoDisciplinario())) {
            textoResultado += " (INCLUYE PROCESO DISCIPLINARIO)";
        }

        apelacion.setResultado(textoResultado + sufijoPlazo);
        apelacion.setFundamentos(request.getFundamentos());
        apelacion.setFechaResolucion(LocalDateTime.now());

        return apelacionRepository.save(apelacion);
    }
}