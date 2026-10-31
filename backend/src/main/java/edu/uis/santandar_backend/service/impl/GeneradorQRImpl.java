package edu.uis.santandar_backend.service.impl;

import edu.uis.santandar_backend.exception.DatosInvalidosException;
import edu.uis.santandar_backend.service.GeneradorQR;
import org.springframework.stereotype.Component;

@Component
public class GeneradorQRImpl implements GeneradorQR {

    private static final String PREFIJO = "SD-";

    /** Devuelve el texto que codifica el QR. La imagen la dibuja el frontend. */
    @Override
    public String crearEtiqueta(String idCaja) {
        if (idCaja == null || idCaja.isBlank()) {
            throw new DatosInvalidosException("No se puede generar el QR de una caja sin identificador");
        }
        return PREFIJO + idCaja;
    }
}