package com.lockdoc.pharmacy.exception;

import com.lockdoc.common.exception.ApiErrorResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class PharmacyExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        return ApiErrorResponseFactory.build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidDocumentStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDocumentState(InvalidDocumentStateException ex) {
        return ApiErrorResponseFactory.build(HttpStatus.CONFLICT, ex.getMessage());
    }
}
