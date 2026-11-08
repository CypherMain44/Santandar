package edu.uis.santandar_backend.service.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uis.santandar_backend.dto.request.ConfirmarRestablecimientoRequest;
import edu.uis.santandar_backend.dto.request.SolicitarRestablecimientoRequest;
import edu.uis.santandar_backend.exception.DatosInvalidosException;
import edu.uis.santandar_backend.model.CodigoRestablecimiento;
import edu.uis.santandar_backend.model.Voluntario;
import edu.uis.santandar_backend.repository.CodigoRestablecimientoRepository;
import edu.uis.santandar_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestablecimientoClaveServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CodigoRestablecimientoRepository codigoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private RestablecimientoClaveServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "fromAddress", "no-reply@test.com");
    }

    @Test
    void solicitar_CorreoRegistrado_EnviaCodigoDeUnSoloUso() {
        when(usuarioRepository.findByCorreoIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(Voluntario.builder().correo("user@test.com").build()));
        when(codigoRepository.findById("user@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("codigo-hash");
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        SolicitarRestablecimientoRequest request = new SolicitarRestablecimientoRequest(" User@Test.com ");

        service.solicitar(request);

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        String text = mailCaptor.getValue().getText();
        Matcher code = Pattern.compile("\\d{6}").matcher(text);
        assertNotNull(text);
        org.junit.jupiter.api.Assertions.assertTrue(code.find());
        ArgumentCaptor<CodigoRestablecimiento> codeCaptor = ArgumentCaptor.forClass(CodigoRestablecimiento.class);
        verify(codigoRepository).save(codeCaptor.capture());
        assertEquals("user@test.com", codeCaptor.getValue().getCorreo());
        assertEquals("codigo-hash", codeCaptor.getValue().getCodigoHash());
        assertEquals("user@test.com", mailCaptor.getValue().getTo()[0]);
    }

    @Test
    void solicitar_CorreoDesconocido_NoRevelaSiLaCuentaExiste() {
        when(usuarioRepository.findByCorreoIgnoreCase("missing@test.com")).thenReturn(Optional.empty());

        service.solicitar(new SolicitarRestablecimientoRequest("missing@test.com"));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(codigoRepository, never()).save(any(CodigoRestablecimiento.class));
    }

    @Test
    void solicitar_DentroDelTiempoDeEspera_NoEnviaOtroCodigo() {
        when(usuarioRepository.findByCorreoIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(Voluntario.builder().correo("user@test.com").build()));
        when(codigoRepository.findById("user@test.com")).thenReturn(Optional.of(CodigoRestablecimiento.builder()
                .correo("user@test.com")
                .solicitadoEn(Instant.now())
                .expiraEn(Instant.now().plusSeconds(600))
                .build()));

        service.solicitar(new SolicitarRestablecimientoRequest("user@test.com"));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void confirmar_CodigoValido_ActualizaContrasenaYEliminaCodigo() {
        Voluntario user = Voluntario.builder().correo("user@test.com").clave("old-hash").build();
        CodigoRestablecimiento code = CodigoRestablecimiento.builder()
                .correo("user@test.com")
                .codigoHash("code-hash")
                .expiraEn(Instant.now().plusSeconds(600))
                .intentos(0)
                .build();
        when(codigoRepository.findById("user@test.com")).thenReturn(Optional.of(code));
        when(passwordEncoder.matches("123456", "code-hash")).thenReturn(true);
        when(usuarioRepository.findByCorreoIgnoreCase("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.confirmar(new ConfirmarRestablecimientoRequest(
                "User@Test.com", "123456", "new-password"));

        assertEquals("new-hash", user.getClave());
        verify(usuarioRepository).save(user);
        verify(codigoRepository).delete(code);
    }

    @Test
    void confirmar_CodigoIncorrecto_SumaIntentoYRechaza() {
        CodigoRestablecimiento code = CodigoRestablecimiento.builder()
                .correo("user@test.com")
                .codigoHash("code-hash")
                .expiraEn(Instant.now().plusSeconds(600))
                .intentos(0)
                .build();
        when(codigoRepository.findById("user@test.com")).thenReturn(Optional.of(code));
        when(passwordEncoder.matches("000000", "code-hash")).thenReturn(false);

        assertThrows(DatosInvalidosException.class, () -> service.confirmar(
                new ConfirmarRestablecimientoRequest("user@test.com", "000000", "new-password")));

        assertEquals(1, code.getIntentos());
        verify(codigoRepository).save(code);
    }

    @Test
    void confirmar_QuintoIntentoIncorrecto_InvalidaCodigo() {
        CodigoRestablecimiento code = CodigoRestablecimiento.builder()
                .correo("user@test.com")
                .codigoHash("code-hash")
                .expiraEn(Instant.now().plusSeconds(600))
                .intentos(4)
                .build();
        when(codigoRepository.findById("user@test.com")).thenReturn(Optional.of(code));
        when(passwordEncoder.matches("000000", "code-hash")).thenReturn(false);

        assertThrows(DatosInvalidosException.class, () -> service.confirmar(
                new ConfirmarRestablecimientoRequest("user@test.com", "000000", "new-password")));

        assertEquals(5, code.getIntentos());
        verify(codigoRepository).delete(code);
    }

    @Test
    void confirmar_CodigoExpirado_LoElimina() {
        CodigoRestablecimiento code = CodigoRestablecimiento.builder()
                .correo("user@test.com")
                .codigoHash("code-hash")
                .expiraEn(Instant.now().minusSeconds(1))
                .intentos(0)
                .build();
        when(codigoRepository.findById("user@test.com")).thenReturn(Optional.of(code));

        assertThrows(DatosInvalidosException.class, () -> service.confirmar(
                new ConfirmarRestablecimientoRequest("user@test.com", "123456", "new-password")));

        verify(codigoRepository).delete(code);
    }
}
