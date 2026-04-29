package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transparencia.api.exception.RecursoNoEncontradoException;
import com.transparencia.api.model.dto.ApelacionDTO;
import com.transparencia.api.model.dto.CalificacionRequest;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.Calificacion;
import com.transparencia.api.model.entity.EstadoApelacion;
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

    private static final Set<EstadoApelacion> ESTADOS_RESUELTOS = Set.of(
        EstadoApelacion.RESUELTO,
        EstadoApelacion.RESUELTO_FUNDADO,
        EstadoApelacion.RESUELTO_FUNDADO_EN_PARTE,
        EstadoApelacion.RESUELTO_INFUNDADO,
        EstadoApelacion.RESUELTO_INFUNDADO_EN_PARTE,
        EstadoApelacion.RESUELTO_IMPROCEDENTE,
        EstadoApelacion.TENER_POR_NO_PRESENTADO,
        EstadoApelacion.CONCLUSION_SUSTRACCION_MATERIA,
        EstadoApelacion.CONCLUSION_DESISTIMIENTO
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
            apelacionService.contarApelacionesPorEstado(EstadoApelacion.PENDIENTE_ELEVACION)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.EN_CALIFICACION_1);
        stats.put("pendientes", pendientes);

        long enProceso =
            apelacionService.contarApelacionesPorEstado(EstadoApelacion.EN_CALIFICACION_2)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.EN_RESOLUCION);
        stats.put("enProceso", enProceso);

        stats.put("enSubsanacion", apelacionService.contarApelacionesPorEstado(EstadoApelacion.EN_SUBSANACION));

        long resueltas =
            apelacionService.contarApelacionesPorEstado(EstadoApelacion.RESUELTO)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.RESUELTO_FUNDADO)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.RESUELTO_FUNDADO_EN_PARTE)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.RESUELTO_INFUNDADO)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.RESUELTO_INFUNDADO_EN_PARTE)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.RESUELTO_IMPROCEDENTE)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.TENER_POR_NO_PRESENTADO)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.CONCLUSION_SUSTRACCION_MATERIA)
                + apelacionService.contarApelacionesPorEstado(EstadoApelacion.CONCLUSION_DESISTIMIENTO);
        stats.put("resueltas", resueltas);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<ApelacionDTO> listarPendientes() {
        return apelacionService.findPendientes().stream()
            .map(ApelacionDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ApelacionDTO> listarPorEstado(EstadoApelacion estado) {
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
        boolean esSegundaCalificacion = apelacion.getEstado() == EstadoApelacion.EN_CALIFICACION_2;

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
            apelacion.setEstado(EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION);
            apelacion.setCalificacionSegunda(Calificacion.ADMITIDO);
        } else {
            apelacion.setEstado(EstadoApelacion.EN_CALIFICACION_2);
            apelacion.setCalificacionPrimera(Calificacion.ADMISIBLE);
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

        apelacion.setEstado(EstadoApelacion.EN_SUBSANACION);
        apelacion.setCalificacionPrimera(Calificacion.INADMISIBLE);
        apelacion.setFechaSubsanacion(LocalDate.now());
        apelacion.setDiasSubsanacion(diasSubsanacion);
        apelacionService.save(apelacion);

        return ApelacionDTO.from(apelacion);
    }

    @Transactional
    public ApelacionDTO inadmitirApelacion(Long apelacionId, CalificacionRequest request) {
        Apelacion apelacion = buscarApelacion(apelacionId);
        boolean esSegundaCalificacion = apelacion.getEstado() == EstadoApelacion.EN_CALIFICACION_2;

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

        apelacion.setEstado(EstadoApelacion.RESUELTO_IMPROCEDENTE);
        if (esSegundaCalificacion) {
            apelacion.setCalificacionSegunda(Calificacion.IMPROCEDENTE);
        } else {
            apelacion.setCalificacionPrimera(Calificacion.IMPROCEDENTE);
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

        apelacion.setEstado(EstadoApelacion.TENER_POR_NO_PRESENTADO);
        apelacion.setResultado("TENER_POR_NO_PRESENTADO");
        apelacionService.save(apelacion);

        return ApelacionDTO.from(apelacion);
    }

    private Apelacion buscarApelacion(Long apelacionId) {
        try {
            return apelacionService.obtenerApelacionPorId(apelacionId)
                    .orElseThrow();
        } catch (Exception e) {
            throw new RecursoNoEncontradoException("Apelacion no encontrada con ID: " + apelacionId);
        }
    }

    private void validarEstadoPrimeraCalificacion(Apelacion apelacion) {
        if (apelacion.getEstado() != EstadoApelacion.EN_CALIFICACION_1) {
            throw new IllegalArgumentException(
                "La apelacion debe estar en EN_CALIFICACION_1 para la primera calificacion"
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
            throw new IllegalArgumentException(
                "El plazo de 7 dias habiles para la primera calificacion ha vencido"
            );
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
}
