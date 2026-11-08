package edu.uis.santandar_backend.controller;

import java.util.List;

import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.model.enums.EstadoCaja;
import edu.uis.santandar_backend.service.CajaConsultaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CajaConsultaControllerTest {

    @Mock
    private CajaConsultaService cajaConsultaService;

    @InjectMocks
    private CajaConsultaController cajaConsultaController;

    private CajaResponse response;

    @BeforeEach
    void setUp() {
        response = new CajaResponse("caja-1", "QR123", "Medicina", "Zona C", EstadoCaja.NO_DESPACHADO);
    }

    @Test
    void listar_SinCriterio_DebeVisualizarInventario() {
        when(cajaConsultaService.visualizarInventario()).thenReturn(List.of(response));

        List<CajaResponse> resultado = cajaConsultaController.listar(null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cajaConsultaService).visualizarInventario();
    }

    @Test
    void listar_ConCriterio_DebeFiltrarRecursos() {
        when(cajaConsultaService.filtrarRecursos("Medicina")).thenReturn(List.of(response));

        List<CajaResponse> resultado = cajaConsultaController.listar("Medicina");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cajaConsultaService).filtrarRecursos("Medicina");
    }

    @Test
    void consultarInformacion_DebeRetornarCaja() {
        when(cajaConsultaService.consultarInformacionCaja("caja-1")).thenReturn(response);

        CajaResponse resultado = cajaConsultaController.consultarInformacion("caja-1");

        assertNotNull(resultado);
        assertEquals("caja-1", resultado.idCaja());
        verify(cajaConsultaService).consultarInformacionCaja("caja-1");
    }

    @Test
    void consultarPorQr_DebeRetornarCaja() {
        when(cajaConsultaService.consultarPorQr("QR123")).thenReturn(response);

        CajaResponse resultado = cajaConsultaController.consultarPorQr("QR123");

        assertNotNull(resultado);
        assertEquals("QR123", resultado.identificadorQR());
        verify(cajaConsultaService).consultarPorQr("QR123");
    }
}
