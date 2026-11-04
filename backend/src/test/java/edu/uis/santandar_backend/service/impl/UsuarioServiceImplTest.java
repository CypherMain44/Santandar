package edu.uis.santandar_backend.service.impl;

import java.util.List;
import java.util.Optional;

import edu.uis.santandar_backend.dto.TipoUsuario;
import edu.uis.santandar_backend.dto.request.ActualizarUsuarioRequest;
import edu.uis.santandar_backend.dto.request.RegistroUsuarioRequest;
import edu.uis.santandar_backend.dto.response.UsuarioResponse;
import edu.uis.santandar_backend.exception.DatosInvalidosException;
import edu.uis.santandar_backend.exception.RecursoDuplicadoException;
import edu.uis.santandar_backend.exception.RecursoNoEncontradoException;
import edu.uis.santandar_backend.mapper.UsuarioMapper;
import edu.uis.santandar_backend.model.Administrador;
import edu.uis.santandar_backend.model.Usuario;
import edu.uis.santandar_backend.model.Voluntario;
import edu.uis.santandar_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Administrador admin;
    private Voluntario voluntario;
    private RegistroUsuarioRequest registroRequest;
    private UsuarioResponse usuarioResponse;

    @BeforeEach
    void setUp() {
        admin = Administrador.builder()
                .id("usr-1")
                .correo("admin@test.com")
                .clave("encodedPass")
                .build();

        voluntario = Voluntario.builder()
                .id("usr-2")
                .correo("voluntario@test.com")
                .clave("encodedPass")
                .build();

        registroRequest = new RegistroUsuarioRequest("admin@test.com", "pass123", TipoUsuario.ADMINISTRADOR);
        usuarioResponse = new UsuarioResponse("usr-1", "admin@test.com", TipoUsuario.ADMINISTRADOR);
    }

    @Test
    void registrarUsuario_Exitoso() {
        when(usuarioRepository.existsByCorreo("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(usuarioMapper.toEntity(registroRequest, "encodedPass")).thenReturn(admin);
        when(usuarioRepository.save(admin)).thenReturn(admin);
        when(usuarioMapper.toResponse(admin)).thenReturn(usuarioResponse);

        UsuarioResponse resultado = usuarioService.registrarUsuario(registroRequest);

        assertNotNull(resultado);
        assertEquals("usr-1", resultado.id());
        verify(usuarioRepository).save(admin);
    }

    @Test
    void registrarUsuario_CorreoDuplicado_LanzaExcepcion() {
        when(usuarioRepository.existsByCorreo("admin@test.com")).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> usuarioService.registrarUsuario(registroRequest));
    }

    @Test
    void actualizarUsuario_ExitosoSinCambiarClave() {
        ActualizarUsuarioRequest updateReq = new ActualizarUsuarioRequest("nuevo@test.com", "");
        when(usuarioRepository.findById("usr-1")).thenReturn(Optional.of(admin));
        when(usuarioRepository.existsByCorreo("nuevo@test.com")).thenReturn(false);
        when(usuarioRepository.save(admin)).thenReturn(admin);
        when(usuarioMapper.toResponse(admin)).thenReturn(usuarioResponse);

        UsuarioResponse resultado = usuarioService.actualizarUsuario("usr-1", updateReq);

        assertNotNull(resultado);
        assertEquals("nuevo@test.com", admin.getCorreo());
    }

    @Test
    void actualizarUsuario_CorreoExisteEnOtroUsuario_LanzaExcepcion() {
        ActualizarUsuarioRequest updateReq = new ActualizarUsuarioRequest("existente@test.com", "pass");
        when(usuarioRepository.findById("usr-1")).thenReturn(Optional.of(admin));
        when(usuarioRepository.existsByCorreo("existente@test.com")).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> usuarioService.actualizarUsuario("usr-1", updateReq));
    }

    @Test
    void eliminarUsuario_UnicoAdmin_LanzaExcepcion() {
        when(usuarioRepository.findById("usr-1")).thenReturn(Optional.of(admin));
        when(usuarioRepository.findAll()).thenReturn(List.of(admin));

        assertThrows(DatosInvalidosException.class, () -> usuarioService.eliminarUsuario("usr-1"));
    }

    @Test
    void eliminarUsuario_Voluntario_Exitoso() {
        when(usuarioRepository.findById("usr-2")).thenReturn(Optional.of(voluntario));
        doNothing().when(usuarioRepository).delete(voluntario);

        usuarioService.eliminarUsuario("usr-2");

        verify(usuarioRepository).delete(voluntario);
    }

    @Test
    void consultarUsuario_Exitoso() {
        when(usuarioRepository.findById("usr-1")).thenReturn(Optional.of(admin));
        when(usuarioMapper.toResponse(admin)).thenReturn(usuarioResponse);

        UsuarioResponse resultado = usuarioService.consultarUsuario("usr-1");

        assertNotNull(resultado);
        assertEquals("usr-1", resultado.id());
    }

    @Test
    void consultarUsuario_NoExiste_LanzaExcepcion() {
        when(usuarioRepository.findById("usr-99")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> usuarioService.consultarUsuario("usr-99"));
    }

    @Test
    void listarUsuarios_Exitoso() {
        when(usuarioRepository.findAll()).thenReturn(List.of(admin, voluntario));
        when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(usuarioResponse);

        List<UsuarioResponse> resultado = usuarioService.listarUsuarios();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }
}
