package edu.uis.santandar_backend.service.impl;

import java.util.List;

import edu.uis.santandar_backend.dto.TipoUsuario;
import edu.uis.santandar_backend.dto.request.ActualizarUsuarioRequest;
import edu.uis.santandar_backend.dto.request.RegistroUsuarioRequest;
import edu.uis.santandar_backend.dto.response.UsuarioResponse;
import edu.uis.santandar_backend.exception.DatosInvalidosException;
import edu.uis.santandar_backend.exception.RecursoDuplicadoException;
import edu.uis.santandar_backend.exception.RecursoNoEncontradoException;
import edu.uis.santandar_backend.mapper.UsuarioMapper;
import edu.uis.santandar_backend.model.Usuario;
import edu.uis.santandar_backend.model.Voluntario;
import edu.uis.santandar_backend.repository.UsuarioRepository;
import edu.uis.santandar_backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Override
    public UsuarioResponse registrarUsuario(RegistroUsuarioRequest request) {
        if (request.tipo() != TipoUsuario.VOLUNTARIO) {
            throw new DatosInvalidosException("Solo se pueden registrar usuarios voluntarios desde esta sección");
        }
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
        if (!(usuario instanceof Voluntario)) {
            throw new DatosInvalidosException("Solo se pueden eliminar cuentas de voluntarios desde esta sección");
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

}
