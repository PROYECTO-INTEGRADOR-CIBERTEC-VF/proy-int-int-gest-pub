package com.transparencia.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.transparencia.api.model.entity.Ciudadano;
import com.transparencia.api.model.entity.Usuario;
import com.transparencia.api.security.JwtService;
import com.transparencia.api.service.AdministradorService;
import com.transparencia.api.service.CiudadanoService;
import com.transparencia.api.service.FuncionarioService;
import com.transparencia.api.service.MiembroTTAIPService;
import com.transparencia.api.service.UsuarioService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UsuarioService usuarioService;

    @MockitoBean
    CiudadanoService ciudadanoService;

    @MockitoBean
    FuncionarioService funcionarioService;

    @MockitoBean
    MiembroTTAIPService miembroTTAIPService;

    @MockitoBean
    AdministradorService administradorService;

    @MockitoBean
    JwtService jwtService;

        @MockitoBean
        UserDetailsService userDetailsService;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encodedHash");
    }

    private Ciudadano crearCiudadano(String password) {
        Ciudadano ciudadano = new Ciudadano();
        ciudadano.setIdUsuario(1L);
        ciudadano.setEmail("ciudadano@test.com");
        ciudadano.setPassword(password);
        ciudadano.setTipoUsuario(Usuario.TipoUsuario.CIUDADANO);
        ciudadano.setActivo(true);
        ciudadano.setFechaRegistro(LocalDateTime.now());
        ciudadano.setDni("12345678");
        ciudadano.setNombreCompleto("Juan Perez");
        return ciudadano;
    }

    @Test
    void loginConDniValido_debeRetornar200YToken() throws Exception {
        Ciudadano ciudadano = crearCiudadano("$2a$12$hashedpassword");
        when(ciudadanoService.obtenerCiudadanoPorDni("12345678")).thenReturn(Optional.of(ciudadano));
        when(ciudadanoService.findById(1L)).thenReturn(Optional.of(ciudadano));
        when(passwordEncoder.matches("pass123", "$2a$12$hashedpassword")).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString(), any())).thenReturn("test.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"identificador":"12345678","password":"pass123"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }

    @Test
    void loginConEmail_debeRetornar200YToken() throws Exception {
        Ciudadano ciudadano = crearCiudadano("$2a$12$hashedpassword");
        when(usuarioService.obtenerUsuarioPorEmail("ciudadano@test.com")).thenReturn(Optional.of(ciudadano));
        when(ciudadanoService.findById(1L)).thenReturn(Optional.of(ciudadano));
        when(passwordEncoder.matches("pass123", "$2a$12$hashedpassword")).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString(), any())).thenReturn("test.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"identificador":"ciudadano@test.com","password":"pass123"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }

    @Test
    void loginPasswordIncorrecto_debeRetornar401() throws Exception {
        Ciudadano ciudadano = crearCiudadano("$2a$12$hashedpassword");
        when(usuarioService.obtenerUsuarioPorEmail("ciudadano@test.com")).thenReturn(Optional.of(ciudadano));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"identificador":"ciudadano@test.com","password":"wrongpass"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginUsuarioInexistente_debeRetornar401() throws Exception {
        when(usuarioService.obtenerUsuarioPorEmail("noexiste@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"identificador":"noexiste@test.com","password":"pass123"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginUsuarioDesactivado_debeRetornar401() throws Exception {
        Ciudadano ciudadano = crearCiudadano("$2a$12$hashedpassword");
        ciudadano.setActivo(false);
        when(usuarioService.obtenerUsuarioPorEmail("ciudadano@test.com")).thenReturn(Optional.of(ciudadano));
        when(passwordEncoder.matches("pass123", "$2a$12$hashedpassword")).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"identificador":"ciudadano@test.com","password":"pass123"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void registroExitoso_debeRetornar201() throws Exception {
        when(usuarioService.obtenerUsuarioPorEmail("nuevo@test.com")).thenReturn(Optional.empty());
        when(ciudadanoService.obtenerCiudadanoPorDni("87654321")).thenReturn(Optional.empty());
        Ciudadano ciudadano = crearCiudadano("$2a$12$encoded");
        ciudadano.setEmail("nuevo@test.com");
        when(ciudadanoService.guardarCiudadano(any())).thenReturn(ciudadano);

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"nuevo@test.com","password":"pass123","dni":"87654321","nombreCompleto":"Maria Garcia"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void registroEmailDuplicado_debeRetornar400() throws Exception {
        when(usuarioService.obtenerUsuarioPorEmail("existe@test.com")).thenReturn(Optional.of(crearCiudadano("hash")));

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"existe@test.com","password":"pass123","dni":"87654321","nombreCompleto":"Maria Garcia"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void registroDniInvalido_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"nuevo@test.com","password":"pass123","dni":"123","nombreCompleto":"Maria Garcia"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void registroPasswordCorta_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"nuevo@test.com","password":"ab","dni":"87654321","nombreCompleto":"Maria Garcia"}
                    """))
            .andExpect(status().isBadRequest());
    }
}
