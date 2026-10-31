package edu.uis.santandar_backend.controller;

import edu.uis.santandar_backend.dto.request.CajaRequest;
import edu.uis.santandar_backend.dto.request.CambioEstadoRequest;
import edu.uis.santandar_backend.dto.response.CajaResponse;
import edu.uis.santandar_backend.service.CajaGestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cajas")
@RequiredArgsConstructor
public class CajaGestionController {

    private final CajaGestionService cajaGestionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CajaResponse registrar(@Valid @RequestBody CajaRequest request) {
        return cajaGestionService.registrarCaja(request);
    }

    @PutMapping("/{idCaja}")
    public CajaResponse actualizar(@PathVariable String idCaja, @Valid @RequestBody CajaRequest request) {
        return cajaGestionService.actualizarCaja(idCaja, request);
    }

    @PatchMapping("/{idCaja}/estado")
    public CajaResponse cambiarEstado(@PathVariable String idCaja, @Valid @RequestBody CambioEstadoRequest request) {
        return cajaGestionService.cambiarEstado(idCaja, request.estado());
    }

    @DeleteMapping("/{idCaja}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String idCaja) {
        cajaGestionService.eliminarCaja(idCaja);
    }
}