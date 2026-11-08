const VIEW_FRAGMENTS = [
  { path: "views/login.html", target: "#view-login-host", selector: "#view-login" },
  { path: "views/inicio.html", target: '[data-view-host="inicio"]', selector: '[data-app-view="inicio"]' },
  { path: "views/inventario.html", target: '[data-view-host="inventario"]', selector: '[data-app-view="inventario"]' },
  { path: "views/registrar.html", target: '[data-view-host="registrar"]', selector: '[data-app-view="registrar"]' },
  { path: "views/despacho.html", target: '[data-view-host="despacho"]', selector: '[data-app-view="despacho"]' },
  { path: "views/usuarios.html", target: '[data-view-host="usuarios"]', selector: '[data-app-view="usuarios"]' },
  { path: "views/dialogs.html", target: "#dialog-host", selector: "dialog" }
];

async function loadViewFragments() {
  const fragments = await Promise.all(VIEW_FRAGMENTS.map(async ({ path, target, selector }) => {
    const response = await fetch(path);
    if (!response.ok) {
      throw new Error(`No fue posible cargar ${path} (HTTP ${response.status}).`);
    }

    const documentFragment = new DOMParser().parseFromString(await response.text(), "text/html");
    const host = document.querySelector(target);
    const elements = [...documentFragment.body.querySelectorAll(selector)];
    if (!host || elements.length === 0) {
      throw new Error(`El fragmento ${path} no tiene el contenido esperado.`);
    }
    return { host, elements: elements.map((element) => element.cloneNode(true)) };
  }));

  fragments.forEach(({ host, elements }) => {
    host.replaceChildren(...elements);
  });

  const appScript = document.createElement("script");
  appScript.src = "resources/js/app.js";
  appScript.addEventListener("error", () => {
    showViewLoadError("No fue posible iniciar la aplicación.");
  });
  document.body.appendChild(appScript);
}

function showViewLoadError(message) {
  const errorMessage = document.getElementById("view-load-error");
  errorMessage.textContent = `${message} Inicia la aplicación desde un servidor local; no funciona abriendo index.html directamente.`;
  errorMessage.hidden = false;
}

loadViewFragments().catch((error) => {
  showViewLoadError(error instanceof Error ? error.message : "No fue posible cargar las vistas.");
});
