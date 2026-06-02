# Banco Digital Backend

Backend REST para el proyecto Banco Digital (CodeFactory UdeA), construido con Spring Boot, PostgreSQL y seguridad JWT.

## Estado Actual

Este repositorio ya incluye:

- Autenticacion JWT stateless.
- Registro de usuarios cliente.
- Login con emision de token.
- HU3 implementada: actualizacion de datos personales del cliente autenticado.
- Manejo global de errores con formato estandar.
- Auditoria JPA (`created_at`, `updated_at`).
- Versionamiento de BD con Flyway y migracion inicial de indices.
- Swagger/OpenAPI con `Authorize` para probar endpoints protegidos.

## Stack Tecnico

- Java 25
- Spring Boot 4.0.4
- Spring Web, Spring Data JPA, Spring Security
- JWT (jjwt)
- PostgreSQL
- Flyway
- Springdoc OpenAPI (Swagger UI)
- Actuator + Prometheus
- Maven Wrapper

## Estructura Principal

```text
src/main/java/co/edu/udea/bancodigital
	config/
	controllers/
	dtos/
	exception/
	models/
	repositories/
	services/

src/main/resources
	application.properties
	db/migration/
```

## Requisitos Previos

- JDK 21
- Maven (opcional, se recomienda usar `mvnw`)
- PostgreSQL accesible (actualmente configurado para Neon)

## Variables y Configuracion

La aplicacion requiere al menos esta variable de entorno:

- `JWT_SECRET_KEY`: clave Base64 usada para firmar JWT.

Ejemplo en PowerShell:

```powershell
$env:JWT_SECRET_KEY="TU_CLAVE_BASE64_AQUI"
```

Notas:

- La conexion a BD esta definida en `application.properties` para entorno de desarrollo.
- Para despliegue real, se recomienda sobreescribir credenciales por variables de entorno (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).

## Ejecutar Proyecto

### Windows (PowerShell)

```powershell
$env:JWT_SECRET_KEY="TU_CLAVE_BASE64_AQUI"
.\mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
export JWT_SECRET_KEY="TU_CLAVE_BASE64_AQUI"
./mvnw spring-boot:run
```

La API queda disponible en:

- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Migraciones con Flyway

Flyway esta habilitado y se ejecuta en arranque.

- Carpeta de migraciones: `src/main/resources/db/migration`
- Migracion actual: `V1__indexes_core.sql`
- Tabla de control en BD: `flyway_schema_history`

Consulta util para validar estado:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## Seguridad

- Endpoints publicos:
	- `POST /api/v1/auth/login`
	- `POST /api/v1/usuarios/registro`
	- Rutas de Swagger/OpenAPI
- Endpoints protegidos:
	- `PUT /api/v1/usuarios/me`
	- Cualquier otra ruta no marcada como publica
- Roles:
	- Soporte para RBAC basado en catalogo `roles`.

Para endpoints protegidos usar header:

```http
Authorization: Bearer <token>
```

## Endpoints Implementados

### 1) Registro de usuario

`POST /api/v1/usuarios/registro`

Request ejemplo:

```json
{
	"idTipoDoc": 1,
	"numeroDocumento": "1032456789",
	"nombre": "Camilo",
	"primerApellido": "Mosquera",
	"segundoApellido": "Lopez",
	"direccion": "Calle 10 #20-30",
	"telefono": "3001234567",
	"correo": "camilo@example.com",
	"contrasena": "ClaveSegura1!"
}
```

### 2) Login

`POST /api/v1/auth/login`

Request ejemplo:

```json
{
	"correo": "camilo@example.com",
	"contrasena": "ClaveSegura1!"
}
```

Response ejemplo:

```json
{
	"token": "<jwt>",
	"tipo": "Bearer",
	"nombre": "Camilo",
	"correo": "camilo@example.com",
	"idRol": 2,
	"rol": "CLIENTE"
}
```

### 3) HU3 - Actualizar datos cliente

`PUT /api/v1/usuarios/me` (requiere JWT)

Request ejemplo:

```json
{
	"nombre": "Camilo Andres",
	"primerApellido": "Mosquera",
	"segundoApellido": "Lopez",
	"direccion": "Cra 45 #50-20",
	"telefono": "3009876543",
	"correo": "camilo.andres@example.com"
}
```

Comportamiento implementado:

- Actualiza solo los campos personales permitidos.
- Valida formatos (correo, telefono, nombres).
- Impide correo duplicado.
- Mantiene datos sensibles fuera de la operacion.
- Retorna `updatedAt` para trazabilidad.

## HU3 - Criterios funcionales cubiertos

### Escenario exitoso

- Cliente autenticado.
- Modifica datos personales validos.
- El sistema persiste cambios y responde exito.

### Escenario no exitoso

- Cliente autenticado.
- Envia datos invalidos.
- El sistema rechaza con `400 Bad Request` y detalle de validacion.

## Formato de Errores

Las respuestas de error siguen un formato estandar:

```json
{
	"errorCode": "VALIDATION_ERROR",
	"message": "Error de validacion en la solicitud",
	"details": "telefono: El telefono debe ser celular colombiano: 10 digitos iniciando en 3",
	"traceId": "uuid",
	"timestamp": "2026-04-05T13:20:00"
}
```

## Comandos Utiles

```bash
# Compilar y validar
./mvnw -DskipTests validate

# Ejecutar pruebas
./mvnw test
```

En Windows reemplazar `./mvnw` por `.\\mvnw.cmd`.

## Proximos pasos

- Agregar migracion Flyway V2 para procedimiento almacenado.
- Aumentar cobertura automatizada para HU3 (unitaria e integracion).
- Externalizar secretos y credenciales en todos los ambientes.
