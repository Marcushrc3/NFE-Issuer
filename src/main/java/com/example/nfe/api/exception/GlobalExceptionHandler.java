package com.example.nfe.api.exception;

import com.example.nfe.api.dto.ErrorResponse;
import com.example.nfe.application.sefaz.SefazNotConfiguredException;
import com.example.nfe.application.service.SefazTransmissionFailedException;
import com.example.nfe.application.service.SigningNotConfiguredException;
import com.example.nfe.infrastructure.xml.validation.NfeXmlValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Translates exceptions into the standardized {@link ErrorResponse}.
 * <p>
 * Extends {@link ResponseEntityExceptionHandler} so framework-level exceptions
 * (e.g. 404, 405) keep their correct HTTP status codes while reusing the same
 * error body shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                "Request validation failed",
                resolvePath(request),
                fieldErrors);

        return new ResponseEntity<>(body, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                "Malformed request body",
                resolvePath(request),
                List.of());

        return new ResponseEntity<>(body, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                OffsetDateTime.now(),
                statusCode.value(),
                reasonPhrase(statusCode),
                resolvePath(request),
                List.of());

        return new ResponseEntity<>(errorResponse, headers, statusCode);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex, WebRequest request) {
        log.error("Unexpected error handling request to {}", resolvePath(request), ex);

        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                resolvePath(request),
                List.of());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        // Domain construction errors (e.g. invalid operation/section combinations)
        // are client errors, not internal errors.
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";

        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                message,
                resolvePath(request),
                List.of());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(SigningNotConfiguredException.class)
    public ResponseEntity<ErrorResponse> handleSigningNotConfigured(SigningNotConfiguredException ex,
                                                                    WebRequest request) {
        log.error("Signing requested but not configured for {}", resolvePath(request), ex);

        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                resolvePath(request),
                List.of());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(SefazNotConfiguredException.class)
    public ResponseEntity<ErrorResponse> handleSefazNotConfigured(SefazNotConfiguredException ex,
                                                                  WebRequest request) {
        log.error("SEFAZ transmission requested but not configured for {}", resolvePath(request), ex);

        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                resolvePath(request),
                List.of());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(SefazTransmissionFailedException.class)
    public ResponseEntity<ErrorResponse> handleSefazTransmissionFailed(SefazTransmissionFailedException ex,
                                                                       WebRequest request) {
        log.error("SEFAZ transmission failed for {}", resolvePath(request));

        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                resolvePath(request),
                List.of());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(NfeXmlValidationException.class)
    public ResponseEntity<ErrorResponse> handleXmlValidation(NfeXmlValidationException ex,
                                                             WebRequest request) {
        log.error("Generated NF-e XML failed XSD validation for {}", resolvePath(request), ex);

        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "NF-e XML validation failed: " + ex.getMessage(),
                resolvePath(request),
                List.of());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String reasonPhrase(HttpStatusCode statusCode) {
        return statusCode instanceof HttpStatus httpStatus
                ? httpStatus.getReasonPhrase()
                : "Request error";
    }

    private String resolvePath(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        return request.getDescription(false);
    }
}
