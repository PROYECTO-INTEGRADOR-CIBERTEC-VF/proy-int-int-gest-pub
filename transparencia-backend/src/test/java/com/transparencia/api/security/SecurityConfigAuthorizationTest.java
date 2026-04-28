package com.transparencia.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rutaPublicaAuth_sinToken_noDebeSerBloqueadaPorSeguridad() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void rutasProtegidas_sinToken_debenRechazarAcceso() throws Exception {
        mockMvc.perform(get("/api/solicitudes"))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                assertTrue(status == 401 || status == 403);
            });
    }

    @Test
    void postSolicitudes_conRolNoPermitido_debeRetornarForbidden() throws Exception {
        mockMvc.perform(post("/api/solicitudes")
            .with(user("funcionario@saip.gob.pe").roles("FUNCIONARIO"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void postSolicitudes_conRolPermitido_debePasarSeguridad() throws Exception {
        mockMvc.perform(post("/api/solicitudes")
            .with(user("ciudadano@saip.gob.pe").roles("CIUDADANO"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void rutaTtaip_conRolNoPermitido_debeRetornarForbidden() throws Exception {
        mockMvc.perform(get("/api/ttaip/estadisticas")
            .with(user("funcionario@saip.gob.pe").roles("FUNCIONARIO")))
            .andExpect(status().isForbidden());
    }

    @Test
    void rutaTtaip_conRolPermitido_debePasarSeguridad() throws Exception {
        mockMvc.perform(get("/api/ttaip/estadisticas")
            .with(user("miembro@saip.gob.pe").roles("TTAIP")))
            .andExpect(status().isOk());
    }
}
