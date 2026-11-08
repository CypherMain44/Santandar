package edu.uis.santandar_backend.controller;

import edu.uis.santandar_backend.dto.request.CajaRequest;
import edu.uis.santandar_backend.dto.request.CambioEstadoRequest;
import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.model.enums.EstadoCaja;
import edu.uis.santandar_backend.service.CajaGestionService;
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
class CajaGestionControllerTest {

    @Mock
    private CajaGestionService cajaGestionService;

    @InjectMocks
    private CajaGestionController cajaGestionController;

    private CajaRequest request;
    private CajaResponse response;

    @BeforeEach
    void setUp() {
        request = new CajaRequest("Cobijas", "Zona Norte");
        response = new CajaResponse("caja-1", "QR1", "Cobijas", "Zona Norte", EstadoCaja.NO_DESPACHADO);
    }

    @Test
    void registrar_DebeDelegarAlServicio() {
        when(cajaGestionService.registrarCaja(request)).thenReturn(response);

        CajaResponse resultado = cajaGestionController.registrar(request);

        assertNotNull(resultado);
        assertEquals("caja-1", resultado.idCaja());
        verify(cajaGestionService).registrarCaja(request);
    }

    @Test
    void actualizar_DebeDelegarAlServicio() {
        when(cajaGestionService.actualizarCaja("caja-1", request)).thenReturn(response);

        CajaResponse resultado = cajaGestionController.actualizar("caja-1", request);

        assertNotNull(resultado);
        verify(cajaGestionService).actualizarCaja("caja-1", request);
    }

    @Test
    void cambiarEstado_DebeDelegarAlServicio() {
        CambioEstadoRequest cambioReq = new CambioEstadoRequest(EstadoCaja.DESPACHADO);
        when(cajaGestionService.cambiarEstado("caja-1", EstadoCaja.DESPACHADO)).thenReturn(response);

        CajaResponse resultado = cajaGestionController.cambiarEstado("caja-1", cambioReq);

        assertNotNull(resultado);
        verify(cajaGestionService).cambiarEstado("caja-1", EstadoCaja.DESPACHADO);
    }

    @Test
    void eliminar_DebeDelegarAlServicio() {
        cajaGestionController.eliminar("caja-1");

        verify(cajaGestionService).eliminarCaja("caja-1");
    }
}
