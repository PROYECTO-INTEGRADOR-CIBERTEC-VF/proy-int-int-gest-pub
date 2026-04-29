package com.transparencia.api.service;

import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.repository.ApelacionRepository;
import com.transparencia.api.repository.CiudadanoRepository;
import com.transparencia.api.repository.DocumentoRepository;
import com.transparencia.api.repository.SolicitudRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApelacionServiceTest {

    // --- MOCKS --
    @Mock
    private ApelacionRepository apelacionRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private CiudadanoRepository ciudadanoRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @InjectMocks
    private ApelacionService apelacionService;


    // TESTS PARA HU-08 (BE-04): EMITIR RESOLUCIÓN FINAL


    // SCENARIO 1 y 3: Cada tipo de resolución genera el estado correcto en la apelación
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
            "fundado, RESUELTO_FUNDADO, RESUELTO - FUNDADO",
            "fundado_en_parte, RESUELTO_FUNDADO_EN_PARTE, RESUELTO - FUNDADO EN PARTE",
            "infundado, RESUELTO_INFUNDADO, RESUELTO - INFUNDADO",
            "infundado_en_parte, RESUELTO_INFUNDADO_EN_PARTE, RESUELTO - INFUNDADO EN PARTE",
            "improcedente, RESUELTO_IMPROCEDENTE, RESUELTO - IMPROCEDENTE",
            "sustraccion_materia, CONCLUSION_SUSTRACCION_MATERIA, CONCLUSIÓN POR SUSTRACCIÓN DE MATERIA",
            "desistimiento, CONCLUSION_DESISTIMIENTO, CONCLUSIÓN POR DESISTIMIENTO"
    })
    void emitirResolucionFinal_TiposDeResolucion_GeneranEstadoCorrecto(String decision, String estadoEnumEsperado, String resultadoEsperado) {
        Long idApelacion = 1L;
        Apelacion apelacion = new Apelacion();
        apelacion.setIdApelacion(idApelacion);
        apelacion.setEstado(Apelacion.EstadoApelacion.EN_RESOLUCION);
        apelacion.setFechaApelacion(java.time.LocalDateTime.now().minusDays(5));

        com.transparencia.api.model.dto.ResolucionFinalRequest request = new com.transparencia.api.model.dto.ResolucionFinalRequest();
        request.setDecision(decision);
        request.setFundamentos("Fundamentos de prueba");
        request.setIniciarProcesoDisciplinario(false);

        org.springframework.web.multipart.MultipartFile archivo =
                new org.springframework.mock.web.MockMultipartFile("file", "res.pdf", "application/pdf", "PDF".getBytes());

        org.mockito.Mockito.when(apelacionRepository.findById(idApelacion)).thenReturn(java.util.Optional.of(apelacion));
        org.mockito.Mockito.when(fileStorageService.storeFile(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString())).thenReturn("ruta.pdf");
        org.mockito.Mockito.when(apelacionRepository.save(org.mockito.ArgumentMatchers.any(Apelacion.class)))
                .thenAnswer(i -> i.getArgument(0));

        Apelacion resultado = apelacionService.emitirResolucionFinal(idApelacion, request, archivo);

        org.junit.jupiter.api.Assertions.assertEquals(Apelacion.EstadoApelacion.valueOf(estadoEnumEsperado), resultado.getEstado());
        org.junit.jupiter.api.Assertions.assertEquals(resultadoEsperado, resultado.getResultado());
    }

    // SCENARIO 2: Una resolución final no puede registrarse dos veces
    @org.junit.jupiter.api.Test
    void emitirResolucionFinal_ApelacionYaResuelta_LanzaExcepcion() {
        Long idApelacion = 2L;
        Apelacion apelacion = new Apelacion();
        apelacion.setIdApelacion(idApelacion);
        apelacion.setEstado(Apelacion.EstadoApelacion.RESUELTO_FUNDADO);

        com.transparencia.api.model.dto.ResolucionFinalRequest request = new com.transparencia.api.model.dto.ResolucionFinalRequest();
        request.setDecision("FUNDADO");
        org.springframework.web.multipart.MultipartFile archivo =
                new org.springframework.mock.web.MockMultipartFile("file", "res.pdf", "application/pdf", "PDF".getBytes());

        org.mockito.Mockito.when(apelacionRepository.findById(idApelacion)).thenReturn(java.util.Optional.of(apelacion));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            apelacionService.emitirResolucionFinal(idApelacion, request, archivo);
        }, "Debería rechazar si la apelación ya no está en etapa de resolución");
    }

    // SCENARIO EXTRA: El sistema advierte cuando se supera el plazo para resolver (BE-02)
    @org.junit.jupiter.api.Test
    void emitirResolucionFinal_FueraDePlazo_GeneraAdvertencia() {
        Long idApelacion = 3L;
        Apelacion apelacion = new Apelacion();
        apelacion.setIdApelacion(idApelacion);
        apelacion.setEstado(Apelacion.EstadoApelacion.EN_RESOLUCION);
        apelacion.setFechaApelacion(java.time.LocalDateTime.now().minusDays(15));

        com.transparencia.api.model.dto.ResolucionFinalRequest request = new com.transparencia.api.model.dto.ResolucionFinalRequest();
        request.setDecision("FUNDADO");
        request.setFundamentos("Prueba");
        org.springframework.web.multipart.MultipartFile archivo =
                new org.springframework.mock.web.MockMultipartFile("file", "res.pdf", "application/pdf", "PDF".getBytes());

        org.mockito.Mockito.when(apelacionRepository.findById(idApelacion)).thenReturn(java.util.Optional.of(apelacion));
        org.mockito.Mockito.when(fileStorageService.storeFile(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString())).thenReturn("ruta.pdf");
        org.mockito.Mockito.when(apelacionRepository.save(org.mockito.ArgumentMatchers.any(Apelacion.class)))
                .thenAnswer(i -> i.getArgument(0));

        Apelacion resultado = apelacionService.emitirResolucionFinal(idApelacion, request, archivo);

        org.junit.jupiter.api.Assertions.assertTrue(resultado.getResultado().contains("ALERTA: FUERA DE PLAZO"),
                "El resultado debe contener la alerta por superar los 10 días");
    }
}