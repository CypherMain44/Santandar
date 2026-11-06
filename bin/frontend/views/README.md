# Vistas

La aplicación actual es una página estática de una sola entrada:
[`../index.html`](../index.html). Cada pantalla es una sección identificada
con `data-app-view` (`inicio`, `inventario`, `registrar`, `despacho` o
`usuarios`); la pantalla de inicio de sesión usa `id="view-login"`.

Para agregar una pantalla, crea una sección `app-view` en `index.html`, asigna
un valor único a `data-app-view` y registra ese valor en `ORDER` dentro de
[`../resources/js/app.js`](../resources/js/app.js). Mantener las secciones en
la página de entrada permite abrirla como archivo estático sin requerir un
servidor para cargar fragmentos HTML.
