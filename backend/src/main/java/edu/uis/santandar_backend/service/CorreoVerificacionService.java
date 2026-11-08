package edu.uis.santandar_backend.service;

public interface CorreoVerificacionService {

    void enviarCodigoRestablecimiento(String correo, String codigo);
}
