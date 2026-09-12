package com.lockdoc.app.dto.pharmacy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One row of the supplier ledger (Master Spec §17.7 screen #32) - a GRN (debit) or a purchase return (credit). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierLedgerEntry {

    private LocalDate date;
    private String docType;
    private String docNo;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal runningBalance;
}
