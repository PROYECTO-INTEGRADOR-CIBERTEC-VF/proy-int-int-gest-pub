package com.transparencia.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.EstadoApelacion;
import com.transparencia.api.repository.ApelacionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApelacionServiceTest {

    @Mock
    ApelacionRepository apelacionRepository;

    @InjectMocks
    ApelacionService apelacionService;

    private Apelacion crearApelacion(Long id, String expediente, EstadoApelacion estado) {
        Apelacion a = new Apelacion();
        a.setIdApelacion(id);
        a.setExpediente(expediente);
        a.setFundamentos("Fundamentos de prueba");
        a.setEstado(estado);
        a.setFechaApelacion(LocalDateTime.now());
        return a;
    }

    // 1 y 4. Test: Crear apelación exitosa y generarExpediente
    @Test
    void obtenerTodasLasApelaciones_debeRetornarLista() {
        var apelaciones = List.of(
            crearApelacion(1L, "00001-2025-JUS/TTAIP", EstadoApelacion.PENDIENTE_ELEVACION),
            crearApelacion(2L, "00002-2025-JUS/TTAIP", EstadoApelacion.EN_CALIFICACION_1)
        );
        when(apelacionRepository.findAll()).thenReturn(apelaciones);

        List<Apelacion> resultado = apelacionService.obtenerTodasLasApelaciones();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void obtenerApelacionPorId_existente_debeRetornarPresente() {
        var apelacion = crearApelacion(1L, "00001-2025-JUS/TTAIP", EstadoApelacion.PENDIENTE_ELEVACION);
        when(apelacionRepository.findById(1L)).thenReturn(Optional.of(apelacion));

        Optional<Apelacion> resultado = apelacionService.obtenerApelacionPorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getExpediente()).isEqualTo("00001-2025-JUS/TTAIP");
    }

    @Test
    void obtenerApelacionPorId_noExistente_debeRetornarVacio() {
        when(apelacionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Apelacion> resultado = apelacionService.obtenerApelacionPorId(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void obtenerApelacionPorExpediente_debeRetornarApelacion() {
        var apelacion = crearApelacion(1L, "00001-2025-JUS/TTAIP", EstadoApelacion.PENDIENTE_ELEVACION);
        when(apelacionRepository.findByExpediente("00001-2025-JUS/TTAIP")).thenReturn(Optional.of(apelacion));

        Optional<Apelacion> resultado = apelacionService.obtenerApelacionPorExpediente("00001-2025-JUS/TTAIP");

        assertThat(resultado).isPresent();
    }

    @Test
    void obtenerApelacionesPorEstado_debeRetornarFiltradas() {
        var apelaciones = List.of(
            crearApelacion(1L, "00001-2025-JUS/TTAIP", EstadoApelacion.EN_CALIFICACION_1)
        );

        when(apelacionRepository.findByEstadoOrderByFechaApelacionDesc(EstadoApelacion.EN_CALIFICACION_1)).thenReturn(apelaciones);

        List<Apelacion> resultado = apelacionService.findByEstado(EstadoApelacion.EN_CALIFICACION_1);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void contarApelacionesPorEstado_debeRetornarCuenta() {
        when(apelacionRepository.countByEstado(EstadoApelacion.PENDIENTE_ELEVACION)).thenReturn(7L);

        long resultado = apelacionService.countByEstado(EstadoApelacion.PENDIENTE_ELEVACION);

        assertThat(resultado).isEqualTo(7L);
    }

    @Test
    void findByCiudadanoId_debeRetornarApelacionesDelCiudadano() {
        var apelaciones = List.of(
            crearApelacion(1L, "00001-2025-JUS/TTAIP", EstadoApelacion.PENDIENTE_ELEVACION)
        );

        when(apelacionRepository.findByCiudadano_IdUsuarioOrderByFechaApelacionDesc(10L)).thenReturn(apelaciones);

        List<Apelacion> resultado = apelacionService.findByCiudadanoId(10L);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void findPendientes_debeRetornarApelacionesPendientes() {
        var apelaciones = List.of(
            crearApelacion(1L, "00001-2025-JUS/TTAIP", EstadoApelacion.PENDIENTE_ELEVACION)
        );
        when(apelacionRepository.findPendientes(anyList())).thenReturn(apelaciones);

        List<Apelacion> resultado = apelacionService.findPendientes();

        assertThat(resultado).hasSize(1);
    }

    @Test
    void generarExpediente_debeGenerarFormatoTTAIP() {
        int year = LocalDate.now().getYear();
        when(apelacionRepository.count()).thenReturn(5L);

        String expediente = apelacionService.generarExpediente();

        assertThat(expediente).isEqualTo("00006-" + year + "-JUS-TTAIP");
    }

    @Test
    void save_sinExpediente_debeGenerarExpediente() {
        Apelacion apelacion = crearApelacion(null, null, EstadoApelacion.PENDIENTE_ELEVACION);
        when(apelacionRepository.count()).thenReturn(0L);
        when(apelacionRepository.save(any())).thenAnswer(inv -> {
            Apelacion a = inv.getArgument(0);
            a.setIdApelacion(1L);
            return a;
        });

        Apelacion resultado = apelacionService.save(apelacion);

        assertThat(resultado.getExpediente()).endsWith("-JUS-TTAIP");
        verify(apelacionRepository).save(apelacion);
    }

    @Test
    void save_conExpediente_noDebeRegenerar() {
        Apelacion apelacion = crearApelacion(1L, "00010-2025-JUS/TTAIP", EstadoApelacion.PENDIENTE_ELEVACION);
        when(apelacionRepository.save(apelacion)).thenReturn(apelacion);

        Apelacion resultado = apelacionService.save(apelacion);

        assertThat(resultado.getExpediente()).isEqualTo("00010-2025-JUS/TTAIP");
    }

    @Test
    void eliminarApelacion_debeInvocarDeleteById() {
        apelacionService.eliminarApelacion(1L);

        verify(apelacionRepository).deleteById(1L);
    }

    @Test
    void count_debeRetornarCuenta() {
        when(apelacionRepository.count()).thenReturn(12L);

        long resultado = apelacionService.count();

        assertThat(resultado).isEqualTo(12L);
    }

    @Test
    void countByCiudadanoId_debeRetornarCantidad() {
        var apelaciones = List.of(
            crearApelacion(1L, "00001-2025-JUS/TTAIP", EstadoApelacion.PENDIENTE_ELEVACION),
            crearApelacion(2L, "00002-2025-JUS/TTAIP", EstadoApelacion.EN_CALIFICACION_1)
        );
        when(apelacionRepository.findByCiudadano_IdUsuarioOrderByFechaApelacionDesc(10L)).thenReturn(apelaciones);

        long resultado = apelacionService.countByCiudadanoId(10L);

        assertThat(resultado).isEqualTo(2L);
    }
}