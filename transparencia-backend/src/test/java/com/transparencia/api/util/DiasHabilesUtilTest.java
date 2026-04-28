package com.transparencia.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DiasHabilesUtilTest {

    @Test
    void esDiaHabil_debeRetornarFalseParaSabado() {
        LocalDate sabado = LocalDate.of(2026, 4, 18);
        assertFalse(DiasHabilesUtil.esDiaHabil(sabado));
    }

    @Test
    void esDiaHabil_debeRetornarFalseParaFeriado() {
        LocalDate fiestasPatrias = LocalDate.of(2026, 7, 28);
        assertFalse(DiasHabilesUtil.esDiaHabil(fiestasPatrias));
    }

    @Test
    void esDiaHabil_debeRetornarTrueParaDiaLaboralNoFeriado() {
        LocalDate juevesLaboral = LocalDate.of(2026, 4, 16);
        assertTrue(DiasHabilesUtil.esDiaHabil(juevesLaboral));
    }

    @Test
    void esFeriado_debeDetectarFeriadoFijo() {
        LocalDate navidad = LocalDate.of(2026, 12, 25);
        assertTrue(DiasHabilesUtil.esFeriado(navidad));
    }

    @Test
    void esFeriado_debeRetornarFalseCuandoNoEsFeriado() {
        LocalDate fechaRegular = LocalDate.of(2026, 12, 24);
        assertFalse(DiasHabilesUtil.esFeriado(fechaRegular));
    }

    @Test
    void sumarDiasHabiles_debeSaltarFinDeSemana() {
        LocalDate viernes = LocalDate.of(2026, 4, 17);
        LocalDate resultado = DiasHabilesUtil.sumarDiasHabiles(viernes, 1);

        assertEquals(LocalDate.of(2026, 4, 20), resultado);
    }

    @Test
    void sumarDiasHabiles_debeSaltarFeriadosConsecutivos() {
        LocalDate inicio = LocalDate.of(2026, 7, 27);
        LocalDate resultado = DiasHabilesUtil.sumarDiasHabiles(inicio, 1);

        assertEquals(LocalDate.of(2026, 7, 30), resultado);
    }

    @Test
    void contarDiasHabiles_debeContarEnRangoAscendente() {
        LocalDate desde = LocalDate.of(2026, 4, 13);
        LocalDate hasta = LocalDate.of(2026, 4, 17);

        assertEquals(5, DiasHabilesUtil.contarDiasHabiles(desde, hasta));
    }

    @Test
    void contarDiasHabiles_debeRetornarNegativoEnRangoDescendente() {
        LocalDate desde = LocalDate.of(2026, 4, 17);
        LocalDate hasta = LocalDate.of(2026, 4, 13);

        assertEquals(-5, DiasHabilesUtil.contarDiasHabiles(desde, hasta));
    }

    @Test
    void diasHabilesRestantes_debeSerPositivoParaFechaFutura() {
        LocalDate fechaFutura = LocalDate.now().plusDays(10);
        assertTrue(DiasHabilesUtil.diasHabilesRestantes(fechaFutura) > 0);
    }

    @Test
    void diasHabilesRestantes_debeSerNegativoParaFechaVencida() {
        LocalDate fechaVencida = LocalDate.now().minusDays(10);
        assertTrue(DiasHabilesUtil.diasHabilesRestantes(fechaVencida) < 0);
    }
}
