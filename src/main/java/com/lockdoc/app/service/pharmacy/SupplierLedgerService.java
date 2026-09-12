package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.pharmacy.SupplierLedgerEntry;
import com.lockdoc.app.entity.pharmacy.Purchase;
import com.lockdoc.app.entity.pharmacy.PurchaseReturn;
import com.lockdoc.app.repository.pharmacy.PurchaseRepository;
import com.lockdoc.app.repository.pharmacy.PurchaseReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Supplier ledger (Master Spec §17.7 screen #32, build order step 7) -
 * every GRN (debit - what the facility owes) and purchase return
 * (credit - what the supplier owes back), oldest first, with a running
 * balance. Not a new entity - a read-only merge of Purchase and
 * PurchaseReturn, the same two documents the supplier relationship is
 * actually made of.
 */
@Service
@RequiredArgsConstructor
public class SupplierLedgerService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseReturnRepository purchaseReturnRepository;

    public List<SupplierLedgerEntry> forSupplier(Long supplierId) {
        Long facilityId = SecurityUtils.requireFacilityId();

        record RawEntry(LocalDate date, String docType, String docNo, BigDecimal debit, BigDecimal credit) {}
        List<RawEntry> raw = new ArrayList<>();

        for (Purchase p : purchaseRepository.findByFacilityIdAndSupplierIdOrderByPurchaseDateAsc(facilityId, supplierId)) {
            if (!"CANCELLED".equals(p.getStatus())) {
                raw.add(new RawEntry(p.getPurchaseDate(), "GRN", p.getGrnNumber(), p.getTotalAmount(), BigDecimal.ZERO));
            }
        }
        for (PurchaseReturn r : purchaseReturnRepository.findBySupplierForLedger(facilityId, supplierId)) {
            if (PurchaseReturn.STATUS_POSTED.equals(r.getStatus())) {
                raw.add(new RawEntry(r.getCreatedDate().toLocalDate(), "PURCHASE_RETURN", r.getReturnNo(), BigDecimal.ZERO, r.getTotalAmount()));
            }
        }
        raw.sort(Comparator.comparing(RawEntry::date));

        List<SupplierLedgerEntry> entries = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;
        for (RawEntry r : raw) {
            runningBalance = runningBalance.add(r.debit()).subtract(r.credit());
            entries.add(SupplierLedgerEntry.builder()
                    .date(r.date()).docType(r.docType()).docNo(r.docNo()).debit(r.debit()).credit(r.credit())
                    .runningBalance(runningBalance)
                    .build());
        }
        return entries;
    }
}
