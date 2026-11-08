package edu.uis.santandar_backend.model;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
@NoArgsConstructor
@SuperBuilder
public class Administrador extends Usuario {
}