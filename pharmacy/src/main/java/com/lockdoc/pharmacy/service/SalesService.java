package com.lockdoc.pharmacy.service;

import com.lockdoc.common.service.DocumentNumberService;
import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.pharmacy.dto.SalesInvoiceItemRequest;
import com.lockdoc.pharmacy.dto.SalesInvoiceRequest;
import com.lockdoc.pharmacy.dto.SalesInvoiceResponse;
import com.lockdoc.pharmacy.entity.Medicine;
import com.lockdoc.pharmacy.entity.MedicineBatch;
import com.lockdoc.pharmacy.entity.SalesInvoice;
import com.lockdoc.pharmacy.entity.SalesInvoiceItem;
import com.lockdoc.pharmacy.entity.StockLedgerEntry;
import com.lockdoc.pharmacy.exception.InsufficientStockException;
import com.lockdoc.pharmacy.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.pharmacy.repository.MedicineBatchRepository;
import com.lockdoc.pharmacy.repository.MedicineRepository;
import com.lockdoc.pharmacy.repository.SalesInvoiceRepository;
import com.lockdoc.pharmacy.repository.SalesReturnRepository;
import com.lockdoc.pharmacy.repository.StockLedgerEntryRepository;
import com.lockdoc.outpatient.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesService {

    private static final String DOC_TYPE = "SALE";
    private static final String PREFIX = "INV";

    private static final String STATUS_POSTED = "POSTED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final String TXN_TYPE_SALE = "SALE";
    private static final String REFERENCE_TYPE_SALE = "SALE";

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PatientService patientService;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final SalesReturnRepository salesReturnRepository;
    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final DocumentNumberService documentNumberService;

    public PageResponse<SalesInvoiceResponse> list(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SalesInvoice> result = StringUtils.hasText(search)
                ? salesInvoiceRepository.search(search, pageable)
                : salesInvoiceRepository.findAll(pageable);
        return PageResponse.of(result, s -> enrichPatientName(SalesInvoiceResponse.toResponse(s)));
    }

    public SalesInvoiceResponse get(Long id) {
        return enrichPatientName(SalesInvoiceResponse.toResponse(findEntity(id)));
    }

    public SalesInvoiceResponse create(SalesInvoiceRequest request, Long userId) {
        if (request.getPatientId() != null) {
            // validates the patient exists via the outpatient module's service - no direct entity/repository access across modules
            patientService.get(request.getPatientId());
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        List<SalesInvoiceItem> items = new ArrayList<>();
        for (SalesInvoiceItemRequest itemRequest : request.getItems()) {
            Medicine medicine = medicineRepository.findById(itemRequest.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + itemRequest.getMedicineId()));

            MedicineBatch batch = medicineBatchRepository.findById(itemRequest.getMedicineBatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine batch not found with id: " + itemRequest.getMedicineBatchId()));

            if (batch.getQuantityOnHand() < itemRequest.getQty()) {
                throw new InsufficientStockException(
                        "Insufficient stock for medicine " + medicine.getName() + " batch " + batch.getBatchNo()
                                + " - available: " + batch.getQuantityOnHand() + ", requested: " + itemRequest.getQty());
            }

            BigDecimal taxPercent = itemRequest.getTaxPercent() != null ? itemRequest.getTaxPercent() : BigDecimal.ZERO;
            BigDecimal lineDiscount = itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal lineBase = itemRequest.getRate().multiply(BigDecimal.valueOf(itemRequest.getQty()));
            BigDecimal lineTax = lineBase.multiply(taxPercent).divide(BigDecimal.valueOf(100));
            BigDecimal lineAmount = lineBase.add(lineTax).subtract(lineDiscount);

            subtotal = subtotal.add(lineBase);
            taxAmount = taxAmount.add(lineTax);
            discountAmount = discountAmount.add(lineDiscount);

            batch.setQuantityOnHand(batch.getQuantityOnHand() - itemRequest.getQty());
            medicineBatchRepository.save(batch);

            items.add(SalesInvoiceItem.builder()
                    .medicine(medicine)
                    .medicineBatch(batch)
                    .qty(itemRequest.getQty())
                    .rate(itemRequest.getRate())
                    .taxPercent(taxPercent)
                    .discountAmount(lineDiscount)
                    .amount(lineAmount)
                    .build());
        }

        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
        // Retail pharmacy convention: cash collected is rounded to the nearest
        // rupee; roundOffAmount records that adjustment explicitly instead of
        // letting it vanish - previously balanceDue silently absorbed it because
        // amountPaid defaulted to the unrounded totalAmount.
        BigDecimal payableAmount = totalAmount.setScale(0, RoundingMode.HALF_UP);
        BigDecimal roundOffAmount = payableAmount.subtract(totalAmount);
        BigDecimal amountPaid = request.getAmountPaid() != null ? request.getAmountPaid() : payableAmount;
        BigDecimal balanceDue = payableAmount.subtract(amountPaid);

        SalesInvoice invoice = SalesInvoice.builder()
                .invoiceNumber(documentNumberService.next(DOC_TYPE, PREFIX))
                .patientId(request.getPatientId())
                .walkInCustomerName(request.getWalkInCustomerName())
                .walkInCustomerPhone(request.getWalkInCustomerPhone())
                .saleDate(request.getSaleDate())
                .paymentMode(request.getPaymentMode())
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .amountPaid(amountPaid)
                .balanceDue(balanceDue)
                .roundOffAmount(roundOffAmount)
                .status(STATUS_POSTED)
                .createdBy(userId)
                .items(new ArrayList<>())
                .build();
        for (SalesInvoiceItem item : items) {
            item.setSalesInvoice(invoice);
        }
        invoice.setItems(items);

        invoice = salesInvoiceRepository.save(invoice);

        for (SalesInvoiceItem item : invoice.getItems()) {
            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .medicine(item.getMedicine())
                    .medicineBatch(item.getMedicineBatch())
                    .txnType(TXN_TYPE_SALE)
                    .qtyIn(0)
                    .qtyOut(item.getQty())
                    .balanceQty(item.getMedicineBatch().getQuantityOnHand())
                    .referenceType(REFERENCE_TYPE_SALE)
                    .referenceId(invoice.getId())
                    .referenceNumber(invoice.getInvoiceNumber())
                    .txnDate(LocalDateTime.now())
                    .build());
        }

        return enrichPatientName(SalesInvoiceResponse.toResponse(invoice));
    }

    public SalesInvoiceResponse cancel(Long id) {
        SalesInvoice invoice = findEntity(id);
        if (!STATUS_POSTED.equals(invoice.getStatus())) {
            throw new InvalidDocumentStateException("Sales invoice cannot be cancelled from status: " + invoice.getStatus());
        }
        if (salesReturnRepository.existsBySalesInvoiceId(invoice.getId())) {
            throw new InvalidDocumentStateException("Sales invoice cannot be cancelled - sales returns already exist against it");
        }

        for (SalesInvoiceItem item : invoice.getItems()) {
            MedicineBatch batch = item.getMedicineBatch();
            batch.setQuantityOnHand(batch.getQuantityOnHand() + item.getQty());
            medicineBatchRepository.save(batch);

            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .medicine(item.getMedicine())
                    .medicineBatch(batch)
                    .txnType(TXN_TYPE_SALE)
                    .qtyIn(item.getQty())
                    .qtyOut(0)
                    .balanceQty(batch.getQuantityOnHand())
                    .referenceType(REFERENCE_TYPE_SALE)
                    .referenceId(invoice.getId())
                    .referenceNumber(invoice.getInvoiceNumber())
                    .txnDate(LocalDateTime.now())
                    .build());
        }

        invoice.setStatus(STATUS_CANCELLED);
        return enrichPatientName(SalesInvoiceResponse.toResponse(salesInvoiceRepository.save(invoice)));
    }

    private SalesInvoiceResponse enrichPatientName(SalesInvoiceResponse response) {
        if (response.getPatientId() != null) {
            response.setPatientName(patientService.get(response.getPatientId()).getFullName());
        }
        return response;
    }

    private SalesInvoice findEntity(Long id) {
        return salesInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found with id: " + id));
    }
}
