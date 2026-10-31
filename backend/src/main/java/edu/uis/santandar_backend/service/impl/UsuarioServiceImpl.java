package edu.uis.santandar_backend.service.impl;

import java.util.List;

import edu.uis.santandar_backend.dto.request.ActualizarUsuarioRequest;
import edu.uis.santandar_backend.dto.request.RegistroUsuarioRequest;
import edu.uis.santandar_backend.dto.response.UsuarioResponse;
import edu.uis.santandar_backend.exception.DatosInvalidosException;
import edu.uis.santandar_backend.exception.RecursoDuplicadoException;
import edu.uis.santandar_backend.exception.RecursoNoEncontradoException;
import edu.uis.santandar_backend.mapper.UsuarioMapper;
import edu.uis.santandar_backend.model.Administrador;
import edu.uis.santandar_backend.model.Usuario;
import edu.uis.santandar_backend.repository.UsuarioRepository;
import edu.uis.santandar_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UsuarioResponse registrarUsuario(RegistroUsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new RecursoDuplicadoException("El correo ya está registrado");
        }
        String claveCifrada = passwordEncoder.encode(request.clave());
        Usuario usuario = usuarioMapper.toEntity(request, claveCifrada);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioResponse actualizarUsuario(String idUsuario, ActualizarUsuarioRequest datos) {
        Usuario usuario = buscarUsuario(idUsuario);

        boolean cambiaCorreo = !usuario.getCorreo().equalsIgnoreCase(datos.correo());
        if (cambiaCorreo && usuarioRepository.existsByCorreo(datos.correo())) {
            throw new RecursoDuplicadoException("El correo ya está registrado");
        }
        usuario.setCorreo(datos.correo());

        if (datos.clave() != null && !datos.clave().isBlank()) {
            usuario.setClave(passwordEncoder.encode(datos.clave()));
        }
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    public void eliminarUsuario(String idUsuario) {
        Usuario usuario = buscarUsuario(idUsuario);
        if (usuario instanceof Administrador && contarAdministradores() <= 1) {
            throw new DatosInvalidosException("No se puede eliminar al único administrador del sistema");
        }
        usuarioRepository.delete(usuario);
    }

    @Override
    public UsuarioResponse consultarUsuario(String idUsuario) {
        return usuarioMapper.toResponse(buscarUsuario(idUsuario));
    }

    @Override
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    private Usuario buscarUsuario(String idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario con id " + idUsuario + " no existe"));
    }

    private long contarAdministradores() {
        return usuarioRepository.findAll().stream()
                .filter(Administrador.class::isInstance)
                .count();
    }
}