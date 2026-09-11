package com.lockdoc.pharmacy.service;

import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.pharmacy.dto.MedicineBatchResponse;
import com.lockdoc.pharmacy.dto.MedicineSalesReportRow;
import com.lockdoc.pharmacy.dto.PurchaseOrderResponse;
import com.lockdoc.pharmacy.dto.PurchaseResponse;
import com.lockdoc.pharmacy.dto.SalesInvoiceResponse;
import com.lockdoc.pharmacy.dto.SalesReturnResponse;
import com.lockdoc.pharmacy.dto.StockDetailReportRow;
import com.lockdoc.pharmacy.dto.StockSummaryResponse;
import com.lockdoc.pharmacy.entity.Medicine;
import com.lockdoc.pharmacy.entity.MedicineBatch;
import com.lockdoc.pharmacy.repository.MedicineBatchRepository;
import com.lockdoc.pharmacy.repository.MedicineRepository;
import com.lockdoc.pharmacy.repository.PurchaseOrderRepository;
import com.lockdoc.pharmacy.repository.PurchaseRepository;
import com.lockdoc.pharmacy.repository.SalesInvoiceItemRepository;
import com.lockdoc.pharmacy.repository.SalesInvoiceRepository;
import com.lockdoc.pharmacy.repository.SalesReturnItemRepository;
import com.lockdoc.pharmacy.repository.SalesReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final String STATUS_POSTED = "POSTED";

    private final InventoryService inventoryService;
    private final MedicineBatchRepository medicineBatchRepository;
    private final MedicineRepository medicineRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesInvoiceItemRepository salesInvoiceItemRepository;
    private final SalesReturnItemRepository salesReturnItemRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesReturnRepository salesReturnRepository;

    public PageResponse<StockSummaryResponse> stockSummary(int page, int size) {
        return inventoryService.getStockSummary(page, size, false);
    }

    public List<MedicineBatchResponse> expiryReport(int withinDays) {
        LocalDate cutoff = LocalDate.now().plusDays(withinDays);
        return medicineBatchRepository.findExpiringBatches(cutoff).stream()
                .map(MedicineBatchResponse::toResponse)
                .toList();
    }

    public List<StockSummaryResponse> lowStockReport() {
        return inventoryService.buildStockSummaries(true);
    }

    public List<SalesInvoiceResponse> salesRegister(LocalDate from, LocalDate to) {
        return salesInvoiceRepository.findBySaleDateBetweenAndStatus(from, to, STATUS_POSTED).stream()
                .map(SalesInvoiceResponse::toResponse)
                .toList();
    }

    public List<PurchaseResponse> purchaseRegister(LocalDate from, LocalDate to) {
        return purchaseRepository.findByPurchaseDateBetweenAndStatus(from, to, STATUS_POSTED).stream()
                .map(PurchaseResponse::toResponse)
                .toList();
    }

    public List<SalesReturnResponse> salesReturnRegister(LocalDate from, LocalDate to) {
        return salesReturnRepository.findByReturnDateBetweenAndStatus(from, to, STATUS_POSTED).stream()
                .map(SalesReturnResponse::toResponse)
                .toList();
    }

    public List<PurchaseOrderResponse> purchaseOrderReport(LocalDate from, LocalDate to) {
        return purchaseOrderRepository.findByOrderDateBetween(from, to).stream()
                .map(PurchaseOrderResponse::toResponse)
                .toList();
    }

    /**
     * GRNs with an outstanding balance, oldest due date first - mirrors the
     * payables-aging shape of the legacy "PharmacyPurchases" report (grand
     * total / paid / due), now possible since Purchase tracks amountPaid/
     * balanceDue (see V7 migration).
     */
    public List<PurchaseResponse> purchaseDuesReport() {
        return purchaseRepository.findByBalanceDueGreaterThanAndStatusOrderByDueDateAsc(BigDecimal.ZERO, STATUS_POSTED).stream()
                .map(PurchaseResponse::toResponse)
                .toList();
    }

    /**
     * Medicine-wise sales for a date range, net of returns in the same
     * range - the item-level equivalent of the legacy "MedicineSalesReport".
     */
    public List<MedicineSalesReportRow> medicineSalesReport(LocalDate from, LocalDate to) {
        Map<Long, Medicine> medicinesById = new HashMap<>();
        medicineRepository.findAll().forEach(m -> medicinesById.put(m.getId(), m));

        Map<Long, BigDecimal[]> returnsByMedicine = new HashMap<>();
        for (Object[] row : salesReturnItemRepository.aggregateReturnsByMedicine(from, to)) {
            Long medicineId = (Long) row[0];
            Long qty = (Long) row[1];
            BigDecimal amount = (BigDecimal) row[2];
            returnsByMedicine.put(medicineId, new BigDecimal[] { BigDecimal.valueOf(qty), amount });
        }

        List<MedicineSalesReportRow> rows = new ArrayList<>();
        for (Object[] row : salesInvoiceItemRepository.aggregateSalesByMedicine(from, to)) {
            Long medicineId = (Long) row[0];
            Long qtySold = (Long) row[1];
            BigDecimal grossAmount = (BigDecimal) row[2];
            BigDecimal discountAmount = (BigDecimal) row[3];
            BigDecimal netAmount = (BigDecimal) row[4];

            Medicine medicine = medicinesById.get(medicineId);
            BigDecimal[] returned = returnsByMedicine.getOrDefault(medicineId, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            int qtyReturned = returned[0].intValue();
            BigDecimal returnedAmount = returned[1];

            rows.add(MedicineSalesReportRow.builder()
                    .medicineId(medicineId)
                    .medicineCode(medicine != null ? medicine.getCode() : null)
                    .medicineName(medicine != null ? medicine.getName() : null)
                    .category(medicine != null ? medicine.getCategory() : null)
                    .manufacturer(medicine != null ? medicine.getManufacturer() : null)
                    .qtySold(qtySold.intValue())
                    .qtyReturned(qtyReturned)
                    .netQty(qtySold.intValue() - qtyReturned)
                    .grossAmount(grossAmount)
                    .discountAmount(discountAmount)
                    .returnedAmount(returnedAmount)
                    .netAmount(netAmount.subtract(returnedAmount))
                    .build());
        }

        rows.sort((a, b) -> {
            String an = a.getMedicineName() != null ? a.getMedicineName() : "";
            String bn = b.getMedicineName() != null ? b.getMedicineName() : "";
            return an.compareToIgnoreCase(bn);
        });
        return rows;
    }

    /**
     * Batch-level stock valuation, flattening medicine + batch + supplier
     * into one row - the equivalent of the legacy "MedicineStockReport",
     * which the existing aggregate stock-summary/low-stock reports don't
     * cover since they carry no rate/MRP/value or category/manufacturer.
     */
    public List<StockDetailReportRow> stockDetailReport() {
        List<MedicineBatch> batches = medicineBatchRepository.findByQuantityOnHandGreaterThanOrderByMedicine_NameAsc(0);
        return batches.stream()
                .map(b -> {
                    Medicine m = b.getMedicine();
                    BigDecimal qty = BigDecimal.valueOf(b.getQuantityOnHand());
                    return StockDetailReportRow.builder()
                            .medicineId(m.getId())
                            .medicineCode(m.getCode())
                            .medicineName(m.getName())
                            .category(m.getCategory())
                            .manufacturer(m.getManufacturer())
                            .batchNo(b.getBatchNo())
                            .expiryDate(b.getExpiryDate())
                            .supplierId(b.getSupplier() != null ? b.getSupplier().getId() : null)
                            .supplierName(b.getSupplier() != null ? b.getSupplier().getName() : null)
                            .quantityOnHand(b.getQuantityOnHand())
                            .purchaseRate(b.getPurchaseRate())
                            .saleRate(b.getSaleRate())
                            .mrp(b.getMrp())
                            .stockValueAtCost(b.getPurchaseRate().multiply(qty))
                            .stockValueAtMrp(b.getMrp().multiply(qty))
                            .build();
                })
                .toList();
    }
}
