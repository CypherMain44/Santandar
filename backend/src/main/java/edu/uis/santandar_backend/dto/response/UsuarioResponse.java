package edu.uis.santandar_backend.dto.response;

import edu.uis.santandar_backend.dto.TipoUsuario;

public record UsuarioResponse(
        String id,
        String correo,
        TipoUsuario tipo
) {}