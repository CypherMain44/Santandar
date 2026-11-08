package edu.uis.santandar_backend.service.impl;

import edu.uis.santandar_backend.service.impl.BrevoCorreoVerificacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import org.springframework.http.HttpStatus;

class BrevoCorreoVerificacionServiceImplTest {

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;
    private BrevoCorreoVerificacionServiceImpl service;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        service = new BrevoCorreoVerificacionServiceImpl(restClientBuilder);
        ReflectionTestUtils.setField(service, "apiKey", "test-brevo-key");
        ReflectionTestUtils.setField(service, "senderEmail", "verified@example.com");
        ReflectionTestUtils.setField(service, "senderName", "SantanDar");
    }

    @Test
    void enviarCodigo_UsaApiTransaccionalDeBrevo() {
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(POST))
                .andExpect(header("api-key", "test-brevo-key"))
                .andExpect(content().json("""
                        {
                          "sender": {"name": "SantanDar", "email": "verified@example.com"},
                          "to": [{"email": "user@example.com"}],
                          "subject": "Código para restablecer tu contraseña de SantanDar",
                          "textContent": "Tu código de verificación es: 123456\\n\\nEl código vence en 10 minutos. Si no solicitaste este cambio, ignora este mensaje."
                        }
                        """))
                .andRespond(withStatus(HttpStatus.CREATED));

        service.enviarCodigoRestablecimiento("user@example.com", "123456");

        server.verify();
    }

    @Test
    void enviarCodigo_SinConfiguracion_RechazaLaSolicitud() {
        ReflectionTestUtils.setField(service, "apiKey", "");

        assertThrows(IllegalStateException.class,
                () -> service.enviarCodigoRestablecimiento("user@example.com", "123456"));
        server.verify();
    }
}
