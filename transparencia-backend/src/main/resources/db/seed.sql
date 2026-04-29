-- Seed de accesos y entidades publicas para SAIP
-- Uso: ejecutar sobre la BD saipcore_db (MySQL) DESPUES de iniciar el backend una vez
-- Nota: este script es idempotente (usa ON DUPLICATE KEY UPDATE)

START TRANSACTION;

SET @now := NOW();

-- 1) Entidades publicas
INSERT INTO entidades (
    nombre,
    acronimo,
    descripcion,
    fecha_registro,
    activa,
    direccion,
    telefono,
    email,
    estado
) VALUES
    (
        'Ministerio de Justicia y Derechos Humanos',
        'MINJUSDH',
        'Entidad publica rectora en materia de justicia y derechos humanos.',
        @now,
        1,
        'Calle Scipion Llona 350, Miraflores, Lima',
        '014224000',
        'mesadepartes@minjus.gob.pe',
        'Activa'
    ),
    (
        'Ministerio de Salud',
        'MINSA',
        'Entidad publica rectora en materia de salud.',
        @now,
        1,
        'Av. Salaverry 801, Jesus Maria, Lima',
        '013151500',
        'contacto@minsa.gob.pe',
        'Activa'
    ),
    (
        'Ministerio de Educacion',
        'MINEDU',
        'Entidad publica rectora en materia educativa.',
        @now,
        1,
        'Calle Del Comercio 193, San Borja, Lima',
        '016151800',
        'atencionalciudadano@minedu.gob.pe',
        'Activa'
    )
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    descripcion = VALUES(descripcion),
    direccion = VALUES(direccion),
    telefono = VALUES(telefono),
    email = VALUES(email),
    activa = VALUES(activa),
    estado = VALUES(estado);

-- Obtener una entidad existente para vincular al funcionario
SELECT id_entidad INTO @id_entidad_funcionario
FROM entidades
WHERE acronimo = 'MINJUSDH'
LIMIT 1;

-- 2) Acceso CIUDADANO
INSERT INTO usuarios (
    email,
    password,
    tipo_usuario,
    fecha_registro,
    activo
) VALUES (
    'ciudadano.demo@saip.gob.pe',
    'Demo12345*',
    'CIUDADANO',
    @now,
    1
)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    tipo_usuario = VALUES(tipo_usuario),
    activo = VALUES(activo);

SELECT id_usuario INTO @id_usuario_ciudadano
FROM usuarios
WHERE email = 'ciudadano.demo@saip.gob.pe'
LIMIT 1;

INSERT INTO ciudadanos (
    id_usuario,
    dni,
    nombre_completo,
    telefono,
    direccion
) VALUES (
    @id_usuario_ciudadano,
    '12345678',
    'Ciudadano Demo SAIP',
    '999111222',
    'Av. Los Ciudadanos 123, Lima'
)
ON DUPLICATE KEY UPDATE
    dni = VALUES(dni),
    nombre_completo = VALUES(nombre_completo),
    telefono = VALUES(telefono),
    direccion = VALUES(direccion);

-- 3) Acceso FUNCIONARIO (vinculado a una entidad publica)
INSERT INTO usuarios (
    email,
    password,
    tipo_usuario,
    fecha_registro,
    activo
) VALUES (
    'funcionario.minjusdh@saip.gob.pe',
    'Demo12345*',
    'FUNCIONARIO',
    @now,
    1
)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    tipo_usuario = VALUES(tipo_usuario),
    activo = VALUES(activo);

SELECT id_usuario INTO @id_usuario_funcionario
FROM usuarios
WHERE email = 'funcionario.minjusdh@saip.gob.pe'
LIMIT 1;

INSERT INTO funcionarios (
    id_usuario,
    nombre_completo,
    cargo,
    area,
    telefono,
    id_entidad
) VALUES (
    @id_usuario_funcionario,
    'Funcionario Demo Entidad',
    'Analista de Transparencia',
    'Oficina de Acceso a la Informacion',
    '987654321',
    @id_entidad_funcionario
)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    cargo = VALUES(cargo),
    area = VALUES(area),
    telefono = VALUES(telefono),
    id_entidad = VALUES(id_entidad);

-- 4) Acceso MIEMBRO TTAIP
INSERT INTO usuarios (
    email,
    password,
    tipo_usuario,
    fecha_registro,
    activo
) VALUES (
    'vocal.ttaip@saip.gob.pe',
    'Demo12345*',
    'TTAIP',
    @now,
    1
)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    tipo_usuario = VALUES(tipo_usuario),
    activo = VALUES(activo);

SELECT id_usuario INTO @id_usuario_ttaip
FROM usuarios
WHERE email = 'vocal.ttaip@saip.gob.pe'
LIMIT 1;

INSERT INTO miembros_ttaip (
    id_usuario,
    nombre_completo,
    cargo,
    sala,
    especialidad
) VALUES (
    @id_usuario_ttaip,
    'Vocal Demo TTAIP',
    'Vocal',
    'Primera Sala',
    'Transparencia y acceso a la informacion publica'
)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    cargo = VALUES(cargo),
    sala = VALUES(sala),
    especialidad = VALUES(especialidad);

COMMIT;

-- ================================================
-- Credenciales demo creadas por este script:
-- CIUDADANO:   ciudadano.demo@saip.gob.pe / Demo12345*
-- FUNCIONARIO: funcionario.minjusdh@saip.gob.pe / Demo12345*
-- TTAIP:       vocal.ttaip@saip.gob.pe / Demo12345*
-- ================================================