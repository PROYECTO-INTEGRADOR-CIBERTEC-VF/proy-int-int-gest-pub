package com.transparencia.api.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.transparencia.api.util.DiasHabilesUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SilencioAdministrativoScheduler {

    private final SolicitudService solicitudService;
    private final Clock clock;

    @Autowired
    public SilencioAdministrativoScheduler(SolicitudService solicitudService) {
        this(solicitudService, Clock.systemDefaultZone());
    }

    SilencioAdministrativoScheduler(SolicitudService solicitudService, Clock clock) {
        this.solicitudService = solicitudService;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 8 * * MON-FRI")
    public void ejecutarDeteccion() {
        LocalDate hoy = LocalDate.now(clock);
        if (!DiasHabilesUtil.esDiaHabil(hoy)) {
            log.info("Scheduler silencio administrativo omitido por dia no habil: {}", hoy);
            return;
        }

        int totalVencidas = solicitudService.detectarSilencioAdministrativo();
        log.info("Scheduler silencio administrativo ejecuto deteccion. Solicitudes marcadas VENCIDA: {}", totalVencidas);
    }
}
