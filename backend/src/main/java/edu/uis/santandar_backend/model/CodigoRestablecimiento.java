package edu.uis.santandar_backend.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "codigos_restablecimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoRestablecimiento {

    @Id
    private String correo;

    private String codigoHash;

    private Instant solicitadoEn;

    @Indexed(expireAfter = "0s")
    private Instant expiraEn;

    private int intentos;
}
