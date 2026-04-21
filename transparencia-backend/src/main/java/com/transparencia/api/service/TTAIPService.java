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
        return apelacionService.findPendientes().stream()
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
        if (apelacion.getEstado() != Apelacion.EstadoApelacion.EN_CALIFICACION_1) {
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
