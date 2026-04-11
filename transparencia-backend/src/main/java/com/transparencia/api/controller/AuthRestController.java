package com.transparencia.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.transparencia.api.exception.RecursoNoEncontradoException;
import com.transparencia.api.model.dto.LoginRequestDTO;
import com.transparencia.api.model.dto.RegistroRequestDTO;
import com.transparencia.api.model.entity.Ciudadano;
import com.transparencia.api.model.entity.Usuario;
import com.transparencia.api.security.JwtService;
import com.transparencia.api.service.AdministradorService;
import com.transparencia.api.service.CiudadanoService;
import com.transparencia.api.service.FuncionarioService;
import com.transparencia.api.service.MiembroTTAIPService;
import com.transparencia.api.service.UsuarioService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Registro de ciudadanos y login con JWT")
public class AuthRestController {

    private final UsuarioService usuarioService;
    private final CiudadanoService ciudadanoService;
    private final FuncionarioService funcionarioService;
    private final MiembroTTAIPService miembroTTAIPService;
    private final AdministradorService administradorService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthRestController(
        UsuarioService usuarioService,
        CiudadanoService ciudadanoService,
        FuncionarioService funcionarioService,
        MiembroTTAIPService miembroTTAIPService,
        AdministradorService administradorService,
        JwtService jwtService,
        PasswordEncoder passwordEncoder
    ) {
        this.usuarioService = usuarioService;
        this.ciudadanoService = ciudadanoService;
        this.funcionarioService = funcionarioService;
        this.miembroTTAIPService = miembroTTAIPService;
        this.administradorService = administradorService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Iniciar sesion", description = "Autentica al usuario y retorna JWT con datos de perfil")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO request) {
        Optional<Usuario> usuarioOpt = resolverUsuario(request.identificador().trim());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "mensaje", "Usuario no encontrado"
            ));
        }

        Usuario usuario = usuarioOpt.get();

        if (!verificarPassword(request.password(), usuario)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "mensaje", "Contrasena incorrecta"
            ));
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "mensaje", "Usuario desactivado"
            ));
        }

        String token = jwtService.generateToken(
            usuario.getEmail(),
            usuario.getTipoUsuario().name(),
            usuario.getIdUsuario()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("usuarioId", usuario.getIdUsuario());
        response.put("email", usuario.getEmail());
        response.put("tipoUsuario", usuario.getTipoUsuario().name());

        switch (usuario.getTipoUsuario()) {
            case CIUDADANO -> {
                ciudadanoService.findById(usuario.getIdUsuario()).ifPresent(ciudadano -> {
                    response.put("nombre", ciudadano.getNombreCompleto());
                    response.put("dni", ciudadano.getDni());
                    response.put("ciudadanoId", ciudadano.getId());
                });
                response.put("redirectUrl", "/ciudadano/dashboard");
            }
            case FUNCIONARIO -> {
                funcionarioService.obtenerFuncionarioPorId(usuario.getIdUsuario()).ifPresent(funcionario -> {
                    response.put("nombre", funcionario.getNombreCompleto());
                    response.put("cargo", funcionario.getCargo());
                    response.put("funcionarioId", funcionario.getId());

                    if (funcionario.getEntidad() != null) {
                        response.put("entidadId", funcionario.getEntidad().getId());
                        response.put("entidadNombre", funcionario.getEntidad().getNombre());
                    }
                });
                response.put("redirectUrl", "/entidad/dashboard");
            }
            case TTAIP -> {
                miembroTTAIPService.obtenerMiembroTTAIPPorId(usuario.getIdUsuario()).ifPresent(miembro -> {
                    response.put("nombre", miembro.getNombreCompleto());
                    response.put("cargo", miembro.getCargo());
                    response.put("miembroId", miembro.getIdUsuario());
                });
                response.put("redirectUrl", "/ttaip/dashboard");
            }
            case ADMINISTRADOR -> {
                administradorService.obtenerAdministradorPorId(usuario.getIdUsuario()).ifPresent(administrador -> {
                    response.put("nombre", administrador.getNombreCompleto());
                    response.put("administradorId", administrador.getId());
                });
                response.put("redirectUrl", "/admin/dashboard");
            }
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Registrar ciudadano", description = "Registra un ciudadano con email, password y DNI")
    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registro(@Valid @RequestBody RegistroRequestDTO request) {
        if (usuarioService.obtenerUsuarioPorEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "mensaje", "Ya existe un usuario con ese email"
            ));
        }

        if (ciudadanoService.obtenerCiudadanoPorDni(request.dni()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "mensaje", "Ya existe un ciudadano con ese DNI"
            ));
        }

        Ciudadano ciudadano = new Ciudadano();
        ciudadano.setEmail(request.email().trim());
        ciudadano.setPassword(passwordEncoder.encode(request.password()));
        ciudadano.setTipoUsuario(Usuario.TipoUsuario.CIUDADANO);
        ciudadano.setActivo(true);
        ciudadano.setDni(request.dni());
        ciudadano.setNombreCompleto(request.nombreCompleto().trim());
        ciudadano.setTelefono(request.telefono());
        ciudadano.setDireccion(request.direccion());

        ciudadano = ciudadanoService.guardarCiudadano(ciudadano);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "mensaje", "Registro exitoso",
            "ciudadanoId", ciudadano.getId(),
            "email", ciudadano.getEmail()
        ));
    }

    @Operation(summary = "Verificar disponibilidad de email")
    @GetMapping("/verificar-email")
    public ResponseEntity<Map<String, Object>> verificarEmail(@RequestParam String email) {
        boolean existe = usuarioService.obtenerUsuarioPorEmail(email).isPresent();
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    @Operation(summary = "Verificar disponibilidad de DNI")
    @GetMapping("/verificar-dni")
    public ResponseEntity<Map<String, Object>> verificarDni(@RequestParam String dni) {
        boolean existe = ciudadanoService.obtenerCiudadanoPorDni(dni).isPresent();
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    @Operation(summary = "Obtener usuario", description = "Retorna datos basicos del usuario por usuarioId")
    @GetMapping("/me/{usuarioId}")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioActual(@PathVariable Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId));

        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", usuario.getIdUsuario());
        response.put("email", usuario.getEmail());
        response.put("tipoUsuario", usuario.getTipoUsuario().name());
        response.put("activo", usuario.getActivo());

        return ResponseEntity.ok(response);
    }

    private Optional<Usuario> resolverUsuario(String identificador) {
        if (identificador.contains("@")) {
            return usuarioService.obtenerUsuarioPorEmail(identificador);
        }

        if (identificador.matches("\\d{8}")) {
            return ciudadanoService.obtenerCiudadanoPorDni(identificador).map(ciudadano -> (Usuario) ciudadano);
        }

        return usuarioService.obtenerUsuarioPorEmail(identificador);
    }

    private boolean verificarPassword(String rawPassword, Usuario usuario) {
        String storedPassword = usuario.getPassword();
        if (storedPassword == null) {
            return false;
        }

        if (storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (storedPassword.equals(rawPassword)) {
            usuario.setPassword(passwordEncoder.encode(rawPassword));
            usuarioService.guardarUsuario(usuario);
            return true;
        }

        return false;
    }
}