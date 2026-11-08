# SantanDar

Sistema web para la gestión de inventario y trazabilidad de donaciones mediante **cajas virtuales** identificadas con **código QR**.

## Roles

- **Administrador:** registra, actualiza y elimina cajas, cambia su estado, genera su QR y gestiona usuarios.
- **Voluntario:** consulta el inventario, busca y filtra cajas, y escanea códigos QR (solo lectura).

## Tecnologías

| Parte | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot, Spring Security (JWT), Spring Data MongoDB |
| Base de datos | MongoDB Atlas |
| Frontend | _Java Script , CSS , HTML_ |

## Estructura del repositorio

```
santandar/
├── backend/     API REST (Spring Boot)
├── frontend/    Aplicación web estática servida por HTTP
│   ├── index.html
│   ├── views/       Pantallas HTML y guía de organización
│   └── resources/
│       ├── css/
│       ├── js/
│       └── images/  Logo y recursos gráficos
└── docs/
    ├── diagramas/   Diagramas UML
    └── informes/    Requerimientos, EDT y gestión del proyecto
```

Las pantallas se cargan desde archivos de `frontend/views/`; consulta
`frontend/views/README.md` para agregar una y `frontend/resources/images/README.md`
para incorporar el logo.

## Puesta en marcha del backend

### 1. Requisitos

- JDK 21
- Una cuenta en MongoDB Atlas con un clúster, un usuario de base de datos y tu IP en *Network Access*

### 2. Variables de entorno

Define estas variables en tu computador o en la configuración de ejecución del IDE:

| Variable | Descripción |
|---|---|
| `MONGODB_URI` | Cadena de conexión de MongoDB Atlas (con el nombre de la base de datos `santandar`) |
| `JWT_SECRET` | Texto aleatorio de mínimo 32 caracteres (cuando se implemente la seguridad) |
| `SMTP_HOST` | Servidor SMTP que enviará los códigos de recuperación |
| `SMTP_PORT` | Puerto SMTP (por defecto `587`) |
| `SMTP_USERNAME` | Usuario SMTP |
| `SMTP_PASSWORD` | Contraseña o clave de aplicación SMTP |
| `SMTP_FROM` | Dirección remitente aceptada por el proveedor SMTP |

Las credenciales SMTP deben configurarse como variables de entorno y nunca
guardarse en el repositorio. La recuperación de contraseña envía un código
temporal de seis dígitos, válido durante 10 minutos.

En PowerShell, define las variables antes de iniciar el backend (usa una
contraseña de aplicación si tu proveedor la requiere):

```powershell
$env:SMTP_HOST = "smtp.example.com"
$env:SMTP_PORT = "587"
$env:SMTP_USERNAME = "cuenta-remitente@example.com"
$env:SMTP_PASSWORD = "tu-clave-smtp"
$env:SMTP_FROM = "cuenta-remitente@example.com"
.\mvnw.cmd spring-boot:run
```

Si no configuras SMTP, el backend puede iniciar, pero no podrá enviar códigos
de recuperación.


### 3. Ejecutar

```bash
cd backend
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Flujo de trabajo en equipo

- `main`: código estable, solo para entregas.
- `develop`: integración del trabajo del sprint.


Prefijos para los mensajes de commit:

| Prefijo | Uso |
|---|---|
| `feat:` | Funcionalidad nueva |
| `fix:` | Corrección de un error |
| `docs:` | Documentación |
| `chore:` | Configuración y tareas de mantenimiento |

## Equipo

- _William Fernando Basto Corzo - 2235607_
- _Javier Alberto Alean Gonzalez - 2235610_
- _David Alejandro Sandoval Rojas - 2235656_
