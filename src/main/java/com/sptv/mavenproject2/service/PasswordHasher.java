package com.sptv.mavenproject2.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad criptográfica compatible con el algoritmo SHA-256 original de ServiSerc.
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String generarHash(String input) {
        if (input == null) {
            throw new IllegalArgumentException("El texto de entrada para el hash no puede ser nulo.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al inicializar SHA-256", e);
        }
    }

    public static String sha256(String input) {
        return generarHash(input);
    }

    public static boolean matches(String rawPassword, String storedHashOrPlain) {
        if (rawPassword == null || storedHashOrPlain == null) {
            return false;
        }
        String hash = generarHash(rawPassword);
        return hash.equalsIgnoreCase(storedHashOrPlain.trim()) || rawPassword.equals(storedHashOrPlain);
    }
}
