package com.sptv.mavenproject2.service;

/**
 * Excepción de validación y reglas de negocio con mensajes claros para el usuario.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
