package edu.uis.santandar_backend.service;

import java.util.List;

import edu.uis.santandar_backend.dto.request.ActualizarUsuarioRequest;
import edu.uis.santandar_backend.dto.request.RegistroUsuarioRequest;
import edu.uis.santandar_backend.dto.response.UsuarioResponse;

public interface UsuarioService {

    UsuarioResponse registrarUsuario(RegistroUsuarioRequest request);

    UsuarioResponse actualizarUsuario(String idUsuario, ActualizarUsuarioRequest datos);

    void eliminarUsuario(String idUsuario);

    UsuarioResponse consultarUsuario(String idUsuario);

    List<UsuarioResponse> listarUsuarios();
}