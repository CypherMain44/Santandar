package edu.uis.santandar.pruebas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

/** RF1 - Control de acceso por roles (login) | RNF4 - Seguridad. */
class AutenticacionTest extends BaseTest {

    @Test
    @DisplayName("SEL01 Login de administrador exitoso")
    void SEL01_loginAdministradorExitoso() {
        loginAdmin();
        assertTrue(texto("dashboard-user-email").contains(Config.ADMIN_EMAIL));
        assertTrue(estaVisible(".nav-item[data-view=\"usuarios\"]"), "El admin debe ver el menú Usuarios");
        capturar("SEL01_login_admin");
    }

    @Test
    @DisplayName("SEL02 Voluntario sin opciones de administrador")
    void SEL02_voluntarioSinOpcionesDeAdmin() {
        Datos.Voluntario v = Datos.voluntario();
        loginOk(v.correo(), v.clave());
        assertFalse(estaVisible(".nav-item[data-view=\"usuarios\"]"));
        irA("inventario");
        assertFalse(estaVisible(".inventory-summary .btn-outline"));
        capturar("SEL02_login_voluntario");
    }

    @Test
    @DisplayName("SEL03 Login con clave incorrecta")
    void SEL03_loginClaveIncorrecta() {
        login(Config.ADMIN_EMAIL, "ClaveIncorrecta999");
        String msg = visible("#login-message").getText();
        assertTrue(msg.toLowerCase().contains("incorrect"), "Mensaje inesperado: " + msg);
        assertTrue(estaVisible("#login-form"));
        assertFalse(estaVisible("#app-shell"));
        capturar("SEL03_login_invalido");
    }

    @Test
    @DisplayName("SEL04 Login con campos vacíos no se envía")
    void SEL04_loginCamposVacios() {
        driver.get(Config.FRONT_URL + "/index.html");
        visible("#login-form");
        driver.findElement(By.cssSelector(".btn-login")).click();
        assertTrue(estaVisible("#login-form"));
        assertFalse(estaVisible("#app-shell"));
    }

    @Test
    @DisplayName("SEL05 Token JWT guardado en la sesión")
    void SEL05_tokenGuardadoEnSesion() {
        loginAdmin();
        String token = (String) js("return sessionStorage.getItem('authToken');");
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "Se esperaba un JWT (3 partes separadas por punto)");
    }
}
