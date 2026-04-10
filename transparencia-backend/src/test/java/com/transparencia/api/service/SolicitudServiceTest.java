package com.transparencia.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.transparencia.api.model.entity.Solicitud;
import com.transparencia.api.repository.SolicitudRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @InjectMocks
    private SolicitudService solicitudService;

    private Solicitud crearSolicitud(Long id, String expediente, Solicitud.EstadoSolicitud estado) {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(id);
        solicitud.setExpediente(expediente);
        solicitud.setAsunto("Solicitud de prueba");
        solicitud.setDescripcion("Descripcion de prueba");
        solicitud.setEstado(estado);
        solicitud.setFechaPresentacion(LocalDate.now().atStartOfDay());
        solicitud.setFechaLimite(LocalDate.now().plusDays(10));
        return solicitud;
    }

    @Test
    void generarExpedienteSinPreviosDebeGenerarPrimero() {
        int year = LocalDate.now().getYear();
        String prefix = "SAIP-" + year + "-";
        when(solicitudRepository.findMaxExpedienteNumber(prefix + "%")).thenReturn(null);

        String expediente = solicitudService.generarExpediente();

        assertThat(expediente).isEqualTo(prefix + "00001");
    }

    @Test
    void generarExpedienteConPreviosDebeIncrementar() {
        int year = LocalDate.now().getYear();
        String prefix = "SAIP-" + year + "-";
        when(solicitudRepository.findMaxExpedienteNumber(prefix + "%")).thenReturn(42);

        String expediente = solicitudService.generarExpediente();

        assertThat(expediente).isEqualTo(prefix + "00043");
    }

    @Test
    void saveSinExpedienteDebeGenerarExpediente() {
        Solicitud solicitud = crearSolicitud(null, null, Solicitud.EstadoSolicitud.RECEPCIONADA);
        int year = LocalDate.now().getYear();
        when(solicitudRepository.findMaxExpedienteNumber(anyString())).thenReturn(7);
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitud resultado = solicitudService.save(solicitud);

        assertThat(resultado.getExpediente()).isEqualTo("SAIP-" + year + "-00008");
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void saveConExpedienteNoDebeRegenerar() {
        Solicitud solicitud = crearSolicitud(1L, "SAIP-2026-00010", Solicitud.EstadoSolicitud.RECEPCIONADA);
        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);

        Solicitud resultado = solicitudService.save(solicitud);

        assertThat(resultado.getExpediente()).isEqualTo("SAIP-2026-00010");
        verify(solicitudRepository, never()).findMaxExpedienteNumber(anyString());
    }

    @Test
    void detectarSilencioAdministrativoDebeMarcarVencidasYPersistir() {
        Solicitud solicitud1 = crearSolicitud(1L, "SAIP-2026-00001", Solicitud.EstadoSolicitud.RECEPCIONADA);
        Solicitud solicitud2 = crearSolicitud(2L, "SAIP-2026-00002", Solicitud.EstadoSolicitud.EN_REVISION);
        when(solicitudRepository.findVencidas(any(LocalDate.class), anyList())).thenReturn(List.of(solicitud1, solicitud2));
        when(solicitudRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        int totalVencidas = solicitudService.detectarSilencioAdministrativo();

        assertThat(totalVencidas).isEqualTo(2);
        assertThat(solicitud1.getEstado()).isEqualTo(Solicitud.EstadoSolicitud.VENCIDA);
        assertThat(solicitud2.getEstado()).isEqualTo(Solicitud.EstadoSolicitud.VENCIDA);
        verify(solicitudRepository).saveAll(anyList());
    }

    @Test
    void detectarSilencioAdministrativoSinResultadosNoDebePersistir() {
        when(solicitudRepository.findVencidas(any(LocalDate.class), anyList())).thenReturn(List.of());

        int totalVencidas = solicitudService.detectarSilencioAdministrativo();

        assertThat(totalVencidas).isZero();
        verify(solicitudRepository, never()).saveAll(anyList());
    }

    @Test
    void contarSolicitudesPorEstadoDebeRetornarConteo() {
        when(solicitudRepository.countByEstado(Solicitud.EstadoSolicitud.RECEPCIONADA)).thenReturn(12L);

        long total = solicitudService.contarSolicitudesPorEstado(Solicitud.EstadoSolicitud.RECEPCIONADA);

        assertThat(total).isEqualTo(12L);
    }

    @Test
    void countByEntidadIdDebeRetornarConteo() {
        when(solicitudRepository.countByEntidad_IdEntidad(5L)).thenReturn(4L);

        long total = solicitudService.countByEntidadId(5L);

        assertThat(total).isEqualTo(4L);
    }

    @Test
    void countDebeRetornarConteoTotal() {
        when(solicitudRepository.count()).thenReturn(27L);

        long total = solicitudService.count();

        assertThat(total).isEqualTo(27L);
    }
}