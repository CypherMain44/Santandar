package edu.uis.santandar.pruebas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

/** Despacho de cajas (cambio de estado) y panel de inicio (RF4). */
class DespachoDashboardTest extends BaseTest {

    @Test
    @DisplayName("SEL16 Despachar una caja")
    void SEL16_despacharCaja() {
        String[] caja = nuevaCajaApi();
        loginAdmin();
        irA("despacho");
        WebElement fila = esperarFila("dispatch-body", caja[0]);
        fila.findElement(By.cssSelector("button[aria-label^=\"Despachar caja\"]")).click();
        esperarQue(() -> filaConTexto("dispatch-body", caja[0]) == null);
        assertTrue(texto("dispatch-message").contains("fue despachada"));
        irA("inventario");
        String textoFila = esperarFila("inventory-body", caja[0]).getText();
        assertTrue(textoFila.contains("Despachado") && !textoFila.contains("No despachado"),
                "La caja debería figurar como Despachado: " + textoFila);
        capturar("SEL16_despacho");
    }

    @Test
    @DisplayName("SEL17 Contadores del dashboard coinciden")
    void SEL17_dashboardContadores() {
        nuevaCajaApi();
        loginAdmin();
        irA("inicio");
        esperarQue(() -> Integer.parseInt(texto("dashboard-pie-total")) >= 1);
        int total = Integer.parseInt(texto("dashboard-pie-total"));
        int hechas = Integer.parseInt(texto("dashboard-done-count"));
        int pendientes = Integer.parseInt(texto("dashboard-pending-count"));
        assertEquals(total, hechas + pendientes);
        capturar("SEL17_dashboard");
    }
}
