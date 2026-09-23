package com.example.nfe.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String message,
        String path,
        List<FieldError> fieldErrors) {

    public record FieldError(String field, String message) {
    }
}
