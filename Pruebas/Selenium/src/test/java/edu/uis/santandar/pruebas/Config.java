package edu.uis.santandar.pruebas;

/**
 * Configuración de las pruebas. Cada valor se toma de (en este orden):
 * propiedad -D (p. ej. -Dadmin.email=...), variable de entorno (ADMIN_EMAIL) o el valor por defecto.
 */
public final class Config {

    public static final String FRONT_URL = get("front.url", "FRONT_URL", "http://localhost:5500");
    public static final String API_URL = get("api.url", "API_URL", "http://localhost:8080");
    public static final String ADMIN_EMAIL = get("admin.email", "ADMIN_EMAIL", "admin@santandar.test");
    public static final String ADMIN_PASS = get("admin.pass", "ADMIN_PASS", "Admin12345");
    public static final boolean HEADLESS = "1".equals(get("headless", "HEADLESS", "0"))
            || "true".equalsIgnoreCase(get("headless", "HEADLESS", "0"));

    private Config() {}

    private static String get(String prop, String env, String def) {
        String v = System.getProperty(prop);
        if (v == null || v.isBlank() || v.startsWith("${")) v = System.getenv(env);
        if (v == null || v.isBlank()) v = def;
        return v.replaceAll("/+$", "");
    }
}
