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

Las pantallas se cargan desde archivos de `frontend/views/`; 

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
| `BREVO_API_KEY` | Clave de API de Brevo para enviar correos transaccionales |
| `BREVO_SENDER_EMAIL` | Dirección remitente verificada en Brevo |
| `BREVO_SENDER_NAME` | Nombre visible del remitente (por defecto `SantanDar`) |

La clave de API y la dirección remitente deben configurarse como variables de
entorno y nunca guardarse en el repositorio. Brevo envía los códigos temporales
de seis dígitos, válidos durante 10 minutos. La dirección remitente debe estar
verificada en Brevo.

Si configuras opciones locales de Spring, guárdalas en
`backend/src/main/resources/application.properties`; ese archivo está excluido
de Git para no publicar credenciales.

En PowerShell, define las variables antes de iniciar el backend:

```powershell
$env:BREVO_API_KEY = "tu-clave-de-api-de-brevo"
$env:BREVO_SENDER_EMAIL = "cuenta-remitente@example.com"
$env:BREVO_SENDER_NAME = "SantanDar"
.\backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

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
