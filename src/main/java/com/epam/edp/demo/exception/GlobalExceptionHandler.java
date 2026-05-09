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

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String FIELD_EMAIL     = "email";
    private static final String FIELD_PASSWORD  = "password";
    private static final String INVALID_CREDENTIALS_MSG = "Invalid email or password";

    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_STATUS    = "status";
    private static final String KEY_ERROR     = "error";
    private static final String KEY_MESSAGE   = "message";

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
                Map.of(FIELD_PASSWORD, ex.getMessage())));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(
                HttpStatus.CONFLICT, "Email already exists",
                Map.of(FIELD_EMAIL, "An account with this email already exists")));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCreds(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(
                HttpStatus.UNAUTHORIZED, "Wrong password or email",
                Map.of(
                        FIELD_EMAIL,    INVALID_CREDENTIALS_MSG,
                        FIELD_PASSWORD, INVALID_CREDENTIALS_MSG
                )));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> handleLocked(AccountLockedException ex) {
        Map<String, Object> b = simpleBody(HttpStatus.LOCKED, "Account locked",
                "Your account is temporarily locked due to multiple failed login attempts. Please try again later.");
        b.put("retryAfterSeconds", ex.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.LOCKED).body(b);
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthenticated(UnauthenticatedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(simpleBody(HttpStatus.UNAUTHORIZED, "Unauthenticated", ex.getMessage()));
    }

    private static Map<String, Object> simpleBody(HttpStatus status, String error, String message) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put(KEY_TIMESTAMP, Instant.now());
        b.put(KEY_STATUS, status.value());
        b.put(KEY_ERROR, error);
        b.put(KEY_MESSAGE, message);
        return b;
    }

    private static Map<String, Object> body(HttpStatus status, String message, Map<String, String> fieldErrors) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put(KEY_TIMESTAMP, Instant.now());
        b.put(KEY_STATUS, status.value());
        b.put(KEY_ERROR, status.getReasonPhrase());
        b.put(KEY_MESSAGE, message);
        b.put("fieldErrors", fieldErrors);
        return b;
    }
}


