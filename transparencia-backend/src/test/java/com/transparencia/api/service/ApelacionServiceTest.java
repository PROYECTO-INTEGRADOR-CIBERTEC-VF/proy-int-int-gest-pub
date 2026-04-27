package com.transparencia.api.service;

import com.transparencia.api.model.dto.SegundaCalificacionDTO;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.repository.ApelacionRepository;
import com.transparencia.api.repository.DocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApelacionServiceTest {

    @Mock
    private ApelacionRepository apelacionRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ApelacionService apelacionService;

    private Apelacion apelacion;
    private SegundaCalificacionDTO request;
    private MultipartFile archivoMock;

    @BeforeEach
    void setUp() {
        // Configuracion de una apelación base para las pruebas
        apelacion = new Apelacion();
        apelacion.setIdApelacion(1L);
        apelacion.setEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_2);
        apelacion.setFechaSubsanacion(LocalDateTime.now().minusDays(2)); // Dentro del plazo

        request = new SegundaCalificacionDTO();
        request.setFundamentos("Fundamentos de prueba");

        archivoMock = new MockMultipartFile("archivo", "resolucion.pdf", "application/pdf", "dummy content".getBytes());
    }

    // --- ESCENARIO 1: ADMITIR (Transición a Notificación) ---
    @Test
    void procesarSegundaCalificacion_Admitir_GeneraEstadoCorrecto() {
        request.setDecision("ADMISIBLE");

        when(apelacionRepository.findById(1L)).thenReturn(Optional.of(apelacion));
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("ruta/dummy.pdf");
        when(apelacionRepository.save(any(Apelacion.class))).thenReturn(apelacion);

        Apelacion resultado = apelacionService.procesarSegundaCalificacion(1L, request, archivoMock);

        assertEquals(Apelacion.EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION, resultado.getEstado());
        assertEquals(Apelacion.Calificacion.ADMISIBLE, resultado.getCalificacionSegunda());
        assertTrue(resultado.getResultado().contains("ADMITIDO EN SEGUNDA CALIFICACIÓN"));
        verify(documentoRepository, times(1)).save(any());
    }

    // --- ESCENARIO 2: DECLARAR IMPROCEDENTE (Rechazo definitivo) ---
    @Test
    void procesarSegundaCalificacion_Improcedente_FinalizaProceso() {
        request.setDecision("IMPROCEDENTE");

        when(apelacionRepository.findById(1L)).thenReturn(Optional.of(apelacion));
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("ruta/dummy.pdf");
        when(apelacionRepository.save(any(Apelacion.class))).thenReturn(apelacion);

        Apelacion resultado = apelacionService.procesarSegundaCalificacion(1L, request, archivoMock);

        assertEquals(Apelacion.EstadoApelacion.RESUELTO_IMPROCEDENTE, resultado.getEstado());
        assertEquals(Apelacion.Calificacion.IMPROCEDENTE, resultado.getCalificacionSegunda());
        assertTrue(resultado.getResultado().contains("RECHAZO DEFINITIVO - IMPROCEDENTE"));
    }

    // --- ESCENARIO 3: NO PRESENTADO POR FALTA DE SUBSANACIÓN ---
    @Test
    void procesarSegundaCalificacion_NoPresentado_GeneraEstadoCorrecto() {
        request.setDecision("TENER_POR_NO_PRESENTADO");

        when(apelacionRepository.findById(1L)).thenReturn(Optional.of(apelacion));
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("ruta/dummy.pdf");
        when(apelacionRepository.save(any(Apelacion.class))).thenReturn(apelacion);

        Apelacion resultado = apelacionService.procesarSegundaCalificacion(1L, request, archivoMock);

        assertEquals(Apelacion.EstadoApelacion.TENER_POR_NO_PRESENTADO, resultado.getEstado());
    }

    // --- VALIDACIÓN 1: EXCLUSIVIDAD DE ESTADO ---
    @Test
    void procesarSegundaCalificacion_EstadoIncorrecto_LanzaExcepcion() {

        apelacion.setEstado(Apelacion.EstadoApelacion.EN_CALIFICACION_1);

        when(apelacionRepository.findById(1L)).thenReturn(Optional.of(apelacion));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            apelacionService.procesarSegundaCalificacion(1L, request, archivoMock);
        });

        assertEquals("La apelación no se encuentra en Segunda Calificación.", exception.getMessage());
    }

    // --- VALIDACIÓN 2: ADVERTENCIA DE PLAZO SUPERADO (> 7 días) ---
    @Test
    void procesarSegundaCalificacion_FueraDePlazo_AgregaAdvertencia() {
        request.setDecision("ADMISIBLE");
        // Simulamos que la subsanación fue hace 15 días (supera los 7 días hábiles)
        apelacion.setFechaSubsanacion(LocalDateTime.now().minusDays(15));

        when(apelacionRepository.findById(1L)).thenReturn(Optional.of(apelacion));
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("ruta/dummy.pdf");
        when(apelacionRepository.save(any(Apelacion.class))).thenReturn(apelacion);

        Apelacion resultado = apelacionService.procesarSegundaCalificacion(1L, request, archivoMock);

        // El proceso debe continuar sin error, pero inyectando la advertencia
        assertEquals(Apelacion.EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION, resultado.getEstado());
        assertTrue(resultado.getResultado().contains("FUERA DE PLAZO REGLAMENTARIO"));
    }

    // --- VALIDACIÓN 3: CONFIRMAR NOTIFICACIÓN (Avanza a En Resolución) ---
    @Test
    void confirmarNotificacion_EstadoCorrecto_AvanzaEnResolucion() {
        apelacion.setEstado(Apelacion.EstadoApelacion.NOTIFICACION_SEGUNDA_CALIFICACION);

        when(apelacionRepository.findById(1L)).thenReturn(Optional.of(apelacion));
        when(apelacionRepository.save(any(Apelacion.class))).thenReturn(apelacion);

        Apelacion resultado = apelacionService.confirmarNotificacionSegundaCalificacion(1L);

        assertEquals(Apelacion.EstadoApelacion.EN_RESOLUCION, resultado.getEstado());
    }
}