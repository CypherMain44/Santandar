package edu.uis.santandar_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        String correo,

        /** Opcional: si viene vacía, se conserva la clave actual. */
        @Size(min = 8, message = "La clave debe tener al menos 8 caracteres")
        String clave
) {}