package edu.uis.santandar.pruebas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** RNF4 - Seguridad: protección de endpoints por token y por rol (no abre navegador). */
class SeguridadApiTest {

    private String cajaId;

    @AfterEach
    void limpiar() {
        if (cajaId != null) Datos.eliminarCaja(cajaId);
    }

    @Test
    @DisplayName("SEL25 RNF4 La API exige token")
    void SEL25_apiExigeToken() {
        String[][] casos = {{"GET", "/api/cajas"}, {"POST", "/api/cajas"}, {"GET", "/api/usuarios"}};
        for (String[] c : casos) {
            int status = Api.enviar(c[0], c[1], null, c[0].equals("POST") ? "{}" : null).status();
            assertTrue(status == 401 || status == 403, c[0] + " " + c[1] + " respondió " + status + " sin token");
        }
    }

    @Test
    @DisplayName("SEL26 RNF4 El voluntario no puede modificar por API")
    void SEL26_voluntarioNoModifica() {
        cajaId = Datos.crearCaja("Seg-" + Datos.RUN_ID, "Zona-seg");
        Datos.Voluntario v = Datos.voluntario();
        String token = Api.login(v.correo(), v.clave());

        assertEquals(403, Api.enviar("POST", "/api/cajas", token, "{\"contenido\":\"x\",\"zonaDestino\":\"y\"}").status());
        assertEquals(403, Api.enviar("DELETE", "/api/cajas/" + cajaId, token, null).status());
        assertEquals(403, Api.enviar("GET", "/api/usuarios", token, null).status());
        assertEquals(200, Api.enviar("GET", "/api/cajas", token, null).status(), "El voluntario sí debe poder consultar");
    }
}
