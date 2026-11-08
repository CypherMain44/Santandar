package edu.uis.santandar_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CajaRequest(
        @NotBlank(message = "El contenido es obligatorio")
        String contenido,

        @NotBlank(message = "La zona de destino es obligatoria")
        String zonaDestino
) {}