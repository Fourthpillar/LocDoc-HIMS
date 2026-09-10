package com.lockdoc.pharmacy.repository;

import com.lockdoc.pharmacy.entity.StockLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockLedgerEntryRepository extends JpaRepository<StockLedgerEntry, Long> {

    List<StockLedgerEntry> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
