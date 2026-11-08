package edu.uis.santandar_backend.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.security.SecureRandom;

import edu.uis.santandar_backend.dto.request.ConfirmarRestablecimientoRequest;
import edu.uis.santandar_backend.dto.request.SolicitarRestablecimientoRequest;
import edu.uis.santandar_backend.exception.DatosInvalidosException;
import edu.uis.santandar_backend.model.CodigoRestablecimiento;
import edu.uis.santandar_backend.repository.CodigoRestablecimientoRepository;
import edu.uis.santandar_backend.repository.UsuarioRepository;
import edu.uis.santandar_backend.service.CorreoVerificacionService;
import edu.uis.santandar_backend.service.RestablecimientoClaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestablecimientoClaveServiceImpl implements RestablecimientoClaveService {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration RESEND_DELAY = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final CodigoRestablecimientoRepository codigoRepository;
    private final PasswordEncoder passwordEncoder;
    private final CorreoVerificacionService correoVerificacionService;

    public void solicitar(SolicitarRestablecimientoRequest request) {
        String correo = normalizeEmail(request.correo());
        if (usuarioRepository.findByCorreoIgnoreCase(correo).isEmpty()) {
            return;
        }

        Instant ahora = Instant.now();
        CodigoRestablecimiento existente = codigoRepository.findById(correo).orElse(null);
        if (existente != null && existente.getSolicitadoEn().isAfter(ahora.minus(RESEND_DELAY))) {
            return;
        }

        String codigo = String.format(Locale.ROOT, "%06d", SECURE_RANDOM.nextInt(1_000_000));
        CodigoRestablecimiento registro = CodigoRestablecimiento.builder()
                .correo(correo)
                .codigoHash(passwordEncoder.encode(codigo))
                .solicitadoEn(ahora)
                .expiraEn(ahora.plus(CODE_TTL))
                .intentos(0)
                .build();
        codigoRepository.save(registro);

        try {
            correoVerificacionService.enviarCodigoRestablecimiento(correo, codigo);
        } catch (IllegalStateException ex) {
            codigoRepository.deleteById(correo);
            throw ex;
        }
    }

    public void confirmar(ConfirmarRestablecimientoRequest request) {
        String correo = normalizeEmail(request.correo());
        CodigoRestablecimiento registro = codigoRepository.findById(correo)
                .orElseThrow(this::invalidOrExpiredCode);

        if (registro.getExpiraEn().isBefore(Instant.now()) || registro.getIntentos() >= MAX_ATTEMPTS) {
            codigoRepository.delete(registro);
            throw invalidOrExpiredCode();
        }

        if (!passwordEncoder.matches(request.codigo(), registro.getCodigoHash())) {
            registro.setIntentos(registro.getIntentos() + 1);
            if (registro.getIntentos() >= MAX_ATTEMPTS) {
                codigoRepository.delete(registro);
            } else {
                codigoRepository.save(registro);
            }
            throw invalidOrExpiredCode();
        }

        var usuario = usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null);
        if (usuario == null) {
            codigoRepository.delete(registro);
            throw invalidOrExpiredCode();
        }
        usuario.setClave(passwordEncoder.encode(request.nuevaClave()));
        usuarioRepository.save(usuario);
        codigoRepository.delete(registro);
    }

    private DatosInvalidosException invalidOrExpiredCode() {
        return new DatosInvalidosException("El código es inválido o ha expirado");
    }

    private String normalizeEmail(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
