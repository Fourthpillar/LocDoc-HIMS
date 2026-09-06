package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.SalesInvoiceItemRequest;
import com.lockdoc.app.dto.pharmacy.SalesInvoiceRequest;
import com.lockdoc.app.dto.pharmacy.SalesInvoiceResponse;
import com.lockdoc.app.entity.pharmacy.Medicine;
import com.lockdoc.app.entity.pharmacy.MedicineBatch;
import com.lockdoc.app.entity.pharmacy.Patient;
import com.lockdoc.app.entity.pharmacy.SalesInvoice;
import com.lockdoc.app.entity.pharmacy.SalesInvoiceItem;
import com.lockdoc.app.entity.pharmacy.StockLedgerEntry;
import com.lockdoc.app.exception.InsufficientStockException;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.pharmacy.MedicineBatchRepository;
import com.lockdoc.app.repository.pharmacy.MedicineRepository;
import com.lockdoc.app.repository.pharmacy.PatientRepository;
import com.lockdoc.app.repository.pharmacy.SalesInvoiceRepository;
import com.lockdoc.app.repository.pharmacy.SalesReturnRepository;
import com.lockdoc.app.repository.pharmacy.StockLedgerEntryRepository;
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
    private final PatientRepository patientRepository;
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
        return PageResponse.of(result, SalesInvoiceResponse::toResponse);
    }

    public SalesInvoiceResponse get(Long id) {
        return SalesInvoiceResponse.toResponse(findEntity(id));
    }

    public SalesInvoiceResponse create(SalesInvoiceRequest request, Long userId) {
        Patient patient = null;
        if (request.getPatientId() != null) {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
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
                .patient(patient)
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

        return SalesInvoiceResponse.toResponse(invoice);
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
        return SalesInvoiceResponse.toResponse(salesInvoiceRepository.save(invoice));
    }

    private SalesInvoice findEntity(Long id) {
        return salesInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found with id: " + id));
    }
}
