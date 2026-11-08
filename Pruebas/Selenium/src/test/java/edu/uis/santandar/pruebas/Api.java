package edu.uis.santandar.pruebas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Cliente HTTP mínimo para preparar/limpiar datos y probar la seguridad de la API. */
public final class Api {

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final ObjectMapper JSON = new ObjectMapper();

    private Api() {}

    public record Respuesta(int status, String cuerpo) {
        public JsonNode json() {
            try {
                return JSON.readTree(cuerpo);
            } catch (Exception e) {
                throw new IllegalStateException("Respuesta no es JSON: " + cuerpo, e);
            }
        }
    }

    /** Envía una petición; token y cuerpo pueden ser null. */
    public static Respuesta enviar(String metodo, String ruta, String token, String cuerpo) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(Config.API_URL + ruta))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");
            if (token != null) b.header("Authorization", "Bearer " + token);
            b.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(cuerpo));
            HttpResponse<String> r = HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
            return new Respuesta(r.statusCode(), r.body());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo conectar con la API (" + Config.API_URL + "): " + e.getMessage(), e);
        }
    }

    public static String login(String correo, String clave) {
        Respuesta r = enviar("POST", "/api/auth/login", null,
                "{\"correo\":\"" + correo + "\",\"clave\":\"" + clave + "\"}");
        if (r.status() != 200) {
            throw new IllegalStateException("Login por API falló (HTTP " + r.status() + "): " + r.cuerpo());
        }
        return r.json().get("token").asText();
    }
}
