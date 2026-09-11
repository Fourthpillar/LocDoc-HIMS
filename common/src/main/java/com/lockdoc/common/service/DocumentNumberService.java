package com.lockdoc.common.service;

import com.lockdoc.common.entity.DocumentSequence;
import com.lockdoc.common.repository.DocumentSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Generates document numbers in the form "{prefix}-{year}-{6-digit zero-padded number}",
 * e.g. PO-2026-000001. The backing counter row is select-and-locked
 * (PESSIMISTIC_WRITE) so concurrent callers within the same year/docType
 * never hand out the same number.
 */
@Service
@RequiredArgsConstructor
public class DocumentNumberService {

    private final DocumentSequenceRepository documentSequenceRepository;

    @Transactional
    public String next(String docType, String prefix) {
        int year = LocalDate.now().getYear();

        DocumentSequence sequence = documentSequenceRepository.findForUpdate(docType, year)
                .orElseGet(() -> DocumentSequence.builder()
                        .docType(docType)
                        .year(year)
                        .prefix(prefix)
                        .lastNumber(0)
                        .build());

        sequence.setLastNumber(sequence.getLastNumber() + 1);
        documentSequenceRepository.save(sequence);

        return String.format("%s-%d-%06d", prefix, year, sequence.getLastNumber());
    }
}
