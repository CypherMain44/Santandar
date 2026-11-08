package edu.uis.santandar_backend.repository;

import java.util.List;
import java.util.Optional;

import edu.uis.santandar_backend.model.Caja;
import edu.uis.santandar_backend.model.enums.EstadoCaja;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CajaRepository extends MongoRepository<Caja, String> {

    Optional<Caja> findByIdentificadorQR(String identificadorQR);

    boolean existsByIdentificadorQR(String identificadorQR);

    List<Caja> findByZonaDestino(String zonaDestino);

    List<Caja> findByEstadoCaja(EstadoCaja estadoCaja);

    @Query("{ $or: [ { contenido: { $regex: ?0, $options: 'i' } }, "
            + "{ zonaDestino: { $regex: ?0, $options: 'i' } }, "
            + "{ identificadorQR: { $regex: ?0, $options: 'i' } } ] }")
    List<Caja> buscarPorCriterio(String criterio);
}
