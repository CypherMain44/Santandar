# Vistas

Cada pantalla vive en su propio archivo HTML y se carga desde
[`../index.html`](../index.html) mediante
[`../resources/js/view-loader.js`](../resources/js/view-loader.js):

- `login.html`: inicio de sesión.
- `inicio.html`: dashboard.
- `inventario.html`: inventario de cajas.
- `registrar.html`: formulario para registrar cajas.
- `despacho.html`: despacho de cajas.
- `usuarios.html`: administración de voluntarios.
- `dialogs.html`: diálogos compartidos.

Para agregar una pantalla:

1. Crea `nombre.html` como documento HTML completo con `<body>` y su cierre.
   Dentro incluye una sección `app-view` con un valor único para
   `data-app-view`. El documento completo permite que Live Server inserte su
   script de recarga sin interrumpir el contenido de la vista.
2. Agrega el fragmento a `VIEW_FRAGMENTS` en
   [`../resources/js/view-loader.js`](../resources/js/view-loader.js), junto
   con su contenedor `data-view-host` en `index.html`.
3. Agrega el botón de navegación con el mismo valor en `data-view` y conecta
   su comportamiento en [`../resources/js/app.js`](../resources/js/app.js).

La aplicación requiere servirse por HTTP para cargar los fragmentos. Desde
esta carpeta, ejecuta `python -m http.server 8000` y abre
`http://localhost:8000`. No se puede abrir directamente con `file://`.
