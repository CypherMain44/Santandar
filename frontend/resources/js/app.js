const loginView = document.getElementById("view-login");
const appShell = document.getElementById("app-shell");
const loginForm = document.getElementById("login-form");
const loginMessage = document.getElementById("login-message");
const navToggle = document.getElementById("nav-toggle");
const navBackdrop = document.getElementById("nav-backdrop");
const apiBaseUrl = document.querySelector('meta[name="api-base-url"]').content.replace(/\/$/, "");
const inventoryBody = document.getElementById("inventory-body");
const inventoryMessage = document.getElementById("inventory-message");
const inventoryTotal = document.getElementById("inventory-total");
const searchInput = document.getElementById("buscar");
const destinationFilter = document.getElementById("filtro-destino");
const qrDialog = document.getElementById("qr-dialog");
const qrCodeElement = document.getElementById("qr-code");
const qrError = document.getElementById("qr-error");
const qrDetails = document.getElementById("qr-details");
let currentUserType = null;
let inventory = [];

inventoryBody.replaceChildren();

function updateAdminControls() {
  const isAdministrator = currentUserType === "ADMINISTRADOR";
  document.querySelectorAll("[data-admin-only]").forEach((element) => {
    element.hidden = !isAdministrator;
  });
}

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

  updateAdminControls();
  if (name === "inventario") loadInventory();
  closeDrawer();
}

function addCell(row, value) {
  const cell = document.createElement("td");
  cell.textContent = value || "—";
  row.appendChild(cell);
}

function showBoxQr(box) {
  const statusLabels = {
    DESPACHADO: "Despachado",
    NO_DESPACHADO: "No despachado"
  };
  const boxDetails = [
    `ID de caja: ${box.idCaja || "—"}`,
    `Destino: ${box.zonaDestino || "—"}`,
    `Contenido: ${box.contenido || "—"}`,
    `Estado: ${statusLabels[box.estadoCaja] || box.estadoCaja || "Sin estado"}`,
    `Código QR: ${box.identificadorQR || "—"}`
  ];

  qrCodeElement.replaceChildren();
  qrError.hidden = true;
  qrDetails.textContent = boxDetails.join("\n");
  qrDialog.showModal();

  if (typeof QRCode === "undefined") {
    qrError.textContent = "No se pudo cargar la librería para generar el código QR.";
    qrError.hidden = false;
    return;
  }

  try {
    new QRCode(qrCodeElement, {
      text: `SantanDar\n${boxDetails.join("\n")}`,
      width: 256,
      height: 256,
      correctLevel: QRCode.CorrectLevel.L
    });
  } catch {
    qrError.textContent = "No fue posible generar el código QR de esta caja.";
    qrError.hidden = false;
  }
}

function addActionsCell(row, box) {
  const cell = document.createElement("td");
  const actions = document.createElement("div");
  actions.className = "row-actions";

  ["Editar", "Eliminar"].forEach((label) => {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = label;
    button.setAttribute("aria-label", `${label} ${box.idCaja}`);
    actions.appendChild(button);
  });

  const qrButton = document.createElement("button");
  qrButton.type = "button";
  qrButton.textContent = "QR";
  qrButton.setAttribute("aria-label", `Generar QR para ${box.idCaja}`);
  qrButton.addEventListener("click", () => showBoxQr(box));
  actions.appendChild(qrButton);

  cell.appendChild(actions);
  row.appendChild(cell);
}

function renderInventory() {
  const search = searchInput.value.trim().toLocaleLowerCase();
  const destination = destinationFilter.value;
  const filteredBoxes = inventory.filter((box) => {
    const matchesSearch = [box.idCaja, box.zonaDestino, box.contenido, box.estadoCaja]
      .some((value) => String(value || "").toLocaleLowerCase().includes(search));
    return matchesSearch && (!destination || box.zonaDestino === destination);
  });

  inventoryBody.replaceChildren();
  filteredBoxes.forEach((box) => {
    const row = document.createElement("tr");
    addCell(row, box.idCaja);
    addCell(row, box.zonaDestino);
    addCell(row, box.contenido);

    const statusCell = document.createElement("td");
    const status = document.createElement("span");
    const dot = document.createElement("i");
    const statusLabels = {
      DESPACHADO: { label: "Despachado", dot: "dot--green" },
      NO_DESPACHADO: { label: "No despachado", dot: "dot--yellow" }
    };
    const statusInfo = statusLabels[box.estadoCaja] || {
      label: box.estadoCaja || "Sin estado",
      dot: "dot--gray"
    };
    status.className = "status";
    dot.className = `dot ${statusInfo.dot}`;
    dot.setAttribute("aria-hidden", "true");
    status.append(dot, document.createTextNode(statusInfo.label));
    statusCell.appendChild(status);
    row.appendChild(statusCell);

    if (currentUserType === "ADMINISTRADOR") addActionsCell(row, box);
    inventoryBody.appendChild(row);
  });

  inventoryMessage.textContent = inventory.length === 0
    ? "No hay cajas registradas."
    : filteredBoxes.length === 0
      ? "No hay cajas que coincidan con la búsqueda."
      : "";
  inventoryMessage.hidden = filteredBoxes.length > 0;
}

function populateDestinationFilter() {
  const destinations = [...new Set(inventory.map((box) => box.zonaDestino).filter(Boolean))].sort();
  destinationFilter.replaceChildren(new Option("Todos los destinos", ""));
  destinations.forEach((destination) => {
    destinationFilter.add(new Option(destination, destination));
  });
}

async function loadInventory() {
  inventoryMessage.textContent = "Cargando cajas...";
  inventoryMessage.hidden = false;
  inventoryBody.replaceChildren();

  try {
    const response = await fetch(`${apiBaseUrl}/api/cajas`, {
      headers: { Authorization: `Bearer ${sessionStorage.getItem("authToken")}` }
    });
    if (response.status === 401) throw new Error("La sesión expiró. Inicia sesión nuevamente.");
    if (!response.ok) throw new Error(`No fue posible consultar las cajas (HTTP ${response.status}).`);

    const result = await response.json();
    if (!Array.isArray(result)) throw new Error("El servidor devolvió una lista de cajas inválida.");

    inventory = result;
    inventoryTotal.textContent = String(inventory.length);
    populateDestinationFilter();
    renderInventory();
  } catch (error) {
    inventory = [];
    inventoryTotal.textContent = "0";
    populateDestinationFilter();
    inventoryMessage.textContent = error instanceof TypeError
      ? "No se pudo conectar con el servidor."
      : error instanceof Error
        ? error.message
        : "No fue posible cargar las cajas.";
    inventoryMessage.hidden = false;
  }
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

    if (typeof result.token !== "string" || !result.token
      || !["ADMINISTRADOR", "VOLUNTARIO"].includes(result.tipo)) {
      throw new Error("Respuesta de autenticación inválida");
    }

    sessionStorage.setItem("authToken", result.token);
    currentUserType = result.tipo;
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

searchInput.addEventListener("input", renderInventory);
destinationFilter.addEventListener("change", renderInventory);
document.querySelector(".inventory-summary .btn-outline").addEventListener("click", () => showView("registrar"));

document.querySelectorAll('form:not(#login-form):not([method="dialog"])').forEach((form) => {
  form.addEventListener("submit", (event) => event.preventDefault());
});

document.querySelectorAll("[data-visual-only]").forEach((link) => {
  link.addEventListener("click", (event) => event.preventDefault());
});

showView("login");
