package com.jobportal.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralised exception handler for all REST controllers.
 * Stack traces are logged internally but NEVER exposed to API clients.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Domain exceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    // -------------------------------------------------------------------------
    // Validation exceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.merge(
                    fieldError.getField(),
                    fieldError.getDefaultMessage(),
                    (existing, incoming) -> existing + "; " + incoming
            );
        }

        log.warn("Validation failed: {}", fieldErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors);
    }

    // -------------------------------------------------------------------------
    // Catch-all for unexpected errors
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        // Log the full stack trace internally; do NOT include it in the response
        log.error("Unexpected error on [{}] {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request,
                null
        );
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request, Map<String, String> fieldErrors) {

        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .message(message)
                .status(status.value())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
