package edu.uis.santandar_backend.repository;

import java.util.Optional;

import edu.uis.santandar_backend.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}