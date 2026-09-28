package com.vigilplus.sigma.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Utilidades de seguridad: hash de contrasenas y generacion de codigos QR. */
public final class Seguridad {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private Seguridad() { }

    /** Hash SHA-256 en hexadecimal (compatible con la funcion SHA2(x, 256) de MySQL). */
    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /** Genera un codigo aleatorio no predecible para una credencial QR temporal. */
    public static String generarCodigoQr() {
        StringBuilder sb = new StringBuilder("QR-V-");
        for (int i = 0; i < 8; i++) {
            sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
