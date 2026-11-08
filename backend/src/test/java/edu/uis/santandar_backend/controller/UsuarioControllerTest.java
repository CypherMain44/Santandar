package edu.uis.santandar_backend.controller;

import java.util.List;

import edu.uis.santandar_backend.dto.TipoUsuario;
import edu.uis.santandar_backend.dto.request.ActualizarUsuarioRequest;
import edu.uis.santandar_backend.dto.request.ConfirmarRestablecimientoRequest;
import edu.uis.santandar_backend.dto.request.LoginRequest;
import edu.uis.santandar_backend.dto.request.RegistroUsuarioRequest;
import edu.uis.santandar_backend.dto.request.SolicitarRestablecimientoRequest;
import edu.uis.santandar_backend.dto.response.AuthResponse;
import edu.uis.santandar_backend.dto.response.RestablecimientoResponse;
import edu.uis.santandar_backend.dto.response.UsuarioResponse;
import edu.uis.santandar_backend.config.JwtService;
import edu.uis.santandar_backend.model.Administrador;
import edu.uis.santandar_backend.model.Voluntario;
import edu.uis.santandar_backend.service.UsuarioService;
import edu.uis.santandar_backend.service.RestablecimientoClaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RestablecimientoClaveService restablecimientoClaveService;

    @InjectMocks
    private UsuarioController usuarioController;

    private UsuarioResponse response;

    @BeforeEach
    void setUp() {
        response = new UsuarioResponse("usr-1", "user@test.com", TipoUsuario.VOLUNTARIO);
    }

    @Test
    void login_DebeAutenticarUsuarioYDevolverToken() {
        LoginRequest request = new LoginRequest("user@test.com", "pass");
        Administrador usuario = Administrador.builder()
                .id("usr-1")
                .correo("user@test.com")
                .clave("encodedPass")
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        when(usuarioService.loadUserByUsername("user@test.com")).thenReturn(usuario);
        when(jwtService.generateToken(usuario)).thenReturn("jwt-token");

        AuthResponse resultado = usuarioController.login(request);

        assertEquals("jwt-token", resultado.token());
        assertEquals(TipoUsuario.ADMINISTRADOR, resultado.tipo());
        assertEquals("ROLE_ADMINISTRADOR", usuario.getAuthorities().iterator().next().getAuthority());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioService).loadUserByUsername("user@test.com");
    }

    @Test
    void solicitarRestablecimiento_DebeDelegarAlServicioYNoRevelarLaCuenta() {
        SolicitarRestablecimientoRequest request = new SolicitarRestablecimientoRequest("user@test.com");

        RestablecimientoResponse resultado = usuarioController.solicitarRestablecimiento(request);

        assertEquals("Si el correo está registrado, recibirás un código de verificación.", resultado.mensaje());
        verify(restablecimientoClaveService).solicitar(request);
    }

    @Test
    void confirmarRestablecimiento_DebeDelegarAlServicio() {
        ConfirmarRestablecimientoRequest request = new ConfirmarRestablecimientoRequest(
                "user@test.com", "123456", "new-password");

        RestablecimientoResponse resultado = usuarioController.confirmarRestablecimiento(request);

        assertEquals("La contraseña se restableció correctamente.", resultado.mensaje());
        verify(restablecimientoClaveService).confirmar(request);
    }

    @Test
    void voluntario_DebeTenerAutoridadDeVoluntario() {
        Voluntario usuario = Voluntario.builder().build();

        assertEquals("ROLE_VOLUNTARIO", usuario.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void registrar_DebeDelegarAlServicio() {
        RegistroUsuarioRequest regReq = new RegistroUsuarioRequest("user@test.com", "password123", TipoUsuario.VOLUNTARIO);
        when(usuarioService.registrarUsuario(regReq)).thenReturn(response);

        UsuarioResponse resultado = usuarioController.registrar(regReq);

        assertNotNull(resultado);
        assertEquals("usr-1", resultado.id());
        verify(usuarioService).registrarUsuario(regReq);
    }

    @Test
    void listar_DebeDelegarAlServicio() {
        when(usuarioService.listarUsuarios()).thenReturn(List.of(response));

        List<UsuarioResponse> resultado = usuarioController.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(usuarioService).listarUsuarios();
    }

    @Test
    void consultar_DebeDelegarAlServicio() {
        when(usuarioService.consultarUsuario("usr-1")).thenReturn(response);

        UsuarioResponse resultado = usuarioController.consultar("usr-1");

        assertNotNull(resultado);
        assertEquals("usr-1", resultado.id());
        verify(usuarioService).consultarUsuario("usr-1");
    }

    @Test
    void actualizar_DebeDelegarAlServicio() {
        ActualizarUsuarioRequest actReq = new ActualizarUsuarioRequest("user@test.com", "newpass");
        when(usuarioService.actualizarUsuario("usr-1", actReq)).thenReturn(response);

        UsuarioResponse resultado = usuarioController.actualizar("usr-1", actReq);

        assertNotNull(resultado);
        verify(usuarioService).actualizarUsuario("usr-1", actReq);
    }

    @Test
    void eliminar_DebeDelegarAlServicio() {
        usuarioController.eliminar("usr-1");

        verify(usuarioService).eliminarUsuario("usr-1");
    }
}
