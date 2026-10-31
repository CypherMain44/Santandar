package edu.uis.santandar_backend.service;

import java.util.List;

import edu.uis.santandar_backend.dto.response.CajaResponse;

public interface CajaConsultaService {

    List<CajaResponse> visualizarInventario();

    List<CajaResponse> filtrarRecursos(String criterio);

    CajaResponse consultarInformacionCaja(String idCaja);

    CajaResponse consultarPorQr(String identificadorQR);
}