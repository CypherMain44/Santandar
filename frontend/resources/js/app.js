const loginView = document.getElementById("view-login");
const appShell = document.getElementById("app-shell");
const loginForm = document.getElementById("login-form");
const loginMessage = document.getElementById("login-message");
const navToggle = document.getElementById("nav-toggle");
const navBackdrop = document.getElementById("nav-backdrop");
const apiBaseUrl = document.querySelector('meta[name="api-base-url"]').content.replace(/\/$/, "");

function closeDrawer() {
  appShell.classList.remove("is-nav-open");
  navToggle.setAttribute("aria-expanded", "false");
  navBackdrop.hidden = true;
}

function showView(name) {
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

navToggle.addEventListener("click", () => {
  const open = !appShell.classList.contains("is-nav-open");
  appShell.classList.toggle("is-nav-open", open);
  navToggle.setAttribute("aria-expanded", String(open));
  navBackdrop.hidden = !open;
});

navBackdrop.addEventListener("click", closeDrawer);

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  loginMessage.hidden = true;

  const submitButton = loginForm.querySelector('button[type="submit"]');
  submitButton.disabled = true;

  try {
    const response = await fetch(`${apiBaseUrl}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        correo: loginForm.elements.correo.value.trim(),
        clave: loginForm.elements.clave.value
      })
    });
    const result = await response.json();

    if (!response.ok) {
      loginMessage.textContent = response.status === 401
        ? "Correo o clave incorrectos."
        : "No fue posible iniciar sesión. Inténtalo de nuevo.";
      loginMessage.hidden = false;
      return;
    }

    if (typeof result.token !== "string" || !result.token) {
      throw new Error("Respuesta de autenticación inválida");
    }

    sessionStorage.setItem("authToken", result.token);
    loginForm.reset();
    showView("inicio");
  } catch (error) {
    loginMessage.textContent = error instanceof TypeError
      ? "No se pudo conectar con el servidor."
      : "El servidor devolvió una respuesta inválida.";
    loginMessage.hidden = false;
  } finally {
    submitButton.disabled = false;
  }
});

document.querySelectorAll(".nav-item").forEach((item) => {
  item.addEventListener("click", () => showView(item.dataset.view));
});

document.querySelectorAll("form:not(#login-form)").forEach((form) => {
  form.addEventListener("submit", (event) => event.preventDefault());
});

document.querySelectorAll("[data-visual-only]").forEach((link) => {
  link.addEventListener("click", (event) => event.preventDefault());
});

showView("login");
