package edu.uis.santandar_backend.controller;

import java.util.List;

import edu.uis.santandar_backend.dto.request.ActualizarUsuarioRequest;
import edu.uis.santandar_backend.dto.request.LoginRequest;
import edu.uis.santandar_backend.dto.request.RegistroUsuarioRequest;
import edu.uis.santandar_backend.dto.response.AuthResponse;
import edu.uis.santandar_backend.dto.response.UsuarioResponse;
import edu.uis.santandar_backend.config.JwtService;
import edu.uis.santandar_backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correo(), request.clave())
        );
        var user = usuarioService.loadUserByUsername(request.correo());
        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        return usuarioService.registrarUsuario(request);
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listar() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/usuarios/{idUsuario}")
    public UsuarioResponse consultar(@PathVariable String idUsuario) {
        return usuarioService.consultarUsuario(idUsuario);
    }

    @PutMapping("/usuarios/{idUsuario}")
    public UsuarioResponse actualizar(@PathVariable String idUsuario,
                                      @Valid @RequestBody ActualizarUsuarioRequest request) {
        return usuarioService.actualizarUsuario(idUsuario, request);
    }

    @DeleteMapping("/usuarios/{idUsuario}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String idUsuario) {
        usuarioService.eliminarUsuario(idUsuario);
    }
}
