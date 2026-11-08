package edu.uis.santandar_backend.service.impl;

import java.util.Optional;

import edu.uis.santandar_backend.dto.request.CajaRequest;
import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.exception.RecursoDuplicadoException;
import edu.uis.santandar_backend.exception.RecursoNoEncontradoException;
import edu.uis.santandar_backend.mapper.CajaMapper;
import edu.uis.santandar_backend.model.Caja;
import edu.uis.santandar_backend.model.enums.EstadoCaja;
import edu.uis.santandar_backend.repository.CajaRepository;
import edu.uis.santandar_backend.service.GeneradorQR;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CajaGestionServiceImplTest {

    @Mock
    private CajaRepository cajaRepository;

    @Mock
    private CajaMapper cajaMapper;

    @Mock
    private GeneradorQR generadorQR;

    @InjectMocks
    private CajaGestionServiceImpl cajaGestionService;

    private Caja caja;
    private CajaRequest cajaRequest;
    private CajaResponse cajaResponse;

    @BeforeEach
    void setUp() {
        cajaRequest = new CajaRequest("Alimentos", "Zona B");
        caja = Caja.builder()
                .idCaja("caja-1")
                .identificadorQR("QR-caja-1")
                .contenido("Alimentos")
                .zonaDestino("Zona B")
                .estadoCaja(EstadoCaja.NO_DESPACHADO)
                .build();
        cajaResponse = new CajaResponse("caja-1", "QR-caja-1", "Alimentos", "Zona B", EstadoCaja.NO_DESPACHADO);
    }

    @Test
    void registrarCaja_Exitoso() {
        when(cajaMapper.toEntity(cajaRequest)).thenReturn(caja);
        when(generadorQR.crearEtiqueta(any())).thenReturn("QR-caja-1");
        when(cajaRepository.existsByIdentificadorQR("QR-caja-1")).thenReturn(false);
        when(cajaRepository.save(any(Caja.class))).thenReturn(caja);
        when(cajaMapper.toResponse(caja)).thenReturn(cajaResponse);

        CajaResponse resultado = cajaGestionService.registrarCaja(cajaRequest);

        assertNotNull(resultado);
        assertEquals("caja-1", resultado.idCaja());
        verify(cajaRepository).save(any(Caja.class));
    }

    @Test
    void registrarCaja_CuandoQrExiste_LanzaExcepcion() {
        when(cajaMapper.toEntity(cajaRequest)).thenReturn(caja);
        when(generadorQR.crearEtiqueta(any())).thenReturn("QR-caja-1");
        when(cajaRepository.existsByIdentificadorQR("QR-caja-1")).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> cajaGestionService.registrarCaja(cajaRequest));
    }

    @Test
    void actualizarCaja_Exitoso() {
        when(cajaRepository.findById("caja-1")).thenReturn(Optional.of(caja));
        doNothing().when(cajaMapper).actualizar(caja, cajaRequest);
        when(cajaRepository.save(caja)).thenReturn(caja);
        when(cajaMapper.toResponse(caja)).thenReturn(cajaResponse);

        CajaResponse resultado = cajaGestionService.actualizarCaja("caja-1", cajaRequest);

        assertNotNull(resultado);
        verify(cajaMapper).actualizar(caja, cajaRequest);
        verify(cajaRepository).save(caja);
    }

    @Test
    void actualizarCaja_NoExiste_LanzaExcepcion() {
        when(cajaRepository.findById("caja-99")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> cajaGestionService.actualizarCaja("caja-99", cajaRequest));
    }

    @Test
    void eliminarCaja_Exitoso() {
        when(cajaRepository.findById("caja-1")).thenReturn(Optional.of(caja));
        doNothing().when(cajaRepository).delete(caja);

        cajaGestionService.eliminarCaja("caja-1");

        verify(cajaRepository).delete(caja);
    }

    @Test
    void eliminarCaja_NoExiste_LanzaExcepcion() {
        when(cajaRepository.findById("caja-99")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> cajaGestionService.eliminarCaja("caja-99"));
    }

    @Test
    void cambiarEstado_Exitoso() {
        when(cajaRepository.findById("caja-1")).thenReturn(Optional.of(caja));
        when(cajaRepository.save(caja)).thenReturn(caja);
        when(cajaMapper.toResponse(caja)).thenReturn(cajaResponse);

        CajaResponse resultado = cajaGestionService.cambiarEstado("caja-1", EstadoCaja.DESPACHADO);

        assertNotNull(resultado);
        assertEquals(EstadoCaja.DESPACHADO, caja.getEstadoCaja());
        verify(cajaRepository).save(caja);
    }
}
