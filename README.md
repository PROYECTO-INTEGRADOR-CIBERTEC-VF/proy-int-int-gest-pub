# proy-int-int-gest-pub
Sistema Integral de Acceso a la Información Pública

## Backend MySQL

El backend usa MySQL por defecto a través de variables de entorno:

- `DB_URL` (default: `jdbc:mysql://localhost:3306/saip_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima`)
- `DB_USERNAME` (default: `root`)
- `DB_PASSWORD` (default: vacío)
- `DB_DRIVER` (default: `com.mysql.cj.jdbc.Driver`)
- `JPA_DIALECT` (default: `org.hibernate.dialect.MySQLDialect`)

Ejecutar backend:

```powershell
cd transparencia-backend
.\mvnw.cmd spring-boot:run
```

## Contrato OpenAPI hacia Frontend

El backend expone el contrato en `http://localhost:8080/v3/api-docs`.

Para sincronizar OpenAPI al frontend y generar tipos TypeScript:

```powershell
./scripts/sync-openapi.ps1
```

Esto genera:

- `openapi/saip-openapi.json`
- `transparencia-frontend/src/app/api/schema.ts`

Script equivalente en Linux/macOS:

```bash
./scripts/sync-openapi.sh
```
