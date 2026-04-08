package com.gym.gymtracker.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                   HttpServletRequest request) {
        log.error("Resource not found. path={}, message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        log.error("Validation failed. path={}, message={}", request.getRequestURI(), ex.getMessage());
        List<ApiValidationError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toValidationError)
            .toList();

        return ResponseEntity.badRequest()
            .body(buildError(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), errors));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        log.error("Constraint violation. path={}, message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest()
            .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                                  HttpServletRequest request) {
        log.error("Illegal argument. path={}, message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest()
            .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex,
                                                               HttpServletRequest request) {
        log.error("Illegal state. path={}, message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest()
            .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(BulkWorkoutDemoException.class)
    public ResponseEntity<ApiErrorResponse> handleBulkWorkoutDemo(BulkWorkoutDemoException ex,
                                                                  HttpServletRequest request) {
        log.error("Bulk workout demo failed. path={}, message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected server error. path={}, message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected internal server error",
                request.getRequestURI(),
                List.of()));
    }

    private ApiValidationError toValidationError(FieldError fieldError) {
        return ApiValidationError.builder()
            .field(fieldError.getField())
            .rejectedValue(fieldError.getRejectedValue())
            .message(fieldError.getDefaultMessage())
            .build();
    }

    private ApiErrorResponse buildError(HttpStatus status,
                                        String message,
                                        String path,
                                        List<ApiValidationError> validationErrors) {
        return ApiErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(path)
            .validationErrors(validationErrors)
            .build();
    }
}
