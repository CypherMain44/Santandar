package edu.uis.santandar_backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.uis.santandar_backend.model.CodigoRestablecimiento;

public interface CodigoRestablecimientoRepository extends MongoRepository<CodigoRestablecimiento, String> {}
