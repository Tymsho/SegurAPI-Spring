package com.first.api.first_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Le dice a Spring que esta clase maneja los errores de todos los controllers
public class GlobalExceptionHandler {

    // Maneja el error cuando no encontramos un cliente o póliza (Ej: ID 999)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("error", ex.getMessage());
        return new ResponseEntity<>(respuesta, HttpStatus.NOT_FOUND); // Devuelve 404
    }

    // Maneja los errores de validación (@NotBlank, @Email, etc.)[cite: 2]
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidations(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        
        // Recorre todos los campos que fallaron y arma un diccionario { "campo": "mensaje" }
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errores.put(error.getField(), error.getDefaultMessage())
        );
        
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST); // Devuelve 400
    }

    // Maneja IllegalArgumentException para validaciones de negocio o seguridad
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("message", ex.getMessage());
        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }

    // Maneja RuntimeExceptions genéricas o internas del servidor
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("error", "Error interno del servidor. Por favor, intente más tarde.");
        // Log the actual error for debugging
        System.err.println("Runtime Error: " + ex.getMessage());
        ex.printStackTrace();
        return new ResponseEntity<>(respuesta, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // Maneja errores de formato (como un string vacío donde se espera un número)
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("message", "Error en el formato de los datos enviados. Verifique que los campos numéricos o fechas sean válidos.");
        return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);
    }
}