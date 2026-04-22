package com.transparencia.api.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.transparencia.api.model.entity.Usuario;
import com.transparencia.api.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_conUsuarioActivo_debeConstruirUserDetails() {
        Usuario usuario = new Usuario();
        usuario.setEmail("funcionario@saip.gob.pe");
        usuario.setPassword("hash");
        usuario.setTipoUsuario(Usuario.TipoUsuario.FUNCIONARIO);
        usuario.setActivo(true);
        when(usuarioRepository.findByEmail("funcionario@saip.gob.pe")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("funcionario@saip.gob.pe");

        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_FUNCIONARIO".equals(a.getAuthority())));
    }

    @Test
    void loadUserByUsername_conUsuarioInactivo_debeQuedarDeshabilitado() {
        Usuario usuario = new Usuario();
        usuario.setEmail("ttaip@saip.gob.pe");
        usuario.setPassword("hash");
        usuario.setTipoUsuario(Usuario.TipoUsuario.TTAIP);
        usuario.setActivo(false);
        when(usuarioRepository.findByEmail("ttaip@saip.gob.pe")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("ttaip@saip.gob.pe");

        assertFalse(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_conUsuarioNoEncontrado_debeLanzarExcepcion() {
        when(usuarioRepository.findByEmail("noexiste@saip.gob.pe")).thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("noexiste@saip.gob.pe")
        );
    }
}
