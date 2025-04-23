package com.podStream.PodStream.Exception;

import com.podStream.PodStream.Models.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

import jakarta.persistence.EntityNotFoundException;

/**
 * Clase de manejo de excepciones global para controladores REST en la aplicación PodStream.
 * Proporciona respuestas estandarizadas para diferentes tipos de excepciones.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de tipo EntityNotFoundException cuando una entidad no se encuentra.
     *
     * @param ex La excepción EntityNotFoundException lanzada.
     * @return ResponseEntity con un ApiResponse que contiene el mensaje de error y estado NOT_FOUND.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException ex) {
        logger.warn("Entity not found: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones genéricas no capturadas por otros manejadores.
     *
     * @param ex La excepción genérica lanzada.
     * @return ResponseEntity con un ApiResponse que contiene un mensaje genérico y estado INTERNAL_SERVER_ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ApiResponse.error("An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maneja excepciones de autenticación cuando las credenciales son inválidas.
     *
     * @param ex La excepción AuthenticationException lanzada.
     * @return ResponseEntity con un ApiResponse que contiene el mensaje de error y estado UNAUTHORIZED.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error("Authentication failed: " + ex.getMessage()),
                HttpStatus.UNAUTHORIZED);
    }

    /**
     * Maneja excepciones de argumentos inválidos.
     *
     * @param ex La excepción IllegalArgumentException lanzada.
     * @return ResponseEntity con un ApiResponse que contiene el mensaje de error y estado BAD_REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de validación cuando los datos de entrada no cumplen con las reglas de validación.
     *
     * @param ex La excepción MethodArgumentNotValidException lanzada.
     * @return ResponseEntity con un ApiResponse que contiene los errores de validación y estado BAD_REQUEST.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ");
        }
        logger.warn("Validation error: {}", errorMessage.toString());
        return new ResponseEntity<>(ApiResponse.error(errorMessage.toString()), HttpStatus.BAD_REQUEST);
    }
}