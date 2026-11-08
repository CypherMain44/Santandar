package edu.uis.santandar_backend.service.impl;

import java.util.UUID;

import edu.uis.santandar_backend.dto.request.CajaRequest;
import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.exception.RecursoDuplicadoException;
import edu.uis.santandar_backend.exception.RecursoNoEncontradoException;
import edu.uis.santandar_backend.mapper.CajaMapper;
import edu.uis.santandar_backend.model.Caja;
import edu.uis.santandar_backend.model.enums.EstadoCaja;
import edu.uis.santandar_backend.repository.CajaRepository;
import edu.uis.santandar_backend.service.CajaGestionService;
import edu.uis.santandar_backend.service.GeneradorQR;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CajaGestionServiceImpl implements CajaGestionService {

    private final CajaRepository cajaRepository;
    private final CajaMapper cajaMapper;
    private final GeneradorQR generadorQR;

    @Override
    public CajaResponse registrarCaja(CajaRequest request) {
        Caja caja = cajaMapper.toEntity(request);
        caja.setIdCaja(UUID.randomUUID().toString());

        String identificadorQR = generadorQR.crearEtiqueta(caja.getIdCaja());
        if (cajaRepository.existsByIdentificadorQR(identificadorQR)) {
            throw new RecursoDuplicadoException("El identificador QR ya existe");
        }
        caja.setIdentificadorQR(identificadorQR);

        return cajaMapper.toResponse(cajaRepository.save(caja));
    }

    @Override
    public CajaResponse actualizarCaja(String idCaja, CajaRequest request) {
        Caja caja = buscarCaja(idCaja);
        cajaMapper.actualizar(caja, request);
        return cajaMapper.toResponse(cajaRepository.save(caja));
    }

    @Override
    public void eliminarCaja(String idCaja) {
        Caja caja = buscarCaja(idCaja);
        cajaRepository.delete(caja);
    }

    @Override
    public CajaResponse cambiarEstado(String idCaja, EstadoCaja estado) {
        Caja caja = buscarCaja(idCaja);
        caja.setEstadoCaja(estado);
        return cajaMapper.toResponse(cajaRepository.save(caja));
    }

    private Caja buscarCaja(String idCaja) {
        return cajaRepository.findById(idCaja)
                .orElseThrow(() -> new RecursoNoEncontradoException("La caja con id " + idCaja + " no existe"));
    }
}
