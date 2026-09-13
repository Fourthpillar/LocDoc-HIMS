package com.lockdoc.common.exception;

public class InvalidDocumentStateException extends RuntimeException {

    public InvalidDocumentStateException(String message) {
        super(message);
    }
}
