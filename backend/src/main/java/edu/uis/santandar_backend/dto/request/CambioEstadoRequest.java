package edu.uis.santandar_backend.dto.request;

import edu.uis.santandar_backend.model.enums.EstadoCaja;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoCaja estado
) {}