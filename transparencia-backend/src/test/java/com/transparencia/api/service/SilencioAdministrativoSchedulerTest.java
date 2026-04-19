package com.transparencia.api.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SilencioAdministrativoSchedulerTest {

    @Mock
    private SolicitudService solicitudService;

    @Test
    void ejecutarDeteccionEnDiaHabilDebeInvocarServicio() {
        Clock clock = Clock.fixed(Instant.parse("2026-04-15T13:00:00Z"), ZoneId.of("America/Lima"));
        SilencioAdministrativoScheduler scheduler = new SilencioAdministrativoScheduler(solicitudService, clock);
        when(solicitudService.detectarSilencioAdministrativo()).thenReturn(3);

        scheduler.ejecutarDeteccion();

        verify(solicitudService).detectarSilencioAdministrativo();
    }

    @Test
    void ejecutarDeteccionEnSabadoNoDebeInvocarServicio() {
        Clock clock = Clock.fixed(Instant.parse("2026-04-18T13:00:00Z"), ZoneId.of("America/Lima"));
        SilencioAdministrativoScheduler scheduler = new SilencioAdministrativoScheduler(solicitudService, clock);

        scheduler.ejecutarDeteccion();

        verify(solicitudService, never()).detectarSilencioAdministrativo();
    }

    @Test
    void ejecutarDeteccionEnFeriadoNoDebeInvocarServicio() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-28T13:00:00Z"), ZoneId.of("America/Lima"));
        SilencioAdministrativoScheduler scheduler = new SilencioAdministrativoScheduler(solicitudService, clock);

        scheduler.ejecutarDeteccion();

        verify(solicitudService, never()).detectarSilencioAdministrativo();
    }
}
