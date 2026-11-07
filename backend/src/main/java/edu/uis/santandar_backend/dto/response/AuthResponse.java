package edu.uis.santandar_backend.dto.response;

import edu.uis.santandar_backend.dto.TipoUsuario;

public record AuthResponse(String token, TipoUsuario tipo) {
}
