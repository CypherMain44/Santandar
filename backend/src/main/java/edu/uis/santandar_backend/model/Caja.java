package edu.uis.santandar_backend.model;

import edu.uis.santandar_backend.model.enums.EstadoCaja;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cajas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caja {

    @Id
    private String idCaja;

    @Indexed(unique = true, sparse = true)
    private String identificadorQR;

    private String contenido;

    private String zonaDestino;

    @Builder.Default
    private EstadoCaja estadoCaja = EstadoCaja.NO_DESPACHADO;
}