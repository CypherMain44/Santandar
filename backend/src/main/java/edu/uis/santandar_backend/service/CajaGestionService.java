package edu.uis.santandar_backend.service;

import edu.uis.santandar_backend.dto.request.CajaRequest;
import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.model.enums.EstadoCaja;

public interface CajaGestionService {

    CajaResponse registrarCaja(CajaRequest request);

    CajaResponse actualizarCaja(String idCaja, CajaRequest request);

    void eliminarCaja(String idCaja);

    CajaResponse cambiarEstado(String idCaja, EstadoCaja estado);
}