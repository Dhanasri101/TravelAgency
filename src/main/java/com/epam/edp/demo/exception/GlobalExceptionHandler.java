package com.epam.edp.demo.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Error-response shape is spec-compatible (always carries `message`) with
 * `fieldErrors` as an additive extension for UX.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(body(
                HttpStatus.BAD_REQUEST, "Invalid input provided", fieldErrors));
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleWeakPassword(WeakPasswordException ex) {
        return ResponseEntity.badRequest().body(body(
                HttpStatus.BAD_REQUEST, ex.getMessage(),
                Map.of("password", ex.getMessage())));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(
                HttpStatus.CONFLICT, "Email already exists",
                Map.of("email", "An account with this email already exists")));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCreds(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(
                HttpStatus.UNAUTHORIZED, "Wrong password or email",
                Map.of(
                        "email",    "Invalid email or password",
                        "password", "Invalid email or password"
                )));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> handleLocked(AccountLockedException ex) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("timestamp", Instant.now());
        b.put("status", HttpStatus.LOCKED.value());
        b.put("error", "Account locked");
        b.put("message",
                "Your account is temporarily locked due to multiple failed login attempts. Please try again later.");
        b.put("retryAfterSeconds", ex.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.LOCKED).body(b);
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthenticated(UnauthenticatedException ex) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("timestamp", Instant.now());
        b.put("status", HttpStatus.UNAUTHORIZED.value());
        b.put("error", "Unauthenticated");
        b.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(b);
    }

    private static Map<String, Object> body(HttpStatus status, String message, Map<String, String> fieldErrors) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("timestamp", Instant.now());
        b.put("status", status.value());
        b.put("error", status.getReasonPhrase());
        b.put("message", message);
        b.put("fieldErrors", fieldErrors);
        return b;
    }
}


