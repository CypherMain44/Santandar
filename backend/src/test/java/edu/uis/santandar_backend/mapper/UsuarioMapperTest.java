package edu.uis.santandar_backend.mapper;

import edu.uis.santandar_backend.dto.TipoUsuario;
import edu.uis.santandar_backend.dto.request.RegistroUsuarioRequest;
import edu.uis.santandar_backend.dto.response.UsuarioResponse;
import edu.uis.santandar_backend.model.Administrador;
import edu.uis.santandar_backend.model.Usuario;
import edu.uis.santandar_backend.model.Voluntario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UsuarioMapperTest {

    private UsuarioMapper usuarioMapper;

    @BeforeEach
    void setUp() {
        usuarioMapper = new UsuarioMapper();
    }

    @Test
    void toEntity_Administrador_RetornaInstanciaAdministrador() {
        RegistroUsuarioRequest req = new RegistroUsuarioRequest("admin@test.com", "pass", TipoUsuario.ADMINISTRADOR);

        Usuario usuario = usuarioMapper.toEntity(req, "hashedPass");

        assertNotNull(usuario);
        assertInstanceOf(Administrador.class, usuario);
        assertEquals("admin@test.com", usuario.getCorreo());
        assertEquals("hashedPass", usuario.getClave());
    }

    @Test
    void toEntity_Voluntario_RetornaInstanciaVoluntario() {
        RegistroUsuarioRequest req = new RegistroUsuarioRequest("vol@test.com", "pass", TipoUsuario.VOLUNTARIO);

        Usuario usuario = usuarioMapper.toEntity(req, "hashedPass");

        assertNotNull(usuario);
        assertInstanceOf(Voluntario.class, usuario);
        assertEquals("vol@test.com", usuario.getCorreo());
        assertEquals("hashedPass", usuario.getClave());
    }

    @Test
    void toResponse_ConAdministrador_RetornaTipoAdministrador() {
        Usuario admin = Administrador.builder()
                .id("usr-1")
                .correo("admin@test.com")
                .clave("hashedPass")
                .build();

        UsuarioResponse response = usuarioMapper.toResponse(admin);

        assertNotNull(response);
        assertEquals("usr-1", response.id());
        assertEquals("admin@test.com", response.correo());
        assertEquals(TipoUsuario.ADMINISTRADOR, response.tipo());
    }

    @Test
    void toResponse_ConVoluntario_RetornaTipoVoluntario() {
        Usuario voluntario = Voluntario.builder()
                .id("usr-2")
                .correo("vol@test.com")
                .clave("hashedPass")
                .build();

        UsuarioResponse response = usuarioMapper.toResponse(voluntario);

        assertNotNull(response);
        assertEquals("usr-2", response.id());
        assertEquals("vol@test.com", response.correo());
        assertEquals(TipoUsuario.VOLUNTARIO, response.tipo());
    }
}
