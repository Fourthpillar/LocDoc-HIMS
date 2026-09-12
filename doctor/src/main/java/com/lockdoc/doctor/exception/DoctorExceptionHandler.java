package com.lockdoc.doctor.exception;

import com.lockdoc.common.exception.ApiErrorResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Mirrors PharmacyExceptionHandler: same error-body shape, this module's own exception. */
@RestControllerAdvice
public class DoctorExceptionHandler {

    @ExceptionHandler(InvalidDocumentStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDocumentState(InvalidDocumentStateException ex) {
        return ApiErrorResponseFactory.build(HttpStatus.CONFLICT, ex.getMessage());
    }
}
