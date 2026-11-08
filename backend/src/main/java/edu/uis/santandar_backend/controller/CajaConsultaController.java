package edu.uis.santandar_backend.controller;

import java.util.List;

import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.service.CajaConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/cajas")
@RequiredArgsConstructor
public class CajaConsultaController {

    private final CajaConsultaService cajaConsultaService;

    /** Sin parámetro devuelve el inventario completo; con ?criterio=texto filtra. */
    @GetMapping
    public List<CajaResponse> listar(@RequestParam(required = false) String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return cajaConsultaService.visualizarInventario();
        }
        return cajaConsultaService.filtrarRecursos(criterio);
    }

    @GetMapping("/{idCaja}")
    public CajaResponse consultarInformacion(@PathVariable String idCaja) {
        return cajaConsultaService.consultarInformacionCaja(idCaja);
    }

    @GetMapping("/qr/{identificadorQR}")
    public CajaResponse consultarPorQr(@PathVariable String identificadorQR) {
        return cajaConsultaService.consultarPorQr(identificadorQR);
    }
}