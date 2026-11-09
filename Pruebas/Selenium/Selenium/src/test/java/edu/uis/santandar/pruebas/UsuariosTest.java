package edu.uis.santandar.pruebas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** RF9 - Registro de nuevos usuarios (voluntarios). */
class UsuariosTest extends BaseTest {

    private static String correoUnico(String prefijo) {
        return prefijo + "_" + UUID.randomUUID().toString().substring(0, 8) + "@santandar.test";
    }

    @Test
    @DisplayName("SEL18 Admin registra un voluntario y este puede ingresar")
    void SEL18_registrarVoluntario() {
        String correo = correoUnico("ui");
        try {
            loginAdmin();
            irA("usuarios");
            porId("user-email").sendKeys(correo);
            porId("user-password").sendKeys("Voluntario123");
            driver.findElement(By.cssSelector("#user-form .btn-pill")).click();
            WebElement fila = esperarFila("users-body", correo);
            assertTrue(fila.getText().contains("Voluntario"));
            capturar("SEL18_usuario_registrado");

            js("sessionStorage.clear();");
            loginOk(correo, "Voluntario123");
        } finally {
            Datos.eliminarUsuarioPorCorreo(correo);
        }
    }

    @Test
    @DisplayName("SEL19 Correo duplicado muestra error")
    void SEL19_correoDuplicado() {
        Datos.Voluntario v = Datos.voluntario();
        loginAdmin();
        irA("usuarios");
        porId("user-email").sendKeys(v.correo());
        porId("user-password").sendKeys("Voluntario123");
        driver.findElement(By.cssSelector("#user-form .btn-pill")).click();
        WebElement msg = visible("#users-form-message");
        esperarQue(() -> texto("users-form-message").contains("ya está registrado"));
        assertTrue(msg.getAttribute("class").contains("is-error"));
    }

    @Test
    @DisplayName("SEL20 Eliminar voluntario")
    void SEL20_eliminarVoluntario() {
        String correo = correoUnico("del");
        Datos.crearVoluntario(correo, "Voluntario123");
        try {
            loginAdmin();
            irA("usuarios");
            WebElement fila = esperarFila("users-body", correo);
            fila.findElement(By.cssSelector(".user-delete-button")).click();
            visible("#user-delete-confirm-dialog");
            driver.findElement(By.cssSelector("#user-delete-confirm-dialog [value=\"confirm\"]")).click();
            esperarQue(() -> filaConTexto("users-body", correo) == null);
        } finally {
            Datos.eliminarUsuarioPorCorreo(correo);
        }
    }
}
