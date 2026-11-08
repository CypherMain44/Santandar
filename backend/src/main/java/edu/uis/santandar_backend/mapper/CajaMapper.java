package edu.uis.santandar_backend.mapper;

import edu.uis.santandar_backend.dto.request.CajaRequest;
import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.model.Caja;
import org.springframework.stereotype.Component;

@Component
public class CajaMapper {

    /** No asigna idCaja ni identificadorQR: los genera el service. */
    public Caja toEntity(CajaRequest request) {
        return Caja.builder()
                .contenido(request.contenido())
                .zonaDestino(request.zonaDestino())
                .build();
    }

    public void actualizar(Caja caja, CajaRequest request) {
        caja.setContenido(request.contenido());
        caja.setZonaDestino(request.zonaDestino());
    }

    public CajaResponse toResponse(Caja caja) {
        return new CajaResponse(
                caja.getIdCaja(),
                caja.getIdentificadorQR(),
                caja.getContenido(),
                caja.getZonaDestino(),
                caja.getEstadoCaja());
    }
}