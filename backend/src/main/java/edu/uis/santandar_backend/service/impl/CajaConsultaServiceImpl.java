package edu.uis.santandar_backend.service.impl;

import java.util.List;
import java.util.regex.Pattern;

import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.exception.RecursoNoEncontradoException;
import edu.uis.santandar_backend.mapper.CajaMapper;
import edu.uis.santandar_backend.repository.CajaRepository;
import edu.uis.santandar_backend.service.CajaConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CajaConsultaServiceImpl implements CajaConsultaService {

    private final CajaRepository cajaRepository;
    private final CajaMapper cajaMapper;

    @Override
    public List<CajaResponse> visualizarInventario() {
        return cajaRepository.findAll().stream()
                .map(cajaMapper::toResponse)
                .toList();
    }

    @Override
    public List<CajaResponse> filtrarRecursos(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return visualizarInventario();
        }
        // Pattern.quote evita que símbolos como "." o "*" se interpreten como regex
        return cajaRepository.buscarPorCriterio(Pattern.quote(criterio.trim())).stream()
                .map(cajaMapper::toResponse)
                .toList();
    }

    @Override
    public CajaResponse consultarInformacionCaja(String idCaja) {
        return cajaRepository.findById(idCaja)
                .map(cajaMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("La caja con id " + idCaja + " no existe"));
    }

    @Override
    public CajaResponse consultarPorQr(String identificadorQR) {
        return cajaRepository.findByIdentificadorQR(identificadorQR)
                .map(cajaMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("El código QR no corresponde a ninguna caja"));
    }
}