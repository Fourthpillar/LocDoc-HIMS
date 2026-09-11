package com.lockdoc.pharmacy.exception;

public class InvalidDocumentStateException extends RuntimeException {

    public InvalidDocumentStateException(String message) {
        super(message);
    }
}
