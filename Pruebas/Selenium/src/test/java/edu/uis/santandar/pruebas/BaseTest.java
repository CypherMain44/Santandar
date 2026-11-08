package edu.uis.santandar.pruebas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/** Base de todas las pruebas de interfaz: abre Chrome, ofrece utilidades y captura pantalla si algo falla. */
public abstract class BaseTest {

    protected static final Path REPORTES = Path.of("target", "reportes");
    protected WebDriver driver;
    /** Ids de cajas creadas por la prueba para eliminarlas al terminar. */
    protected final List<String> cajasCreadas = new ArrayList<>();

    @RegisterExtension
    final AfterTestExecutionCallback capturaSiFalla = ctx -> {
        if (ctx.getExecutionException().isPresent() && driver != null) {
            capturar("FALLO_" + ctx.getRequiredTestMethod().getName());
        }
    };

    @BeforeEach
    void abrirNavegador() throws IOException {
        Files.createDirectories(REPORTES);
        ChromeOptions opts = new ChromeOptions();
        if (Config.HEADLESS) opts.addArguments("--headless=new");
        opts.addArguments("--window-size=1366,850", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(opts); // Selenium Manager descarga el chromedriver
    }

    @AfterEach
    void cerrarNavegador() {
        cajasCreadas.forEach(Datos::eliminarCaja);
        if (driver != null) driver.quit();
    }

    // --------------------------------------------------------------- utilidades
    protected void capturar(String nombre) {
        try {
            Path origen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.createDirectories(REPORTES);
            Files.copy(origen, REPORTES.resolve(nombre + ".png"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignorado) {
            // la captura es solo evidencia; no debe romper la prueba
        }
    }

    protected WebDriverWait espera() {
        return espera(10);
    }

    protected WebDriverWait espera(int segundos) {
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(segundos));
        w.ignoring(StaleElementReferenceException.class);
        return w;
    }

    /** Espera (10 s) hasta que la condición sea verdadera. */
    protected void esperarQue(BooleanSupplier condicion) {
        esperarQue(10, condicion);
    }

    protected void esperarQue(int segundos, BooleanSupplier condicion) {
        espera(segundos).until((Function<WebDriver, Boolean>) d -> condicion.getAsBoolean());
    }

    protected WebElement visible(String css) {
        return espera().until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(css)));
    }

    protected WebElement clicable(String css) {
        return espera().until(ExpectedConditions.elementToBeClickable(By.cssSelector(css)));
    }

    protected boolean estaVisible(String css) {
        List<WebElement> els = driver.findElements(By.cssSelector(css));
        return !els.isEmpty() && els.get(0).isDisplayed();
    }

    protected WebElement porId(String id) {
        return driver.findElement(By.id(id));
    }

    protected String texto(String id) {
        return porId(id).getText();
    }

    protected Object js(String script) {
        return ((JavascriptExecutor) driver).executeScript(script);
    }

    // ------------------------------------------------------------ flujos de UI
    protected void login(String correo, String clave) {
        driver.get(Config.FRONT_URL + "/index.html");
        visible("#login-form");
        porId("correo").sendKeys(correo);
        porId("clave").sendKeys(clave);
        driver.findElement(By.cssSelector(".btn-login")).click();
    }

    protected void loginOk(String correo, String clave) {
        login(correo, clave);
        visible("#app-shell");
        visible("#dashboard-user-email");
    }

    protected void loginAdmin() {
        loginOk(Config.ADMIN_EMAIL, Config.ADMIN_PASS);
    }

    protected void irA(String vista) {
        clicable(".nav-item[data-view=\"" + vista + "\"]").click();
        visible("[data-app-view=\"" + vista + "\"]");
    }

    protected List<WebElement> filas(String tbodyId) {
        return driver.findElements(By.cssSelector("#" + tbodyId + " tr"));
    }

    protected WebElement filaConTexto(String tbodyId, String texto) {
        for (WebElement tr : filas(tbodyId)) {
            try {
                if (tr.getText().contains(texto)) return tr;
            } catch (StaleElementReferenceException ignorada) {
                // la tabla se repintó; se reintenta en la próxima espera
            }
        }
        return null;
    }

    /**
     * Hace clic en un botón de la fila que contiene el texto. La fila se vuelve a buscar en cada
     * reintento, así un repintado de la tabla no deja el elemento obsoleto (StaleElementReference).
     */
    protected void clicEnFila(String tbodyId, String texto, String selectorBoton) {
        esperarQue(() -> {
            WebElement fila = filaConTexto(tbodyId, texto);
            if (fila == null) return false;
            fila.findElement(By.cssSelector(selectorBoton)).click();
            return true;
        });
    }

    /**
     * Emula el tamaño de pantalla por DevTools. window().setSize() no funciona con la ventana
     * visible en Linux (el gestor de ventanas lo ignora), por eso se usa este método.
     */
    protected void emularPantalla(int ancho, int alto) {
        ((ChromeDriver) driver).executeCdpCommand("Emulation.setDeviceMetricsOverride", Map.of(
                "width", ancho, "height", alto, "deviceScaleFactor", 1, "mobile", false));
    }

    protected WebElement esperarFila(String tbodyId, String texto) {
        esperarQue(() -> filaConTexto(tbodyId, texto) != null);
        return filaConTexto(tbodyId, texto);
    }

    /** Registra una caja desde la UI; queda abierto el diálogo con el QR. */
    protected void crearCajaUi(String contenido, String destino) {
        irA("inventario");
        clicable(".inventory-summary .btn-outline").click();
        visible("#registrar-form");
        porId("contenido").sendKeys(contenido);
        porId("zonaDestino").sendKeys(destino);
        driver.findElement(By.cssSelector("#registrar-form .btn-pill")).click();
        visible("#qr-dialog");
    }

    protected void cerrarQr() {
        driver.findElement(By.cssSelector("#qr-dialog .qr-dialog-close")).click();
        esperarQue(() -> !porId("qr-dialog").isDisplayed());
    }

    /** Crea una caja por API (se elimina sola al terminar la prueba). */
    protected String[] nuevaCajaApi() {
        String contenido = "Arroz-" + Datos.RUN_ID + "-" + java.util.UUID.randomUUID().toString().substring(0, 4);
        String zona = "Zona-" + Datos.RUN_ID;
        cajasCreadas.add(Datos.crearCaja(contenido, zona));
        return new String[]{contenido, zona};
    }
}
