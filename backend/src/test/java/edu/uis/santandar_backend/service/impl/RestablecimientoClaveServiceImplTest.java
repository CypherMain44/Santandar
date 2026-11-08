package edu.uis.santandar_backend.service.impl;

import java.time.Instant;
import java.util.Optional;

import edu.uis.santandar_backend.dto.request.ConfirmarRestablecimientoRequest;
import edu.uis.santandar_backend.dto.request.SolicitarRestablecimientoRequest;
import edu.uis.santandar_backend.exception.DatosInvalidosException;
import edu.uis.santandar_backend.model.CodigoRestablecimiento;
import edu.uis.santandar_backend.model.Voluntario;
import edu.uis.santandar_backend.repository.CodigoRestablecimientoRepository;
import edu.uis.santandar_backend.repository.UsuarioRepository;
import edu.uis.santandar_backend.service.CorreoVerificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private CorreoVerificacionService correoVerificacionService;

    @InjectMocks
    private RestablecimientoClaveServiceImpl service;

    @Test
    void solicitar_CorreoRegistrado_EnviaCodigoDeUnSoloUso() {
        when(usuarioRepository.findByCorreoIgnoreCase("user@test.com"))
                .thenReturn(Optional.of(Voluntario.builder().correo("user@test.com").build()));
        when(codigoRepository.findById("user@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("codigo-hash");
        SolicitarRestablecimientoRequest request = new SolicitarRestablecimientoRequest(" User@Test.com ");

        service.solicitar(request);

        ArgumentCaptor<CodigoRestablecimiento> codeCaptor = ArgumentCaptor.forClass(CodigoRestablecimiento.class);
        verify(codigoRepository).save(codeCaptor.capture());
        assertEquals("user@test.com", codeCaptor.getValue().getCorreo());
        assertEquals("codigo-hash", codeCaptor.getValue().getCodigoHash());
        verify(correoVerificacionService).enviarCodigoRestablecimiento(
                org.mockito.ArgumentMatchers.eq("user@test.com"),
                org.mockito.ArgumentMatchers.matches("\\d{6}")
        );
    }

    @Test
    void solicitar_CorreoDesconocido_NoRevelaSiLaCuentaExiste() {
        when(usuarioRepository.findByCorreoIgnoreCase("missing@test.com")).thenReturn(Optional.empty());

        service.solicitar(new SolicitarRestablecimientoRequest("missing@test.com"));

        verify(correoVerificacionService, never()).enviarCodigoRestablecimiento(any(), any());
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

        verify(correoVerificacionService, never()).enviarCodigoRestablecimiento(any(), any());
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
