-- 1. Insertar en la tabla base 'usuarios'
-- La contraseña es: admin123
INSERT INTO usuarios (email, password, tipo_usuario, activo, fecha_registro)
VALUES ('ttaip@test.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqCyO', 'TTAIP', true, NOW());

-- 2. Insertar una apelación de prueba (ID 1) en el estado correcto
INSERT INTO apelaciones (id_apelacion, expediente, estado, fecha_apelacion, resultado)
VALUES (1, 'EXP-2024-007', 'EN_CALIFICACION_2', NOW(), 'PENDIENTE SEGUNDA CALIFICACIÓN');