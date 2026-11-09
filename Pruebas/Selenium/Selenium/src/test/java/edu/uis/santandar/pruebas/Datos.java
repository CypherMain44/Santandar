package edu.uis.santandar.pruebas;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/** Datos de prueba creados por API y eliminados al terminar. */
public final class Datos {

    /** Sufijo único para identificar lo creado en esta corrida. */
    public static final String RUN_ID = UUID.randomUUID().toString().substring(0, 6);

    public record Voluntario(String id, String correo, String clave) {}

    private static String adminToken;
    private static Voluntario voluntario;

    private Datos() {}

    public static synchronized String adminToken() {
        if (adminToken == null) adminToken = Api.login(Config.ADMIN_EMAIL, Config.ADMIN_PASS);
        return adminToken;
    }

    /** Voluntario único de la corrida; se borra al terminar la JVM. */
    public static synchronized Voluntario voluntario() {
        if (voluntario == null) {
            String correo = "vol_" + UUID.randomUUID().toString().substring(0, 8) + "@santandar.test";
            String clave = "Voluntario123";
            voluntario = new Voluntario(crearVoluntario(correo, clave), correo, clave);
            Runtime.getRuntime().addShutdownHook(new Thread(() ->
                    Api.enviar("DELETE", "/api/usuarios/" + voluntario.id(), adminToken(), null)));
        }
        return voluntario;
    }

    public static String crearVoluntario(String correo, String clave) {
        Api.Respuesta r = Api.enviar("POST", "/api/usuarios", adminToken(),
                "{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\",\"tipo\":\"VOLUNTARIO\"}");
        if (r.status() != 201) throw new IllegalStateException("No se creó el voluntario: " + r.status() + " " + r.cuerpo());
        return r.json().get("id").asText();
    }

    public static void eliminarUsuarioPorCorreo(String correo) {
        for (JsonNode u : Api.enviar("GET", "/api/usuarios", adminToken(), null).json()) {
            if (correo.equals(u.get("correo").asText())) {
                Api.enviar("DELETE", "/api/usuarios/" + u.get("id").asText(), adminToken(), null);
            }
        }
    }

    /** Crea una caja por API y devuelve su id. */
    public static String crearCaja(String contenido, String zona) {
        Api.Respuesta r = Api.enviar("POST", "/api/cajas", adminToken(),
                "{\"contenido\":\"" + contenido + "\",\"zonaDestino\":\"" + zona + "\"}");
        if (r.status() != 201) throw new IllegalStateException("No se creó la caja: " + r.status() + " " + r.cuerpo());
        return r.json().get("idCaja").asText();
    }

    public static void eliminarCaja(String id) {
        Api.enviar("DELETE", "/api/cajas/" + id, adminToken(), null);
    }

    /** Elimina todas las cajas cuyo contenido empiece con el prefijo (limpieza de lo creado desde la UI). */
    public static void eliminarCajasPorPrefijo(String prefijo) {
        for (JsonNode c : Api.enviar("GET", "/api/cajas", adminToken(), null).json()) {
            if (c.get("contenido").asText().startsWith(prefijo)) eliminarCaja(c.get("idCaja").asText());
        }
    }
}
