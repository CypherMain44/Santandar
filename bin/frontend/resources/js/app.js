// TEMPORAL: eliminar cuando se conecten las rutas reales.
// Este archivo solo cambia la vista visible. No envía datos ni llama a la API.

const ORDER = ["login", "inicio", "inventario", "registrar", "despacho", "usuarios"];

const loginView = document.getElementById("view-login");
const appShell = document.getElementById("app-shell");
const siguiente = document.getElementById("btn-siguiente");
const navToggle = document.getElementById("nav-toggle");
const navBackdrop = document.getElementById("nav-backdrop");

let index = 0;

function closeDrawer() {
  appShell.classList.remove("is-nav-open");
  navToggle.setAttribute("aria-expanded", "false");
  navBackdrop.hidden = true;
}

function showView(name) {
  const position = ORDER.indexOf(name);
  if (position >= 0) index = position;

  const isLogin = name === "login";
  loginView.hidden = !isLogin;
  appShell.hidden = isLogin;
  document.body.classList.toggle("is-login", isLogin);

  document.querySelectorAll("[data-app-view]").forEach((section) => {
    section.hidden = section.dataset.appView !== name;
  });

  document.querySelectorAll(".nav-item").forEach((item) => {
    const active = item.dataset.view === name || (name === "registrar" && item.dataset.view === "inventario");
    item.classList.toggle("is-active", active);
    if (active) item.setAttribute("aria-current", "page");
    else item.removeAttribute("aria-current");
  });

  closeDrawer();
}

siguiente.addEventListener("click", () => {
  index = (index + 1) % ORDER.length;
  showView(ORDER[index]);
});

navToggle.addEventListener("click", () => {
  const open = !appShell.classList.contains("is-nav-open");
  appShell.classList.toggle("is-nav-open", open);
  navToggle.setAttribute("aria-expanded", String(open));
  navBackdrop.hidden = !open;
});

navBackdrop.addEventListener("click", closeDrawer);

document.querySelectorAll("form").forEach((form) => {
  form.addEventListener("submit", (event) => event.preventDefault());
});

document.querySelectorAll("[data-visual-only]").forEach((link) => {
  link.addEventListener("click", (event) => event.preventDefault());
});

showView("login");
