package edu.uis.santandar_backend.service.impl;

import java.util.List;
import java.util.Optional;

import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.exception.RecursoNoEncontradoException;
import edu.uis.santandar_backend.mapper.CajaMapper;
import edu.uis.santandar_backend.model.Caja;
import edu.uis.santandar_backend.model.enums.EstadoCaja;
import edu.uis.santandar_backend.repository.CajaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CajaConsultaServiceImplTest {

    @Mock
    private CajaRepository cajaRepository;

    @Mock
    private CajaMapper cajaMapper;

    @InjectMocks
    private CajaConsultaServiceImpl cajaConsultaService;

    private Caja caja;
    private CajaResponse cajaResponse;

    @BeforeEach
    void setUp() {
        caja = Caja.builder()
                .idCaja("caja-1")
                .identificadorQR("QR123")
                .contenido("Ropa")
                .zonaDestino("Zona A")
                .estadoCaja(EstadoCaja.ALMACENADA)
                .build();

        cajaResponse = new CajaResponse("caja-1", "QR123", "Ropa", "Zona A", EstadoCaja.ALMACENADA);
    }

    @Test
    void visualizarInventario_DebeRetornarListaDeCajas() {
        when(cajaRepository.findAll()).thenReturn(List.of(caja));
        when(cajaMapper.toResponse(caja)).thenReturn(cajaResponse);

        List<CajaResponse> resultado = cajaConsultaService.visualizarInventario();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("caja-1", resultado.get(0).idCaja());
        verify(cajaRepository).findAll();
    }

    @Test
    void filtrarRecursos_ConCriterioVacio_DebeVisualizarInventario() {
        when(cajaRepository.findAll()).thenReturn(List.of(caja));
        when(cajaMapper.toResponse(caja)).thenReturn(cajaResponse);

        List<CajaResponse> resultado = cajaConsultaService.filtrarRecursos("   ");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cajaRepository).findAll();
    }

    @Test
    void filtrarRecursos_ConCriterioValido_DebeBuscarPorCriterio() {
        when(cajaRepository.buscarPorCriterio(anyString())).thenReturn(List.of(caja));
        when(cajaMapper.toResponse(caja)).thenReturn(cajaResponse);

        List<CajaResponse> resultado = cajaConsultaService.filtrarRecursos("Ropa");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cajaRepository).buscarPorCriterio("\\QRopa\\E");
    }

    @Test
    void consultarInformacionCaja_CuandoExiste_DebeRetornarCaja() {
        when(cajaRepository.findById("caja-1")).thenReturn(Optional.of(caja));
        when(cajaMapper.toResponse(caja)).thenReturn(cajaResponse);

        CajaResponse resultado = cajaConsultaService.consultarInformacionCaja("caja-1");

        assertNotNull(resultado);
        assertEquals("caja-1", resultado.idCaja());
    }

    @Test
    void consultarInformacionCaja_CuandoNoExiste_DebeLanzarExcepcion() {
        when(cajaRepository.findById("caja-1")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> cajaConsultaService.consultarInformacionCaja("caja-1"));
    }

    @Test
    void consultarPorQr_CuandoExiste_DebeRetornarCaja() {
        when(cajaRepository.findByIdentificadorQR("QR123")).thenReturn(Optional.of(caja));
        when(cajaMapper.toResponse(caja)).thenReturn(cajaResponse);

        CajaResponse resultado = cajaConsultaService.consultarPorQr("QR123");

        assertNotNull(resultado);
        assertEquals("QR123", resultado.identificadorQR());
    }

    @Test
    void consultarPorQr_CuandoNoExiste_DebeLanzarExcepcion() {
        when(cajaRepository.findByIdentificadorQR("QR123")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> cajaConsultaService.consultarPorQr("QR123"));
    }
}
