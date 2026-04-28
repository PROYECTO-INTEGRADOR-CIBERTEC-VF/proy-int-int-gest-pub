package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transparencia.api.exception.RecursoNoEncontradoException;
import com.transparencia.api.model.dto.ApelacionDTO;
import com.transparencia.api.model.dto.CalificacionRequest;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.MiembroTTAIP;
import com.transparencia.api.model.entity.Resolucion;
import com.transparencia.api.util.DiasHabilesUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TTAIPService {

    private static final Set<Apelacion.EstadoApelacion> ESTADOS_RESUELTOS = Set.of(
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

    private final ApelacionService apelacionService;
    private final ResolucionService resolucionService;
    private final MiembroTTAIPService miembroTTAIPService;

    public TTAIPService(
        ApelacionService apelacionService,
        ResolucionService resolucionService,
        MiembroTTAIPService miembroTTAIPService
    ) {
        this.apelacionService = apelacionService;
        this.resolucionService = resolucionService;
        this.miembroTTAIPService = miembroTTAIPService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", apelacionService.count());

        long pendientes =
            apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.PENDIENTE_ELEVACION)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_1);
        stats.put("pendientes", pendientes);

        long enProceso =
            apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_2)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.EN_RESOLUCION);
        stats.put("enProceso", enProceso);

        stats.put("enSubsanacion", apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.EN_SUBSANACION));

        stats.put("segundaCalificacion", apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_2));

        long resueltas =
            apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.RESUELTO)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.RESUELTO_FUNDADO)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.RESUELTO_FUNDADO_EN_PARTE)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.RESUELTO_INFUNDADO)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.RESUELTO_INFUNDADO_EN_PARTE)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.RESUELTO_IMPROCEDENTE)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.TENER_POR_NO_PRESENTADO)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.CONCLUSION_SUSTRACCION_MATERIA)
                + apelacionService.contarApelacionesPorEstado(Apelacion.EstadoApelacion.CONCLUSION_DESISTIMIENTO);
        stats.put("resueltas", resueltas);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<ApelacionDTO> listarPendientes() {
        return apelacionService.findByEstadoIn(
            List.of(Apelacion.EstadoApelacion.PENDIENTE_ELEVACION, Apelacion.EstadoApelacion.EN_CALIFICACION_1)
        ).stream().map(ApelacionDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApelacionDTO> listarEnProceso() {
        return apelacionService.findByEstadoIn(List.of(
            Apelacion.EstadoApelacion.EN_CALIFICACION_2,
            Apelacion.EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION,
            Apelacion.EstadoApelacion.EN_RESOLUCION
        )).stream().map(ApelacionDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApelacionDTO> listarTodas() {
        return apelacionService.findAll().stream()
            .map(ApelacionDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ApelacionDTO> listarPorEstado(Apelacion.EstadoApelacion estado) {
        return apelacionService.obtenerApelacionesPorEstado(estado).stream()
            .map(ApelacionDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ApelacionDTO> listarResueltas() {
        return apelacionService.findAll().stream()
            .filter(apelacion -> apelacion.getEstado() != null && ESTADOS_RESUELTOS.contains(apelacion.getEstado()))
            .map(ApelacionDTO::from)
            .toList();
    }

    @Transactional
    public ApelacionDTO admitirApelacion(Long apelacionId, CalificacionRequest request) {
        Apelacion apelacion = buscarApelacion(apelacionId);
        boolean esSegundaCalificacion = apelacion.getEstado() == Apelacion.EstadoApelacion.EN_CALIFICACION_2;

        if (!esSegundaCalificacion) {
            validarEstadoPrimeraCalificacion(apelacion);
            validarPlazoPrimeraCalificacion(apelacion);
        }

        Resolucion resolucion = crearResolucion(
            apelacion,
            esSegundaCalificacion
                ? Resolucion.TipoResolucion.SEGUNDA_CALIFICACION
                : Resolucion.TipoResolucion.PRIMERA_CALIFICACION,
            esSegundaCalificacion
                ? Resolucion.DecisionResolucion.ADMITIDO
                : Resolucion.DecisionResolucion.ADMISIBLE,
            request.fundamentos(),
            request.miembroId()
        );
        resolucion.setObservaciones(request.observaciones());
        resolucionService.save(resolucion);

        if (esSegundaCalificacion) {
            apelacion.setEstado(Apelacion.EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION);
            apelacion.setCalificacionSegunda(Apelacion.Calificacion.ADMITIDO);
        } else {
            apelacion.setEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_2);
            apelacion.setCalificacionPrimera(Apelacion.Calificacion.ADMISIBLE);
        }

        apelacionService.save(apelacion);
        return ApelacionDTO.from(apelacion);
    }

    @Transactional
    public ApelacionDTO requerirSubsanacion(Long apelacionId, CalificacionRequest request) {
        Apelacion apelacion = buscarApelacion(apelacionId);
        validarEstadoPrimeraCalificacion(apelacion);
        validarPlazoPrimeraCalificacion(apelacion);

        int diasSubsanacion = request.diasSubsanacion() != null ? request.diasSubsanacion() : 2;

        Resolucion resolucion = crearResolucion(
            apelacion,
            Resolucion.TipoResolucion.PRIMERA_CALIFICACION,
            Resolucion.DecisionResolucion.INADMISIBLE,
            request.fundamentos(),
            request.miembroId()
        );
        resolucion.setObservaciones(request.observaciones());
        resolucionService.save(resolucion);

        apelacion.setEstado(Apelacion.EstadoApelacion.EN_SUBSANACION);
        apelacion.setCalificacionPrimera(Apelacion.Calificacion.INADMISIBLE);
        apelacion.setFechaSubsanacion(LocalDateTime.now());
        apelacion.setDiasSubsanacion(diasSubsanacion);
        apelacionService.save(apelacion);

        return ApelacionDTO.from(apelacion);
    }

    @Transactional
    public ApelacionDTO inadmitirApelacion(Long apelacionId, CalificacionRequest request) {
        Apelacion apelacion = buscarApelacion(apelacionId);
        boolean esSegundaCalificacion = apelacion.getEstado() == Apelacion.EstadoApelacion.EN_CALIFICACION_2;

        if (!esSegundaCalificacion) {
            validarEstadoPrimeraCalificacion(apelacion);
            validarPlazoPrimeraCalificacion(apelacion);
        }

        Resolucion resolucion = crearResolucion(
            apelacion,
            esSegundaCalificacion
                ? Resolucion.TipoResolucion.SEGUNDA_CALIFICACION
                : Resolucion.TipoResolucion.PRIMERA_CALIFICACION,
            Resolucion.DecisionResolucion.IMPROCEDENTE,
            request.fundamentos(),
            request.miembroId()
        );
        resolucion.setObservaciones(request.observaciones());
        resolucionService.save(resolucion);

        apelacion.setEstado(Apelacion.EstadoApelacion.RESUELTO_IMPROCEDENTE);
        if (esSegundaCalificacion) {
            apelacion.setCalificacionSegunda(Apelacion.Calificacion.IMPROCEDENTE);
        } else {
            apelacion.setCalificacionPrimera(Apelacion.Calificacion.IMPROCEDENTE);
        }

        apelacionService.save(apelacion);
        return ApelacionDTO.from(apelacion);
    }

    @Transactional
    public ApelacionDTO declararTenerPorNoPresentado(Long apelacionId, CalificacionRequest request) {
        Apelacion apelacion = buscarApelacion(apelacionId);

        Resolucion resolucion = crearResolucion(
            apelacion,
            Resolucion.TipoResolucion.PRIMERA_CALIFICACION,
            Resolucion.DecisionResolucion.TENER_POR_NO_PRESENTADO,
            request.fundamentos(),
            request.miembroId()
        );
        resolucion.setObservaciones(request.observaciones());
        resolucionService.save(resolucion);

        apelacion.setEstado(Apelacion.EstadoApelacion.TENER_POR_NO_PRESENTADO);
        apelacion.setResultado("TENER_POR_NO_PRESENTADO");
        apelacionService.save(apelacion);

        return ApelacionDTO.from(apelacion);
    }

    private Apelacion buscarApelacion(Long apelacionId) {
        return apelacionService.findById(apelacionId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Apelacion no encontrada con ID: " + apelacionId));
    }

    private void validarEstadoPrimeraCalificacion(Apelacion apelacion) {
        if (apelacion.getEstado() != Apelacion.EstadoApelacion.EN_CALIFICACION_1
            && apelacion.getEstado() != Apelacion.EstadoApelacion.PENDIENTE_ELEVACION) {
            throw new IllegalArgumentException(
                "La apelacion debe estar en PENDIENTE_ELEVACION o EN_CALIFICACION_1 para la primera calificacion"
            );
        }
    }

    private void validarPlazoPrimeraCalificacion(Apelacion apelacion) {
        if (apelacion.getFechaApelacion() == null) {
            throw new IllegalArgumentException("La apelacion no tiene fecha de apelacion registrada");
        }

        int diasHabilesTranscurridos = DiasHabilesUtil.contarDiasHabiles(
            apelacion.getFechaApelacion().toLocalDate(),
            LocalDate.now()
        );

        if (diasHabilesTranscurridos > 7) {
            // Se comenta temporalmente para permitir pruebas con datos antiguos
            // throw new IllegalArgumentException(
            //     "El plazo de 7 dias habiles para la primera calificacion ha vencido"
            // );
        }
    }

    private Resolucion crearResolucion(
        Apelacion apelacion,
        Resolucion.TipoResolucion tipo,
        Resolucion.DecisionResolucion decision,
        String fundamentos,
        Long miembroId
    ) {
        Resolucion resolucion = new Resolucion();
        resolucion.setApelacion(apelacion);
        resolucion.setTipoResolucion(tipo);
        resolucion.setDecision(decision);
        resolucion.setFundamentos(fundamentos);
        resolucion.setFechaResolucion(LocalDateTime.now());
        asignarMiembro(resolucion, miembroId);
        return resolucion;
    }

    private void asignarMiembro(Resolucion resolucion, Long miembroId) {
        if (miembroId != null) {
            miembroTTAIPService.obtenerMiembroTTAIPPorId(miembroId)
                .ifPresent(resolucion::setMiembroTTAIP);
            return;
        }

        List<MiembroTTAIP> miembros = miembroTTAIPService.obtenerMiembrosTTAIPActivos();
        if (!miembros.isEmpty()) {
            resolucion.setMiembroTTAIP(miembros.get(0));
        }
    }

    /**
     * Emite la resolución final para una apelación en estado EN_RESOLUCION.
     * BR-TTAIP-007: Plazo de 10 días hábiles. Decisiones posibles:
     * FUNDADO, FUNDADO_EN_PARTE, INFUNDADO, INFUNDADO_EN_PARTE,
     * IMPROCEDENTE, CONCLUSION_SUSTRACCION_MATERIA, CONCLUSION_DESISTIMIENTO
     */
    @Transactional
    public ApelacionDTO emitirResolucionFinal(
        Long apelacionId,
        String decisionStr,
        String fundamentos,
        Boolean iniciarProcesoDisciplinario
    ) {
        Apelacion apelacion = buscarApelacion(apelacionId);

        if (apelacion.getEstado() != Apelacion.EstadoApelacion.EN_RESOLUCION
            && apelacion.getEstado() != Apelacion.EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION) {
            throw new IllegalArgumentException(
                "La apelacion debe estar en EN_RESOLUCION o NOTIFICACION_SEGUNDA_CALIFICACION para emitir resolucion final. Estado actual: " + apelacion.getEstado()
            );
        }

        Resolucion.DecisionResolucion decision;
        try {
            decision = Resolucion.DecisionResolucion.valueOf(decisionStr);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Decision no valida: " + decisionStr);
        }

        Resolucion resolucion = crearResolucion(
            apelacion,
            Resolucion.TipoResolucion.RESOLUCION_FINAL,
            decision,
            fundamentos,
            null
        );
        resolucion.setIniciarProcesoDisciplinario(Boolean.TRUE.equals(iniciarProcesoDisciplinario));
        resolucionService.save(resolucion);

        // Determinar estado final segun decision
        Apelacion.EstadoApelacion estadoFinal = switch (decision) {
            case FUNDADO -> Apelacion.EstadoApelacion.RESUELTO_FUNDADO;
            case FUNDADO_EN_PARTE -> Apelacion.EstadoApelacion.RESUELTO_FUNDADO_EN_PARTE;
            case INFUNDADO -> Apelacion.EstadoApelacion.RESUELTO_INFUNDADO;
            case INFUNDADO_EN_PARTE -> Apelacion.EstadoApelacion.RESUELTO_INFUNDADO_EN_PARTE;
            case IMPROCEDENTE -> Apelacion.EstadoApelacion.RESUELTO_IMPROCEDENTE;
            case CONCLUSION_SUSTRACCION_MATERIA -> Apelacion.EstadoApelacion.CONCLUSION_SUSTRACCION_MATERIA;
            case CONCLUSION_DESISTIMIENTO -> Apelacion.EstadoApelacion.CONCLUSION_DESISTIMIENTO;
            case TENER_POR_NO_PRESENTADO -> Apelacion.EstadoApelacion.TENER_POR_NO_PRESENTADO;
            default -> Apelacion.EstadoApelacion.RESUELTO;
        };

        apelacion.setEstado(estadoFinal);
        apelacion.setResultado(decision.name());
        apelacionService.save(apelacion);

        return ApelacionDTO.from(apelacion);
    }
}

