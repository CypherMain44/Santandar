package edu.uis.santandar.pruebas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** RF2 registrar | RF3 QR | RF4 inventario | RF5 buscar/filtrar | RF6 QR | RF7 actualizar | RF8 eliminar. */
class CajasTest extends BaseTest {

    private static String corto() {
        return UUID.randomUUID().toString().substring(0, 4);
    }

    @Test
    @DisplayName("SEL06 Registrar caja genera su QR")
    void SEL06_registrarCajaGeneraQr() {
        String contenido = "Agua-" + Datos.RUN_ID + "-" + corto();
        try {
            loginAdmin();
            crearCajaUi(contenido, "Zona-" + Datos.RUN_ID);
            assertFalse(driver.findElements(By.cssSelector("#qr-code canvas, #qr-code img")).isEmpty(),
                    "No se dibujó el código QR");
            assertTrue(texto("qr-details").contains(contenido));
            capturar("SEL06_qr_generado");
            cerrarQr();
            esperarFila("inventory-body", contenido);
        } finally {
            Datos.eliminarCajasPorPrefijo(contenido);
        }
    }

    @Test
    @DisplayName("SEL07 Caja con campos vacíos no se guarda")
    void SEL07_registrarCajaCamposVacios() {
        loginAdmin();
        irA("inventario");
        clicable(".inventory-summary .btn-outline").click();
        visible("#registrar-form");
        driver.findElement(By.cssSelector("#registrar-form .btn-pill")).click();
        assertTrue(estaVisible("#registrar-form"));
        assertFalse(estaVisible("#qr-dialog"));
    }

    @Test
    @DisplayName("SEL08 Inventario muestra total y cajas")
    void SEL08_inventarioMuestraCajas() {
        String[] caja = nuevaCajaApi();
        loginAdmin();
        irA("inventario");
        WebElement fila = esperarFila("inventory-body", caja[0]);
        assertTrue(fila.getText().contains(caja[1]));
        assertTrue(fila.getText().contains("No despachado"));
        assertTrue(Integer.parseInt(texto("inventory-total")) >= 1);
        capturar("SEL08_inventario");
    }

    @Test
    @DisplayName("SEL09 Búsqueda por texto")
    void SEL09_busquedaPorTexto() {
        String[] caja = nuevaCajaApi();
        loginAdmin();
        irA("inventario");
        esperarFila("inventory-body", caja[0]);
        WebElement buscar = porId("buscar");
        buscar.sendKeys(caja[0]);
        esperarQue(() -> filas("inventory-body").size() == 1);
        assertTrue(filas("inventory-body").get(0).getText().contains(caja[0]));
        buscar.clear();
        buscar.sendKeys("zzz-no-existe-zzz");
        esperarQue(() -> filas("inventory-body").isEmpty());
        assertTrue(texto("inventory-message").contains("No hay cajas que coincidan"));
        capturar("SEL09_busqueda");
    }

    @Test
    @DisplayName("SEL10 Filtro por destino")
    void SEL10_filtroPorDestino() {
        String[] caja = nuevaCajaApi();
        loginAdmin();
        irA("inventario");
        esperarFila("inventory-body", caja[0]);
        new Select(porId("filtro-destino")).selectByValue(caja[1]);
        esperarQue(() -> !filas("inventory-body").isEmpty());
        for (WebElement tr : filas("inventory-body")) {
            assertTrue(tr.getText().contains(caja[1]), "Fila fuera del filtro: " + tr.getText());
        }
    }

    @Test
    @DisplayName("SEL11 Editar caja")
    void SEL11_editarCaja() {
        String[] caja = nuevaCajaApi();
        String nuevo = "Editado-" + corto();
        loginAdmin();
        irA("inventario");
        esperarFila("inventory-body", caja[0]);
        clicEnFila("inventory-body", caja[0], "button[aria-label^=\"Editar\"]");
        visible("#edit-box-dialog");
        WebElement campo = porId("edit-box-content");
        campo.clear();
        campo.sendKeys(nuevo);
        capturar("SEL11_editar_caja");
        driver.findElement(By.cssSelector("#edit-box-form button[type=\"submit\"]")).click();
        esperarFila("inventory-body", nuevo);
        assertTrue(texto("inventory-message").contains("fue actualizada"));
    }

    @Test
    @DisplayName("SEL12 QR de una caja existente")
    void SEL12_qrDeCajaExistente() {
        String[] caja = nuevaCajaApi();
        loginAdmin();
        irA("inventario");
        esperarFila("inventory-body", caja[0]);
        clicEnFila("inventory-body", caja[0], "button[aria-label^=\"Generar QR\"]");
        visible("#qr-dialog");
        assertTrue(texto("qr-details").contains(caja[0]));
        assertFalse(driver.findElements(By.cssSelector("#qr-code canvas, #qr-code img")).isEmpty());
    }

    @Test
    @DisplayName("SEL13 Eliminar caja con confirmación")
    void SEL13_eliminarCaja() {
        String contenido = "Borrar-" + Datos.RUN_ID + "-" + corto();
        Datos.crearCaja(contenido, "Zona-borrar");
        try {
            loginAdmin();
            irA("inventario");
            esperarFila("inventory-body", contenido);
            clicEnFila("inventory-body", contenido, "button[aria-label^=\"Eliminar\"]");
            visible("#delete-confirm-dialog");
            driver.findElement(By.cssSelector("#delete-confirm-dialog [value=\"confirm\"]")).click();
            esperarQue(() -> filaConTexto("inventory-body", contenido) == null);
            assertTrue(texto("inventory-message").contains("fue eliminada"));
        } finally {
            Datos.eliminarCajasPorPrefijo(contenido);
        }
    }

    @Test
    @DisplayName("SEL14 Cancelar eliminación conserva la caja")
    void SEL14_cancelarEliminacion() {
        String[] caja = nuevaCajaApi();
        loginAdmin();
        irA("inventario");
        esperarFila("inventory-body", caja[0]);
        clicEnFila("inventory-body", caja[0], "button[aria-label^=\"Eliminar\"]");
        visible("#delete-confirm-dialog");
        driver.findElement(By.cssSelector("#delete-confirm-dialog [value=\"cancel\"]")).click();
        esperarQue(() -> !porId("delete-confirm-dialog").isDisplayed());
        assertNotNull(filaConTexto("inventory-body", caja[0]));
    }

    @Test
    @DisplayName("SEL15 Voluntario con permisos de solo lectura")
    void SEL15_voluntarioSoloLectura() {
        String[] caja = nuevaCajaApi();
        Datos.Voluntario v = Datos.voluntario();
        loginOk(v.correo(), v.clave());
        irA("inventario");
        WebElement fila = esperarFila("inventory-body", caja[0]);
        assertTrue(fila.findElements(By.cssSelector("button")).isEmpty(), "El voluntario no debe ver botones de acción");
        assertFalse(estaVisible("#inventory-actions-heading"));
    }
}
