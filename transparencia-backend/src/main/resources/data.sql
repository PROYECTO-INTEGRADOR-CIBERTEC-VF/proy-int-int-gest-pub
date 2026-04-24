-- 1. Insertar en la tabla base 'usuarios' (Funciona en MySQL y H2)
INSERT INTO usuarios (email, password, tipo_usuario, activo, fecha_registro)
VALUES ('ttaip@test.com', '$2a$10$Y0iRy1prmCuSBvmhPsDKCO1r3fx5cCwPqVw7kmv5VnNKH8QqhDCJq', 'TTAIP', true, CURRENT_TIMESTAMP);

-- 2. Insertar una apelación de prueba (Funciona en MySQL y H2)
INSERT INTO apelaciones (expediente, estado, fecha_apelacion, resultado)
VALUES ('EXP-2024-007', 'EN_CALIFICACION_2', CURRENT_TIMESTAMP, 'PENDIENTE SEGUNDA CALIFICACIÓN');