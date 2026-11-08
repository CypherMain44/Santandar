package edu.uis.santandar_backend.dto.request;

import edu.uis.santandar_backend.dto.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        String correo,

        @NotBlank(message = "La clave es obligatoria")
        @Size(min = 8, message = "La clave debe tener al menos 8 caracteres")
        String clave,

        @NotNull(message = "El tipo de usuario es obligatorio")
        TipoUsuario tipo
) {}
