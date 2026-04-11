package com.transparencia.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transparencia.api.model.entity.Ciudadano;
import com.transparencia.api.model.entity.Usuario;
import com.transparencia.api.security.JwtService;
import com.transparencia.api.service.AdministradorService;
import com.transparencia.api.service.CiudadanoService;
import com.transparencia.api.service.FuncionarioService;
import com.transparencia.api.service.MiembroTTAIPService;
import com.transparencia.api.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private CiudadanoService ciudadanoService;

    @MockitoBean
    private FuncionarioService funcionarioService;

    @MockitoBean
    private MiembroTTAIPService miembroTTAIPService;

    @MockitoBean
    private AdministradorService administradorService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void loginConDniValido_debeRetornar200YToken() throws Exception {
        Ciudadano ciudadano = ciudadanoActivo(1L, "ciudadano@saip.gob.pe", "12345678", "$2a$hash");

        when(ciudadanoService.obtenerCiudadanoPorDni("12345678")).thenReturn(Optional.of(ciudadano));
        when(passwordEncoder.matches("ClaveSegura1", "$2a$hash")).thenReturn(true);
        when(jwtService.generateToken("ciudadano@saip.gob.pe", "CIUDADANO", 1L)).thenReturn("jwt-dni-token");
        when(ciudadanoService.findById(1L)).thenReturn(Optional.of(ciudadano));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "identificador", "12345678",
                    "password", "ClaveSegura1"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.token").value("jwt-dni-token"));
    }

    @Test
    void loginConEmailValido_debeRetornar200YToken() throws Exception {
        Usuario admin = new Usuario();
        admin.setIdUsuario(2L);
        admin.setEmail("admin@saip.gob.pe");
        admin.setPassword("$2a$adminHash");
        admin.setTipoUsuario(Usuario.TipoUsuario.ADMINISTRADOR);
        admin.setActivo(true);

        when(usuarioService.obtenerUsuarioPorEmail("admin@saip.gob.pe")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("Admin123", "$2a$adminHash")).thenReturn(true);
        when(jwtService.generateToken("admin@saip.gob.pe", "ADMINISTRADOR", 2L)).thenReturn("jwt-email-token");
        when(administradorService.obtenerAdministradorPorId(2L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "identificador", "admin@saip.gob.pe",
                    "password", "Admin123"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.token").value("jwt-email-token"));
    }

    @Test
    void loginConPasswordIncorrecto_debeRetornar401() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(3L);
        usuario.setEmail("user@saip.gob.pe");
        usuario.setPassword("$2a$hashIncorrecto");
        usuario.setTipoUsuario(Usuario.TipoUsuario.ADMINISTRADOR);
        usuario.setActivo(true);

        when(usuarioService.obtenerUsuarioPorEmail("user@saip.gob.pe")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("NoCoincide", "$2a$hashIncorrecto")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "identificador", "user@saip.gob.pe",
                    "password", "NoCoincide"
                ))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginConUsuarioInexistente_debeRetornar401() throws Exception {
        when(usuarioService.obtenerUsuarioPorEmail("noexiste@saip.gob.pe")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "identificador", "noexiste@saip.gob.pe",
                    "password", "Clave123"
                ))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginConUsuarioDesactivado_debeRetornar401() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(4L);
        usuario.setEmail("desactivado@saip.gob.pe");
        usuario.setPassword("$2a$hashActivo");
        usuario.setTipoUsuario(Usuario.TipoUsuario.ADMINISTRADOR);
        usuario.setActivo(false);

        when(usuarioService.obtenerUsuarioPorEmail("desactivado@saip.gob.pe")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave123", "$2a$hashActivo")).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "identificador", "desactivado@saip.gob.pe",
                    "password", "Clave123"
                ))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void registroExitoso_debeRetornar201() throws Exception {
        when(usuarioService.obtenerUsuarioPorEmail("nuevo@saip.gob.pe")).thenReturn(Optional.empty());
        when(ciudadanoService.obtenerCiudadanoPorDni("87654321")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("ClaveSegura1")).thenReturn("$2a$encoded");
        when(ciudadanoService.guardarCiudadano(any(Ciudadano.class))).thenAnswer(invocation -> {
            Ciudadano guardado = invocation.getArgument(0);
            guardado.setIdUsuario(10L);
            return guardado;
        });

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "nuevo@saip.gob.pe",
                    "password", "ClaveSegura1",
                    "dni", "87654321",
                    "nombreCompleto", "Nuevo Ciudadano",
                    "telefono", "999888777",
                    "direccion", "Av. Lima 123"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.ciudadanoId").value(10));
    }

    @Test
    void registroConEmailDuplicado_debeRetornar400() throws Exception {
        Usuario existente = new Usuario();
        existente.setIdUsuario(20L);
        existente.setEmail("duplicado@saip.gob.pe");

        when(usuarioService.obtenerUsuarioPorEmail("duplicado@saip.gob.pe")).thenReturn(Optional.of(existente));

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "duplicado@saip.gob.pe",
                    "password", "ClaveSegura1",
                    "dni", "12345678",
                    "nombreCompleto", "Usuario Duplicado",
                    "telefono", "999111222",
                    "direccion", "Av. Peru 123"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void registroConDniInvalido_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "dniinvalido@saip.gob.pe",
                    "password", "ClaveSegura1",
                    "dni", "123",
                    "nombreCompleto", "Ciudadano Invalido",
                    "telefono", "999111222",
                    "direccion", "Av. Peru 123"
                ))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void registroConPasswordCorta_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "passcorto@saip.gob.pe",
                    "password", "12345",
                    "dni", "12345678",
                    "nombreCompleto", "Ciudadano Password Corta",
                    "telefono", "999111222",
                    "direccion", "Av. Peru 123"
                ))))
            .andExpect(status().isBadRequest());
    }

    private Ciudadano ciudadanoActivo(Long id, String email, String dni, String password) {
        Ciudadano ciudadano = new Ciudadano();
        ciudadano.setIdUsuario(id);
        ciudadano.setEmail(email);
        ciudadano.setDni(dni);
        ciudadano.setPassword(password);
        ciudadano.setTipoUsuario(Usuario.TipoUsuario.CIUDADANO);
        ciudadano.setActivo(true);
        ciudadano.setNombreCompleto("Ciudadano Prueba");
        return ciudadano;
    }
}
