package edu.uis.santandar_backend.dto.response;

import edu.uis.santandar_backend.model.enums.EstadoCaja;

public record CajaResponse(
        String idCaja,
        String identificadorQR,
        String contenido,
        String zonaDestino,
        EstadoCaja estadoCaja
) {}