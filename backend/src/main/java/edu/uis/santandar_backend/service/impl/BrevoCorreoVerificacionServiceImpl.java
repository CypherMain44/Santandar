package edu.uis.santandar_backend.service.impl;

import java.util.List;
import java.util.Map;

import edu.uis.santandar_backend.service.CorreoVerificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class BrevoCorreoVerificacionServiceImpl implements CorreoVerificacionService {

    private static final String BREVO_SEND_EMAIL_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient.Builder restClientBuilder;

    @Value("${app.brevo.api-key:}")
    private String apiKey;

    @Value("${app.brevo.sender-email:}")
    private String senderEmail;

    @Value("${app.brevo.sender-name:SantanDar}")
    private String senderName;

    @Override
    public void enviarCodigoRestablecimiento(String correo, String codigo) {
        if (apiKey.isBlank() || senderEmail.isBlank()) {
            throw new IllegalStateException("Brevo no está configurado. Define BREVO_API_KEY y BREVO_SENDER_EMAIL.");
        }

        Map<String, Object> message = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", correo)),
                "subject", "Código para restablecer tu contraseña de SantanDar",
                "textContent", "Tu código de verificación es: " + codigo
                        + "\n\nEl código vence en 10 minutos. Si no solicitaste este cambio, ignora este mensaje."
        );

        try {
            restClientBuilder.build()
                    .post()
                    .uri(BREVO_SEND_EMAIL_URL)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(message)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new IllegalStateException("Brevo no pudo enviar el correo de recuperación.", ex);
        }
    }
}
