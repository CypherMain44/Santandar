package edu.uis.santandar_backend.controller;

import java.util.List;

import edu.uis.santandar_backend.dto.TipoUsuario;
import edu.uis.santandar_backend.dto.request.ActualizarUsuarioRequest;
import edu.uis.santandar_backend.dto.request.RegistroUsuarioRequest;
import edu.uis.santandar_backend.dto.response.UsuarioResponse;
import edu.uis.santandar_backend.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private UsuarioResponse response;

    @BeforeEach
    void setUp() {
        response = new UsuarioResponse("usr-1", "user@test.com", TipoUsuario.VOLUNTARIO);
    }

    @Test
    void registrar_DebeDelegarAlServicio() {
        RegistroUsuarioRequest regReq = new RegistroUsuarioRequest("user@test.com", "pass", TipoUsuario.VOLUNTARIO);
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
