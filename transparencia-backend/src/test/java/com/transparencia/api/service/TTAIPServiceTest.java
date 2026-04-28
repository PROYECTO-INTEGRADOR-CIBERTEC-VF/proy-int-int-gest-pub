package com.transparencia.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.transparencia.api.model.dto.ApelacionDTO;
import com.transparencia.api.model.dto.CalificacionRequest;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.Resolucion;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TTAIPServiceTest {

    @Mock
    private ApelacionService apelacionService;

    @Mock
    private ResolucionService resolucionService;

    @Mock
    private MiembroTTAIPService miembroTTAIPService;

    @InjectMocks
    private TTAIPService ttaipService;

    private Apelacion crearApelacionPrimeraCalificacion() {
        Apelacion apelacion = new Apelacion();
        apelacion.setIdApelacion(1L);
        apelacion.setExpediente("00001-2026-JUS-TTAIP");
        apelacion.setEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_1);
        apelacion.setFechaApelacion(LocalDateTime.now());
        return apelacion;
    }

    @Test
    void admitirApelacionDebeCambiarEstadoAEnCalificacion2() {
        Apelacion apelacion = crearApelacionPrimeraCalificacion();
        CalificacionRequest request = new CalificacionRequest(
            "Fundamentos de admision",
            null,
            "Observacion",
            null
        );

        when(apelacionService.findById(1L)).thenReturn(Optional.of(apelacion));
        when(apelacionService.save(any(Apelacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApelacionDTO resultado = ttaipService.admitirApelacion(1L, request);

        assertThat(resultado.getEstado()).isEqualTo(Apelacion.EstadoApelacion.EN_CALIFICACION_2.name());
        assertThat(apelacion.getCalificacionPrimera()).isEqualTo(Apelacion.Calificacion.ADMISIBLE);
        verify(resolucionService).save(any(Resolucion.class));
        verify(apelacionService).save(apelacion);
    }

    @Test
    void requerirSubsanacionDebeCambiarEstadoAEnSubsanacion() {
        Apelacion apelacion = crearApelacionPrimeraCalificacion();
        CalificacionRequest request = new CalificacionRequest(
            "Fundamentos de subsanacion",
            null,
            "Debe subsanar",
            3
        );

        when(apelacionService.findById(1L)).thenReturn(Optional.of(apelacion));
        when(apelacionService.save(any(Apelacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApelacionDTO resultado = ttaipService.requerirSubsanacion(1L, request);

        assertThat(resultado.getEstado()).isEqualTo(Apelacion.EstadoApelacion.EN_SUBSANACION.name());
        assertThat(apelacion.getCalificacionPrimera()).isEqualTo(Apelacion.Calificacion.INADMISIBLE);
        assertThat(apelacion.getDiasSubsanacion()).isEqualTo(3);
        verify(resolucionService).save(any(Resolucion.class));
        verify(apelacionService).save(apelacion);
    }

    @Test
    void inadmitirApelacionDebeCambiarEstadoAResueltoImprocedente() {
        Apelacion apelacion = crearApelacionPrimeraCalificacion();
        CalificacionRequest request = new CalificacionRequest(
            "Fundamentos de improcedencia",
            null,
            "No cumple requisitos",
            null
        );

        when(apelacionService.findById(1L)).thenReturn(Optional.of(apelacion));
        when(apelacionService.save(any(Apelacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApelacionDTO resultado = ttaipService.inadmitirApelacion(1L, request);

        assertThat(resultado.getEstado()).isEqualTo(Apelacion.EstadoApelacion.RESUELTO_IMPROCEDENTE.name());
        assertThat(apelacion.getCalificacionPrimera()).isEqualTo(Apelacion.Calificacion.IMPROCEDENTE);
        verify(resolucionService).save(any(Resolucion.class));
        verify(apelacionService).save(apelacion);
    }
}
