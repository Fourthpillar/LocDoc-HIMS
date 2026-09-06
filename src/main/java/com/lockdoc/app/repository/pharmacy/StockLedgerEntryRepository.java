package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.StockLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockLedgerEntryRepository extends JpaRepository<StockLedgerEntry, Long> {

    List<StockLedgerEntry> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
