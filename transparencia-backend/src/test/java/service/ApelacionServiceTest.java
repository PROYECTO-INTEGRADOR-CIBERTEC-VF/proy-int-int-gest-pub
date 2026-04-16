package com.transparencia.api.service;

import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.Ciudadano;
import com.transparencia.api.model.entity.EstadoApelacion;
import com.transparencia.api.model.entity.Solicitud;
import com.transparencia.api.repository.ApelacionRepository;
import com.transparencia.api.repository.CiudadanoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApelacionServiceTest {

    @Mock
    private ApelacionRepository apelacionRepository;

    @Mock
    private CiudadanoRepository ciudadanoRepository;

    @InjectMocks
    private ApelacionService apelacionService;

    private Apelacion apelacion;
    private Ciudadano ciudadano;
    private Solicitud solicitud;

    @BeforeEach
    void setUp() {
        // Preparamos los datos base antes de cada prueba
        ciudadano = new Ciudadano();
        ciudadano.setIdUsuario(1L);

        solicitud = new Solicitud();
        solicitud.setIdSolicitud(1L);

        apelacion = new Apelacion();
        apelacion.setIdApelacion(1L);
        apelacion.setCiudadano(ciudadano);
        apelacion.setSolicitud(solicitud);
        apelacion.setFundamentos("Fundamentos de prueba");
        apelacion.setEstado(EstadoApelacion.PENDIENTE_ELEVACION);
    }

    // 1 y 4. Test: Crear apelación exitosa y generarExpediente
    @Test
    void testCrearApelacionExitosaYGenerarExpediente() {
        // Arrange
        apelacion.setExpediente(null);
        when(apelacionRepository.existsBySolicitud_IdSolicitud(1L)).thenReturn(false);
        when(ciudadanoRepository.findById(1L)).thenReturn(Optional.of(ciudadano));
        when(apelacionRepository.count()).thenReturn(5L); // Simulamos 5 previas
        when(apelacionRepository.save(any(Apelacion.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Apelacion result = apelacionService.save(apelacion);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getExpediente());
        
        int currentYear = LocalDate.now().getYear();
        String expectedExpediente = String.format("00006-%d-JUS-TTAIP", currentYear);
        
        assertEquals(expectedExpediente, result.getExpediente());
        verify(apelacionRepository, times(1)).save(apelacion);
    }

    // 2. Test: Apelación Duplicada (Lanza Excepción)
    @Test
    void testCrearApelacionDuplicadaLanzaExcepcion() {
        // Arrange
        when(apelacionRepository.existsBySolicitud_IdSolicitud(1L)).thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> apelacionService.save(apelacion));
        assertEquals("La solicitud ya tiene una apelación en curso.", ex.getMessage());
        verify(apelacionRepository, never()).save(any());
    }

    // 3. Test: Ciudadano Inexistente (Lanza Excepción)
    @Test
    void testCrearApelacionCiudadanoInexistenteLanzaExcepcion() {
        // Arrange
        when(apelacionRepository.existsBySolicitud_IdSolicitud(1L)).thenReturn(false);
        when(ciudadanoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> apelacionService.save(apelacion));
        assertEquals("El ciudadano no existe en el sistema.", ex.getMessage());
        verify(apelacionRepository, never()).save(any());
    }

    // 5. Test: Cambiar estado
    @Test
    void testCambiarEstado() {
        // Arrange
        when(apelacionRepository.findById(1L)).thenReturn(Optional.of(apelacion));
        when(apelacionRepository.save(any(Apelacion