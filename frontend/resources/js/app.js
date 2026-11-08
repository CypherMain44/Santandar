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
const deleteConfirmDialog = document.getElementById("delete-confirm-dialog");
const deleteConfirmMessage = document.getElementById("delete-confirm-message");
const editBoxDialog = document.getElementById("edit-box-dialog");
const editBoxForm = document.getElementById("edit-box-form");
const editBoxMessage = document.getElementById("edit-box-message");
const registerForm = document.getElementById("registrar-form");
const registerMessage = document.getElementById("registrar-message");
let currentUserType = null;
let inventory = [];
let editingBoxId = null;

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
  if (name === "registrar" && currentUserType !== "ADMINISTRADOR") {
    name = "inventario";
  }

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
      text: JSON.stringify({
        idCaja: box.idCaja,
        identificadorQR: box.identificadorQR,
        contenido: box.contenido,
        zonaDestino: box.zonaDestino,
        estadoCaja: box.estadoCaja
      }),
      width: 256,
      height: 256,
      correctLevel: QRCode.CorrectLevel.L
    });
  } catch {
    qrError.textContent = "No fue posible generar el código QR de esta caja.";
    qrError.hidden = false;
  }
}

async function deleteBox(box, button) {
  if (currentUserType !== "ADMINISTRADOR") return;
  deleteConfirmMessage.textContent = `¿Seguro que deseas eliminar la caja ${box.idCaja}? Esta acción no se puede deshacer.`;
  const confirmation = new Promise((resolve) => {
    deleteConfirmDialog.addEventListener("close", () => {
      resolve(deleteConfirmDialog.returnValue === "confirm");
    }, { once: true });
  });
  deleteConfirmDialog.showModal();
  const confirmed = await confirmation;
  if (!confirmed) return;

  button.disabled = true;
  inventoryMessage.hidden = true;

  try {
    const response = await fetch(`${apiBaseUrl}/api/cajas/${encodeURIComponent(box.idCaja)}`, {
      method: "DELETE",
      headers: { Authorization: `Bearer ${sessionStorage.getItem("authToken")}` }
    });

    if (!response.ok) {
      let message = `No fue posible eliminar la caja (HTTP ${response.status}).`;
      try {
        const result = await response.json();
        message = result.mensaje || message;
      } catch {
        // Keep the HTTP status message when the server does not return JSON.
      }

      if (response.status === 401) message = "La sesión expiró. Inicia sesión nuevamente.";
      if (response.status === 403) message = "Solo un administrador puede eliminar cajas.";
      throw new Error(message);
    }

    inventory = inventory.filter((item) => item.idCaja !== box.idCaja);
    inventoryTotal.textContent = String(inventory.length);
    populateDestinationFilter();
    renderInventory();
    inventoryMessage.textContent = `La caja ${box.idCaja} fue eliminada.`;
    inventoryMessage.hidden = false;
  } catch (error) {
    inventoryMessage.textContent = error instanceof TypeError
      ? "No se pudo conectar con el servidor."
      : error instanceof Error
        ? error.message
        : "No fue posible eliminar la caja.";
    inventoryMessage.hidden = false;
  } finally {
    button.disabled = false;
  }
}

function addActionsCell(row, box) {
  const cell = document.createElement("td");
  const actions = document.createElement("div");
  actions.className = "row-actions";

  const editButton = document.createElement("button");
  editButton.type = "button";
  editButton.textContent = "Editar";
  editButton.setAttribute("aria-label", `Editar ${box.idCaja}`);
  editButton.addEventListener("click", () => openEditBox(box));
  actions.appendChild(editButton);

  const deleteButton = document.createElement("button");
  deleteButton.type = "button";
  deleteButton.textContent = "Eliminar";
  deleteButton.setAttribute("aria-label", `Eliminar ${box.idCaja}`);
  deleteButton.addEventListener("click", () => deleteBox(box, deleteButton));
  actions.appendChild(deleteButton);

  const qrButton = document.createElement("button");
  qrButton.type = "button";
  qrButton.textContent = "QR";
  qrButton.setAttribute("aria-label", `Generar QR para ${box.idCaja}`);
  qrButton.addEventListener("click", () => showBoxQr(box));
  actions.appendChild(qrButton);

  cell.appendChild(actions);
  row.appendChild(cell);
}

