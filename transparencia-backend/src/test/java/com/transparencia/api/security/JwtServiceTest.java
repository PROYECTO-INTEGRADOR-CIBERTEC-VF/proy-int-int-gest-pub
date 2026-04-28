package com.transparencia.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "UnaClaveSuperSecretaQueTengaAlMenos32CaracteresParaHMACSHA256");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);
    }

    @Test
    void generateToken_y_extraerClaims_debeFuncionar() {
        String token = jwtService.generateToken("usuario@saip.gob.pe", "CIUDADANO", 15L);

        assertNotNull(token);
        assertEquals("usuario@saip.gob.pe", jwtService.extractEmail(token));
        assertEquals("CIUDADANO", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_conTokenInvalido_debeRetornarFalse() {
        assertFalse(jwtService.isTokenValid("token-invalido"));
    }
}
