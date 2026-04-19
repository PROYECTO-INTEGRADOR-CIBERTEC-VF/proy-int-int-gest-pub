package com.transparencia.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.transparencia.api.model.dto.CrearRespuestaRequest;
import com.transparencia.api.model.dto.RespuestaDTO;
import com.transparencia.api.model.entity.Funcionario;
import com.transparencia.api.model.entity.Respuesta;
import com.transparencia.api.model.entity.Solicitud;
import com.transparencia.api.repository.RespuestaRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RespuestaServiceTest {

    @Mock
    private RespuestaRepository respuestaRepository;

    @Mock
    private SolicitudService solicitudService;

    @Mock
    private FuncionarioService funcionarioService;

    @InjectMocks
    private RespuestaService respuestaService;

    private Solicitud crearSolicitudSinRespuesta() {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(1L);
        solicitud.setEstado(Solicitud.EstadoSolicitud.EN_REVISION);
        return solicitud;
    }

    private Funcionario crearFuncionario() {
        Funcionario funcionario = new Funcionario();
        funcionario.setIdUsuario(10L);
        funcionario.setNombreCompleto("Funcionario Test");
        return funcionario;
    }

    @Test
    void aceptarSolicitud_debeCrearEntregaTotalYCambiarEstadoRespondida() {
        Solicitud solicitud = crearSolicitudSinRespuesta();
        Funcionario funcionario = crearFuncionario();

        when(solicitudService.obtenerSolicitudPorId(1L)).thenReturn(Optional.of(solicitud));
        when(funcionarioService.obtenerFuncionarioPorId(10L)).thenReturn(Optional.of(funcionario));
        when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(solicitudService.save(any(Solicitud.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CrearRespuestaRequest request = new CrearRespuestaRequest(
            1L,
            10L,
            "ENTREGA_TOTAL",
            "Se entrega la informacion solicitada",
            null,
            null,
            "DIGITAL",
            2
        );

        RespuestaDTO resultado = respuestaService.aceptarSolicitud(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.tipoRespuesta()).isEqualTo("ENTREGA_TOTAL");
        assertThat(resultado.decision()).isEqualTo("aceptar");
        assertThat(resultado.contenido()).contains("Formato de entrega: DIGITAL");
        assertThat(resultado.contenido()).contains("Plazo estimado: 2 dias habiles");
        assertThat(solicitud.getEstado()).isEqualTo(Solicitud.EstadoSolicitud.RESPONDIDA);
        verify(respuestaRepository).save(any(Respuesta.class));
        verify(solicitudService).save(solicitud);
    }

    @Test
    void denegarSolicitud_debeCrearDenegacionYCambiarEstadoDenegada() {
        Solicitud solicitud = crearSolicitudSinRespuesta();
        Funcionario funcionario = crearFuncionario();

        when(solicitudService.obtenerSolicitudPorId(1L)).thenReturn(Optional.of(solicitud));
        when(funcionarioService.obtenerFuncionarioPorId(10L)).thenReturn(Optional.of(funcionario));
        when(respuestaRepository.save(any(Respuesta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(solicitudService.save(any(Solicitud.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CrearRespuestaRequest request = new CrearRespuestaRequest(
            1L,
            10L,
            "DENEGACION_TOTAL",
            "No se puede entregar la informacion",
            "Informacion Reservada - Art. 15-B Ley 27806",
            "Fundamento legal de prueba",
            null,
            null
        );

        RespuestaDTO resultado = respuestaService.denegarSolicitud(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.tipoRespuesta()).isEqualTo("DENEGACION_TOTAL");
        assertThat(resultado.decision()).isEqualTo("denegar");
        assertThat(resultado.causalDenegatoria()).isEqualTo("Informacion Reservada - Art. 15-B Ley 27806");
        assertThat(resultado.fundamentoLegal()).isEqualTo("Fundamento legal de prueba");
        assertThat(solicitud.getEstado()).isEqualTo(Solicitud.EstadoSolicitud.DENEGADA);
        verify(respuestaRepository).save(any(Respuesta.class));
        verify(solicitudService).save(solicitud);
    }

    @Test
    void crearRespuesta_conSilencioAdministrativoManual_debeFallar() {
        Solicitud solicitud = crearSolicitudSinRespuesta();

        when(solicitudService.obtenerSolicitudPorId(1L)).thenReturn(Optional.of(solicitud));
        when(funcionarioService.obtenerFuncionarioPorId(10L)).thenReturn(Optional.of(crearFuncionario()));

        CrearRespuestaRequest request = new CrearRespuestaRequest(
            1L,
            10L,
            "SILENCIO_ADMINISTRATIVO",
            "No corresponde registrar manualmente",
            null,
            null,
            null,
            null
        );

        assertThatThrownBy(() -> respuestaService.crearRespuesta(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Silencio administrativo se aplica automaticamente");

        verify(respuestaRepository, never()).save(any(Respuesta.class));
        verify(solicitudService, never()).save(any(Solicitud.class));
    }

    @Test
    void crearRespuesta_conSolicitudYaRespondida_debeFallar() {
        Solicitud solicitud = crearSolicitudSinRespuesta();
        solicitud.setRespuesta(new Respuesta());

        when(solicitudService.obtenerSolicitudPorId(1L)).thenReturn(Optional.of(solicitud));

        CrearRespuestaRequest request = new CrearRespuestaRequest(
            1L,
            10L,
            "ENTREGA_TOTAL",
            "Contenido de prueba",
            null,
            null,
            null,
            null
        );

        assertThatThrownBy(() -> respuestaService.crearRespuesta(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La solicitud ya tiene una respuesta");

        verify(funcionarioService, never()).obtenerFuncionarioPorId(anyLong());
        verify(respuestaRepository, never()).save(any(Respuesta.class));
        verify(solicitudService, never()).save(any(Solicitud.class));
    }
}
