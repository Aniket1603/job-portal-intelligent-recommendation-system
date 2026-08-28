package com.jobportal.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Standard API error payload returned for all error responses.
 * Stack traces are NEVER included.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** Indicates success — always {@code false} for error responses. */
    private final boolean success;

    /** Human-readable error message. */
    private final String message;

    /** HTTP status code (numeric). */
    private final int status;

    /** ISO-8601 timestamp of when the error occurred. */
    private final Instant timestamp;

    /** Request path that triggered the error (when available). */
    private final String path;

    /**
     * Field-level validation errors.
     * Key = field name, Value = validation message.
     * Only present for {@code MethodArgumentNotValidException}.
     */
    private final Map<String, String> fieldErrors;
}
