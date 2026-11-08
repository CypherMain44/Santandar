package edu.uis.santandar_backend.dto.response;

public record LoginResponse(
        String token,
        String tipoToken,
        UsuarioResponse usuario
) {}