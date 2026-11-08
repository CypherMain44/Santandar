package edu.uis.santandar_backend.mapper;

import edu.uis.santandar_backend.dto.TipoUsuario;
import edu.uis.santandar_backend.dto.request.RegistroUsuarioRequest;
import edu.uis.santandar_backend.dto.response.UsuarioResponse;
import edu.uis.santandar_backend.model.Administrador;
import edu.uis.santandar_backend.model.Usuario;
import edu.uis.santandar_backend.model.Voluntario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    /** La clave llega ya cifrada: el cifrado es responsabilidad del service. */
    public Usuario toEntity(RegistroUsuarioRequest request, String claveCifrada) {
        return switch (request.tipo()) {
            case ADMINISTRADOR -> Administrador.builder()
                    .correo(request.correo())
                    .clave(claveCifrada)
                    .build();
            case VOLUNTARIO -> Voluntario.builder()
                    .correo(request.correo())
                    .clave(claveCifrada)
                    .build();
        };
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        TipoUsuario tipo = usuario instanceof Administrador
                ? TipoUsuario.ADMINISTRADOR
                : TipoUsuario.VOLUNTARIO;
        return new UsuarioResponse(usuario.getId(), usuario.getCorreo(), tipo);
    }
}