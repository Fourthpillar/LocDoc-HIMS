package com.lockdoc.app.exception;

import lombok.Getter;

import java.util.List;

/**
 * A dispensing-time safety concern (Master Spec §11.5 — allergy, high-
 * alert, duplicate-therapy) that stopped a sale short of completion.
 * Deliberately distinct from every other exception here: it is a
 * soft-stop, not a hard failure — the frontend shows {@code warnings} to
 * the dispensing user and, on confirmation, resubmits the same request
 * with an override reason attached to each flagged line, which lets it
 * proceed (see SalesService). Mandatory prescriber capture is NOT this —
 * that is a hard validation failure (400), since §11.5 calls it
 * mandatory, not an overridable warning.
 */
@Getter
public class SafetyCheckException extends RuntimeException {

    private final List<String> warnings;

    public SafetyCheckException(List<String> warnings) {
        super(String.join("; ", warnings));
        this.warnings = warnings;
    }
}
