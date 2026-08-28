package com.jobportal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Generic API success response wrapper.
 * Used for simple responses that carry a message and optional data payload.
 *
 * @param <T> type of the optional data payload
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    /** Convenience factory — message only (no data). */
    public static ApiResponse<Void> ok(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    /** Convenience factory — message + data. */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
}