function openEditBox(box) {
  if (currentUserType !== "ADMINISTRADOR") return;

  editingBoxId = box.idCaja;
  editBoxForm.elements.contenido.value = box.contenido || "";
  editBoxForm.elements.zonaDestino.value = box.zonaDestino || "";
  editBoxMessage.hidden = true;
  editBoxDialog.showModal();
  editBoxForm.elements.contenido.focus();
}

editBoxForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  editBoxMessage.hidden = true;

  if (currentUserType !== "ADMINISTRADOR" || !editingBoxId) {
    editBoxDialog.close();
    return;
  }

  const submitButton = editBoxForm.querySelector('button[type="submit"]');
  submitButton.disabled = true;

  try {
    const response = await fetch(`${apiBaseUrl}/api/cajas/${encodeURIComponent(editingBoxId)}`, {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${sessionStorage.getItem("authToken")}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        contenido: editBoxForm.elements.contenido.value.trim(),
        zonaDestino: editBoxForm.elements.zonaDestino.value.trim()
      })
    });

    const result = await response.json();
    if (!response.ok) {
      const validationMessage = result.errores && Object.values(result.errores)[0];
      editBoxMessage.textContent = response.status === 401
        ? "La sesión expiró. Inicia sesión nuevamente."
        : response.status === 403
          ? "Solo un administrador puede editar cajas."
          : validationMessage || result.mensaje || "No fue posible actualizar la caja.";
      editBoxMessage.hidden = false;
      return;
    }

    if (result.idCaja !== editingBoxId) {
      throw new Error("El servidor devolvió una caja distinta a la editada.");
    }

    inventory = inventory.map((box) => box.idCaja === editingBoxId ? result : box);
    const updatedBoxId = editingBoxId;
    editBoxDialog.close();
    populateDestinationFilter();
    renderInventory();
    inventoryMessage.textContent = `La caja ${updatedBoxId} fue actualizada.`;
    inventoryMessage.hidden = false;
  } catch (error) {
    editBoxMessage.textContent = error instanceof TypeError
      ? "No se pudo conectar con el servidor."
      : error instanceof Error
        ? error.message
        : "No fue posible actualizar la caja.";
    editBoxMessage.hidden = false;
  } finally {
    submitButton.disabled = false;
  }
});

editBoxDialog.querySelector('[value="cancel"]').addEventListener("click", () => editBoxDialog.close());
editBoxDialog.addEventListener("close", () => {
  editingBoxId = null;
  editBoxForm.reset();
});

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

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  registerMessage.hidden = true;

  if (currentUserType !== "ADMINISTRADOR") {
    showView("inventario");
    return;
  }

  const submitButton = registerForm.querySelector('button[type="submit"]');
  submitButton.disabled = true;

  try {
    const response = await fetch(`${apiBaseUrl}/api/cajas`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${sessionStorage.getItem("authToken")}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        contenido: registerForm.elements.contenido.value.trim(),
        zonaDestino: registerForm.elements.zonaDestino.value.trim()
      })
    });
    const result = await response.json();

    if (!response.ok) {
      const validationMessage = result.errores && Object.values(result.errores)[0];
      registerMessage.textContent = response.status === 401
        ? "La sesión expiró. Inicia sesión nuevamente."
        : response.status === 403
          ? "Solo un administrador puede registrar cajas."
          : validationMessage || result.mensaje || "No fue posible registrar la caja.";
      registerMessage.hidden = false;
      return;
    }

    if (!result.idCaja || !result.identificadorQR) {
      throw new Error("El servidor devolvió una caja sin identificador o código QR.");
    }

    registerForm.reset();
    showView("inventario");
    showBoxQr(result);
  } catch (error) {
    registerMessage.textContent = error instanceof TypeError
      ? "No se pudo conectar con el servidor."
      : error instanceof Error
        ? error.message
        : "No fue posible registrar la caja.";
    registerMessage.hidden = false;
  } finally {
    submitButton.disabled = false;
  }
});

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
deleteConfirmDialog.querySelector('[value="cancel"]').addEventListener("click", () => deleteConfirmDialog.close("cancel"));
deleteConfirmDialog.querySelector('[value="confirm"]').addEventListener("click", () => deleteConfirmDialog.close("confirm"));

document.querySelectorAll('form:not(#login-form):not(#registrar-form):not(#edit-box-form):not([method="dialog"])').forEach((form) => {
  form.addEventListener("submit", (event) => event.preventDefault());
});

document.querySelectorAll("[data-visual-only]").forEach((link) => {
  link.addEventListener("click", (event) => event.preventDefault());
});

showView("login");
