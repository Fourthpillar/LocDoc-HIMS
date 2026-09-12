package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.SalesReturnItemRequest;
import com.lockdoc.app.dto.pharmacy.SalesReturnRequest;
import com.lockdoc.app.dto.pharmacy.SalesReturnResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.pharmacy.MedicineBatch;
import com.lockdoc.app.entity.pharmacy.SalesInvoice;
import com.lockdoc.app.entity.pharmacy.SalesInvoiceItem;
import com.lockdoc.app.entity.pharmacy.SalesReturn;
import com.lockdoc.app.entity.pharmacy.SalesReturnItem;
import com.lockdoc.app.entity.pharmacy.StockLedgerEntry;
import com.lockdoc.app.exception.InsufficientStockException;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.pharmacy.MedicineBatchRepository;
import com.lockdoc.app.repository.pharmacy.SalesInvoiceItemRepository;
import com.lockdoc.app.repository.pharmacy.SalesInvoiceRepository;
import com.lockdoc.app.repository.pharmacy.SalesReturnItemRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesReturnService {

    private static final String DOC_TYPE = "SALES_RETURN";
    private static final String PREFIX = "SR";

    private static final String STATUS_POSTED = "POSTED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final String TXN_TYPE_SALES_RETURN = "SALES_RETURN";
    private static final String REFERENCE_TYPE_SALES_RETURN = "SALES_RETURN";

    private final SalesReturnRepository salesReturnRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesInvoiceItemRepository salesInvoiceItemRepository;
    private final SalesReturnItemRepository salesReturnItemRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final DocumentNumberService documentNumberService;

    public PageResponse<SalesReturnResponse> list(int page, int size, String search) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Pageable pageable = PageRequest.of(page, size);
        Page<SalesReturn> result = StringUtils.hasText(search)
                ? salesReturnRepository.search(facilityId, search, pageable)
                : salesReturnRepository.findByFacilityId(facilityId, pageable);
        return PageResponse.of(result, SalesReturnResponse::toResponse);
    }

    public SalesReturnResponse get(Long id) {
        return SalesReturnResponse.toResponse(findEntity(id));
    }

    public SalesReturnResponse create(SalesReturnRequest request, Long userId) {
        Long facilityId = SecurityUtils.requireFacilityId();

        SalesInvoice invoice = salesInvoiceRepository.findByIdAndFacilityId(request.getSalesInvoiceId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales invoice not found with id: " + request.getSalesInvoiceId()));
        if (!STATUS_POSTED.equals(invoice.getStatus())) {
            throw new InvalidDocumentStateException("Cannot return against a sales invoice with status: " + invoice.getStatus());
        }
        // The return's facility is always the parent invoice's - never taken
        // from the request, so a return can never be filed against a
        // different tenant's invoice by mistake.
        Facility facility = invoice.getFacility();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SalesReturnItem> items = new ArrayList<>();

        for (SalesReturnItemRequest itemRequest : request.getItems()) {
            SalesInvoiceItem invoiceItem = salesInvoiceItemRepository.findById(itemRequest.getSalesInvoiceItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sales invoice item not found with id: " + itemRequest.getSalesInvoiceItemId()));

            if (!invoiceItem.getSalesInvoice().getId().equals(invoice.getId())) {
                throw new InvalidDocumentStateException("Sales invoice item " + invoiceItem.getId() + " does not belong to invoice " + invoice.getInvoiceNumber());
            }

            int alreadyReturned = salesReturnItemRepository.sumReturnedQtyForInvoiceItem(invoiceItem.getId());
            int returnable = invoiceItem.getQty() - alreadyReturned;
            if (itemRequest.getQty() > returnable) {
                throw new InvalidDocumentStateException(
                        "Cannot return " + itemRequest.getQty() + " of invoice item " + invoiceItem.getId()
                                + " - only " + returnable + " remaining returnable");
            }

            MedicineBatch batch = invoiceItem.getMedicineBatch();
            batch.setQuantityOnHand(batch.getQuantityOnHand() + itemRequest.getQty());
            medicineBatchRepository.save(batch);

            BigDecimal lineAmount = invoiceItem.getRate().multiply(BigDecimal.valueOf(itemRequest.getQty()));
            totalAmount = totalAmount.add(lineAmount);

            items.add(SalesReturnItem.builder()
                    .salesInvoiceItem(invoiceItem)
                    .medicine(invoiceItem.getMedicine())
                    .medicineBatch(batch)
                    .qty(itemRequest.getQty())
                    .rate(invoiceItem.getRate())
                    .amount(lineAmount)
                    .build());
        }

        SalesReturn salesReturn = SalesReturn.builder()
                .facility(facility)
                .returnNumber(documentNumberService.next(facilityId, DOC_TYPE, PREFIX))
                .salesInvoice(invoice)
                .returnDate(request.getReturnDate())
                .reason(request.getReason())
                .totalAmount(totalAmount)
                .status(STATUS_POSTED)
                .createdBy(userId)
                .items(new ArrayList<>())
                .build();
        for (SalesReturnItem item : items) {
            item.setSalesReturn(salesReturn);
        }
        salesReturn.setItems(items);

        salesReturn = salesReturnRepository.save(salesReturn);

        for (SalesReturnItem item : salesReturn.getItems()) {
            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .facility(facility)
                    .store(invoice.getStore())
                    .medicine(item.getMedicine())
                    .medicineBatch(item.getMedicineBatch())
                    .txnType(TXN_TYPE_SALES_RETURN)
                    .qtyIn(item.getQty())
                    .qtyOut(0)
                    .balanceQty(item.getMedicineBatch().getQuantityOnHand())
                    .referenceType(REFERENCE_TYPE_SALES_RETURN)
                    .referenceId(salesReturn.getId())
                    .referenceNumber(salesReturn.getReturnNumber())
                    .txnDate(LocalDateTime.now())
                    .build());
        }

        return SalesReturnResponse.toResponse(salesReturn);
    }

    public SalesReturnResponse cancel(Long id) {
        SalesReturn salesReturn = findEntity(id);
        if (!STATUS_POSTED.equals(salesReturn.getStatus())) {
            throw new InvalidDocumentStateException("Sales return cannot be cancelled from status: " + salesReturn.getStatus());
        }

        for (SalesReturnItem item : salesReturn.getItems()) {
            MedicineBatch batch = item.getMedicineBatch();
            if (batch.getQuantityOnHand() < item.getQty()) {
                throw new InsufficientStockException(
                        "Cannot cancel sales return " + salesReturn.getReturnNumber() + " - batch " + batch.getBatchNo()
                                + " stock has already been consumed below the returned quantity");
            }
        }

        for (SalesReturnItem item : salesReturn.getItems()) {
            MedicineBatch batch = item.getMedicineBatch();
            batch.setQuantityOnHand(batch.getQuantityOnHand() - item.getQty());
            medicineBatchRepository.save(batch);

            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .facility(salesReturn.getFacility())
                    .store(salesReturn.getSalesInvoice().getStore())
                    .medicine(item.getMedicine())
                    .medicineBatch(batch)
                    .txnType(TXN_TYPE_SALES_RETURN)
                    .qtyIn(0)
                    .qtyOut(item.getQty())
                    .balanceQty(batch.getQuantityOnHand())
                    .referenceType(REFERENCE_TYPE_SALES_RETURN)
                    .referenceId(salesReturn.getId())
                    .referenceNumber(salesReturn.getReturnNumber())
                    .txnDate(LocalDateTime.now())
                    .build());
        }

        salesReturn.setStatus(STATUS_CANCELLED);
        return SalesReturnResponse.toResponse(salesReturnRepository.save(salesReturn));
    }

    private SalesReturn findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return salesReturnRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales return not found with id: " + id));
    }
}
