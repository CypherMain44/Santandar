package edu.uis.santandar_backend.service;

import edu.uis.santandar_backend.dto.request.ConfirmarRestablecimientoRequest;
import edu.uis.santandar_backend.dto.request.SolicitarRestablecimientoRequest;

public interface RestablecimientoClaveService {

    void solicitar(SolicitarRestablecimientoRequest request);

    void confirmar(ConfirmarRestablecimientoRequest request);
}
