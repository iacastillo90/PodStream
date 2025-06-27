package com.podStream.PodStream.Exception;

/**
 * Excepción personalizada para errores relacionados con la actualización de calificaciones de productos.
 */
public class RatingUpdateException extends RuntimeException {
    public RatingUpdateException(String message) {
        super(message);
    }

    public RatingUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}