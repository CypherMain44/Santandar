package edu.uis.santandar_backend.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant fecha,
        int estado,
        String mensaje,
        Map<String, String> errores
) {}
