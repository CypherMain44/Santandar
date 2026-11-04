package edu.uis.santandar_backend.mapper;

import edu.uis.santandar_backend.dto.request.CajaRequest;
import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.model.Caja;
import edu.uis.santandar_backend.model.enums.EstadoCaja;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CajaMapperTest {

    private CajaMapper cajaMapper;

    @BeforeEach
    void setUp() {
        cajaMapper = new CajaMapper();
    }

    @Test
    void toEntity_MapeaCorrectamenteSinIds() {
        CajaRequest request = new CajaRequest("Herramientas", "Zona D");

        Caja caja = cajaMapper.toEntity(request);

        assertNotNull(caja);
        assertEquals("Herramientas", caja.getContenido());
        assertEquals("Zona D", caja.getZonaDestino());
        assertNull(caja.getIdCaja());
        assertNull(caja.getIdentificadorQR());
    }

    @Test
    void actualizar_ModificaCamposCorrectamente() {
        Caja caja = Caja.builder()
                .idCaja("caja-1")
                .contenido("Viejo contenido")
                .zonaDestino("Zona A")
                .build();
        CajaRequest request = new CajaRequest("Nuevo contenido", "Zona B");

        cajaMapper.actualizar(caja, request);

        assertEquals("Nuevo contenido", caja.getContenido());
        assertEquals("Zona B", caja.getZonaDestino());
        assertEquals("caja-1", caja.getIdCaja());
    }

    @Test
    void toResponse_MapeaCamposCorrectamente() {
        Caja caja = Caja.builder()
                .idCaja("caja-1")
                .identificadorQR("QR-999")
                .contenido("Medicamentos")
                .zonaDestino("Zona C")
                .estadoCaja(EstadoCaja.NO_DESPACHADO)
                .build();

        CajaResponse response = cajaMapper.toResponse(caja);

        assertNotNull(response);
        assertEquals("caja-1", response.idCaja());
        assertEquals("QR-999", response.identificadorQR());
        assertEquals("Medicamentos", response.contenido());
        assertEquals("Zona C", response.zonaDestino());
        assertEquals(EstadoCaja.NO_DESPACHADO, response.estadoCaja());
    }
}
