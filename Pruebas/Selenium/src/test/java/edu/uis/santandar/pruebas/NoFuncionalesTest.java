package edu.uis.santandar.pruebas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Requerimientos no funcionales medibles desde el navegador: RNF1 (eficiencia) y RNF3 (responsive). */
class NoFuncionalesTest extends BaseTest {

    @Test
    @DisplayName("SEL21 RNF1 El dashboard carga en menos de 3 s")
    void SEL21_dashboardCargaEnMenosDe3s() {
        nuevaCajaApi();
        loginAdmin();
        irA("inventario");
        long t0 = System.nanoTime();
        irA("inicio");
        esperarQue(3, () -> Integer.parseInt(texto("dashboard-pie-total")) >= 1);
        double seg = (System.nanoTime() - t0) / 1e9;
        System.out.printf("[RNF1] Dashboard cargado en %.2f s%n", seg);
        assertTrue(seg < 3.0, "El dashboard tardó " + seg + " s (límite 3 s)");
    }

    @Test
    @DisplayName("SEL22 RNF1 La búsqueda responde en menos de 3 s")
    void SEL22_busquedaEnMenosDe3s() {
        String[] caja = nuevaCajaApi();
        loginAdmin();
        irA("inventario");
        esperarQue(() -> !filas("inventory-body").isEmpty());
        long t0 = System.nanoTime();
        porId("buscar").sendKeys(caja[0]);
        esperarQue(3, () -> filas("inventory-body").size() == 1);
        double seg = (System.nanoTime() - t0) / 1e9;
        System.out.printf("[RNF1] Búsqueda resuelta en %.2f s%n", seg);
        assertTrue(seg < 3.0);
    }

    @Test
    @DisplayName("SEL23 RNF3 Responsive en móvil (375x812)")
    void SEL23_responsiveMovil() {
        emularPantalla(375, 812);
        loginAdmin();
        assertTrue(estaVisible("#nav-toggle"), "En móvil debe verse el botón de menú (hamburguesa)");
        long ancho = (Long) js("return document.documentElement.scrollWidth;");
        assertTrue(ancho <= 377, "Desbordamiento horizontal: scrollWidth=" + ancho);
        porId("nav-toggle").click();
        visible("#sidebar");
        capturar("SEL23_responsive_movil");
    }

    @Test
    @DisplayName("SEL24 RNF3 Responsive en tablet (768x1024)")
    void SEL24_responsiveTablet() {
        emularPantalla(768, 1024);
        loginAdmin();
        long ancho = (Long) js("return document.documentElement.scrollWidth;");
        assertTrue(ancho <= 770, "Desbordamiento horizontal: scrollWidth=" + ancho);
        capturar("SEL24_responsive_tablet");
    }
}
