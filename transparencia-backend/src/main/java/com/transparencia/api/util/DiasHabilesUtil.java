package com.transparencia.api.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

/**
 * Utilidad para calculo de dias habiles segun normativa peruana.
 */
public final class DiasHabilesUtil {

    private DiasHabilesUtil() {
    }

    private static final Set<MonthDay> FERIADOS_FIJOS = Set.of(
        MonthDay.of(1, 1),
        MonthDay.of(5, 1),
        MonthDay.of(6, 7),
        MonthDay.of(6, 29),
        MonthDay.of(7, 23),
        MonthDay.of(7, 28),
        MonthDay.of(7, 29),
        MonthDay.of(8, 6),
        MonthDay.of(8, 30),
        MonthDay.of(10, 8),
        MonthDay.of(11, 1),
        MonthDay.of(12, 8),
        MonthDay.of(12, 9),
        MonthDay.of(12, 25)
    );

    public static boolean esDiaHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return false;
        }
        return !esFeriado(fecha);
    }

    public static boolean esFeriado(LocalDate fecha) {
        return FERIADOS_FIJOS.contains(MonthDay.from(fecha));
    }

    public static LocalDate sumarDiasHabiles(LocalDate fechaInicio, int diasHabiles) {
        LocalDate fecha = fechaInicio;
        int contador = 0;
        while (contador < diasHabiles) {
            fecha = fecha.plusDays(1);
            if (esDiaHabil(fecha)) {
                contador++;
            }
        }
        return fecha;
    }

    public static int contarDiasHabiles(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            return -contarDiasHabiles(hasta, desde);
        }
        int contador = 0;
        LocalDate fecha = desde;
        while (!fecha.isAfter(hasta)) {
            if (esDiaHabil(fecha)) {
                contador++;
            }
            fecha = fecha.plusDays(1);
        }
        return contador;
    }

    public static int diasHabilesRestantes(LocalDate fechaLimite) {
        LocalDate hoy = LocalDate.now();
        if (hoy.isAfter(fechaLimite)) {
            return -contarDiasHabiles(fechaLimite, hoy);
        }
        return contarDiasHabiles(hoy, fechaLimite);
    }
}
