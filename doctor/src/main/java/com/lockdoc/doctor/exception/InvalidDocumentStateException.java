package com.lockdoc.doctor.exception;

/**
 * An operation refused because the record is in the wrong state for it —
 * verifying a doctor who is already verified, accepting an invitation that has
 * been withdrawn, approving a consultation rate twice.
 *
 * The pharmacy module has a class of the same name for the same semantics. They
 * are deliberately not shared: a module owning its own domain exceptions is the
 * pattern this codebase already follows, and the alternative would be the doctor
 * module depending on pharmacy for an exception type, which is a coupling
 * neither module wants. If a third module needs it, that is the moment to
 * promote one copy into {@code common} rather than add a third.
 */
public class InvalidDocumentStateException extends RuntimeException {

    public InvalidDocumentStateException(String message) {
        super(message);
    }
}
