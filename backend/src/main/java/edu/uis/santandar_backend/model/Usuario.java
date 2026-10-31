package edu.uis.santandar_backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Usuario {
    //id de usuario unico
    @Id
    protected String id;
    //correo unico
    @Indexed(unique = true)
    protected String correo;

    /** Guarda el hash BCrypt, nunca la contraseña en texto plano. */
    protected String clave;
}
