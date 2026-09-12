package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.*;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.Store;
import com.lockdoc.app.entity.pharmacy.Indent;
import com.lockdoc.app.entity.pharmacy.IndentLine;
import com.lockdoc.app.entity.pharmacy.Medicine;
import com.lockdoc.app.entity.pharmacy.MedicineBatch;
import com.lockdoc.app.entity.pharmacy.StockLedgerEntry;
import com.lockdoc.app.exception.InsufficientStockException;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.StoreRepository;
import com.lockdoc.app.repository.pharmacy.IndentRepository;
import com.lockdoc.app.repository.pharmacy.MedicineBatchRepository;
import com.lockdoc.app.repository.pharmacy.MedicineRepository;
import com.lockdoc.app.repository.pharmacy.StockLedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Indent (Master Spec §6/§11.3) - the step before a Purchase Order this
 * document's earlier revisions underspecified. DRAFT -> SUBMITTED ->
 * APPROVED -> PARTIALLY_ISSUED/ISSUED, or REJECTED off SUBMITTED.
 * Issuing actually moves stock FEFO (first-expiry-first-out) from the
 * source store's batches into matching batches at the destination store,
 * with a stock ledger entry on both sides - the real multi-store
 * movement this whole feature exists to make possible.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class IndentService {

    private static final String DOC_TYPE = "INDENT";
    private static final String PREFIX = "IND";
    private static final String TXN_INDENT_ISSUE = "INDENT_ISSUE";
    private static final String TXN_INDENT_RECEIPT = "INDENT_RECEIPT";

    private final IndentRepository indentRepository;
    private final StoreRepository storeRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final DocumentNumberService documentNumberService;

    public PageResponse<IndentResponse> list(int page, int size, String status) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Indent> result = StringUtils.hasText(status)
                ? indentRepository.findByFacilityIdAndStatus(facilityId, status, pageable)
                : indentRepository.findByFacilityId(facilityId, pageable);
        return PageResponse.of(result, IndentResponse::toResponse);
    }

    public IndentResponse get(Long id) {
        return IndentResponse.toResponse(findEntity(id));
    }

    public IndentResponse create(IndentRequest request, Long userId) {
        Long facilityId = SecurityUtils.requireFacilityId();

        Store fromStore = storeRepository.findById(request.getFromStoreId())
                .filter(s -> s.getFacility().getId().equals(facilityId))
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + request.getFromStoreId()));
        Store toStore = storeRepository.findById(request.getToStoreId())
                .filter(s -> s.getFacility().getId().equals(facilityId))
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + request.getToStoreId()));
        if (fromStore.getId().equals(toStore.getId())) {
            throw new InvalidDocumentStateException("fromStoreId and toStoreId cannot be the same store");
        }
        Facility facility = fromStore.getFacility();

        Indent indent = Indent.builder()
                .facility(facility)
                .indentNo(documentNumberService.next(facilityId, DOC_TYPE, PREFIX))
                .fromStore(fromStore)
                .toStore(toStore)
                .status(Indent.STATUS_DRAFT)
                .remarks(request.getRemarks())
                .requestedByUserId(userId)
                .build();

        for (IndentLineRequest lineRequest : request.getLines()) {
            Medicine medicine = medicineRepository.findByIdAndFacilityId(lineRequest.getMedicineId(), facilityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + lineRequest.getMedicineId()));
            indent.getLines().add(IndentLine.builder().indent(indent).medicine(medicine).requestedQty(lineRequest.getRequestedQty()).build());
        }

        return IndentResponse.toResponse(indentRepository.save(indent));
    }

    public IndentResponse submit(Long id) {
        Indent indent = findEntity(id);
        requireStatus(indent, Indent.STATUS_DRAFT, "submitted");
        indent.setStatus(Indent.STATUS_SUBMITTED);
        return IndentResponse.toResponse(indentRepository.save(indent));
    }

    /** Hospital/Clinic Admin approves, optionally adjusting each line's approved quantity down from what was requested. */
    public IndentResponse approve(Long id, IndentLineQtyListRequest request, Long userId) {
        Indent indent = findEntity(id);
        requireStatus(indent, Indent.STATUS_SUBMITTED, "approved");

        Map<Long, Integer> approvedByLine = toLineMap(request);
        for (IndentLine line : indent.getLines()) {
            line.setApprovedQty(approvedByLine.getOrDefault(line.getId(), line.getRequestedQty()));
        }

        indent.setStatus(Indent.STATUS_APPROVED);
        indent.setApprovedByUserId(userId);
        indent.setApprovedDate(LocalDateTime.now());
        return IndentResponse.toResponse(indentRepository.save(indent));
    }

    public IndentResponse reject(Long id) {
        Indent indent = findEntity(id);
        requireStatus(indent, Indent.STATUS_SUBMITTED, "rejected");
        indent.setStatus(Indent.STATUS_REJECTED);
        return IndentResponse.toResponse(indentRepository.save(indent));
    }

    /**
     * Issues stock FEFO from the source store into the destination store,
     * batch by batch, for whatever quantity each line specifies (capped
     * at that line's remaining approved-minus-already-issued amount).
     * Every line is validated for available stock BEFORE any batch is
     * touched, so a mid-way shortfall never leaves a partial movement.
     */
    public IndentResponse issue(Long id, IndentLineQtyListRequest request, Long userId) {
        Indent indent = findEntity(id);
        if (!Indent.STATUS_APPROVED.equals(indent.getStatus()) && !Indent.STATUS_PARTIALLY_ISSUED.equals(indent.getStatus())) {
            throw new InvalidDocumentStateException(
                    "Indent can only be issued from APPROVED or PARTIALLY_ISSUED status, current status: " + indent.getStatus());
        }

        Map<Long, Integer> issueQtyByLine = toLineMap(request);
        Map<Long, List<MedicineBatch>> plannedDeductions = new HashMap<>();

        // Pass 1: validate every line has enough FEFO stock available before moving anything.
        for (IndentLine line : indent.getLines()) {
            int qtyToIssue = issueQtyByLine.getOrDefault(line.getId(), 0);
            if (qtyToIssue <= 0) continue;

            int remaining = (line.getApprovedQty() != null ? line.getApprovedQty() : line.getRequestedQty()) - line.getIssuedQty();
            if (qtyToIssue > remaining) {
                throw new InvalidDocumentStateException(
                        "Cannot issue " + qtyToIssue + " of " + line.getMedicine().getName() + " - only " + remaining + " remains approved");
            }

            List<MedicineBatch> available = medicineBatchRepository.findByStoreIdAndMedicineIdAndQuantityOnHandGreaterThanOrderByExpiryDateAsc(
                    indent.getFromStore().getId(), line.getMedicine().getId(), 0);
            int totalAvailable = available.stream().mapToInt(MedicineBatch::getQuantityOnHand).sum();
            if (totalAvailable < qtyToIssue) {
                throw new InsufficientStockException(
                        "Insufficient stock at " + indent.getFromStore().getName() + " for " + line.getMedicine().getName()
                                + " - available: " + totalAvailable + ", requested: " + qtyToIssue);
            }
            plannedDeductions.put(line.getId(), available);
        }

        // Pass 2: actually move stock, FEFO, batch by batch.
        for (IndentLine line : indent.getLines()) {
            int qtyToIssue = issueQtyByLine.getOrDefault(line.getId(), 0);
            if (qtyToIssue <= 0) continue;

            int remainingToIssue = qtyToIssue;
            for (MedicineBatch sourceBatch : plannedDeductions.get(line.getId())) {
                if (remainingToIssue <= 0) break;
                int fromThisBatch = Math.min(remainingToIssue, sourceBatch.getQuantityOnHand());

                sourceBatch.setQuantityOnHand(sourceBatch.getQuantityOnHand() - fromThisBatch);
                medicineBatchRepository.save(sourceBatch);
                stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                        .facility(indent.getFacility()).store(indent.getFromStore()).medicine(line.getMedicine()).medicineBatch(sourceBatch)
                        .txnType(TXN_INDENT_ISSUE).qtyIn(0).qtyOut(fromThisBatch).balanceQty(sourceBatch.getQuantityOnHand())
                        .referenceType("INDENT").referenceId(indent.getId()).referenceNumber(indent.getIndentNo()).txnDate(LocalDateTime.now())
                        .build());

                MedicineBatch destBatch = medicineBatchRepository.findByStoreIdAndMedicineIdAndBatchNo(
                                indent.getToStore().getId(), line.getMedicine().getId(), sourceBatch.getBatchNo())
                        .orElseGet(() -> MedicineBatch.builder()
                                .facility(indent.getFacility()).store(indent.getToStore()).medicine(line.getMedicine())
                                .batchNo(sourceBatch.getBatchNo()).expiryDate(sourceBatch.getExpiryDate()).mrp(sourceBatch.getMrp())
                                .purchaseRate(sourceBatch.getPurchaseRate()).saleRate(sourceBatch.getSaleRate())
                                .quantityOnHand(0).supplier(sourceBatch.getSupplier()).build());
                destBatch.setQuantityOnHand(destBatch.getQuantityOnHand() + fromThisBatch);
                destBatch = medicineBatchRepository.save(destBatch);
                stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                        .facility(indent.getFacility()).store(indent.getToStore()).medicine(line.getMedicine()).medicineBatch(destBatch)
                        .txnType(TXN_INDENT_RECEIPT).qtyIn(fromThisBatch).qtyOut(0).balanceQty(destBatch.getQuantityOnHand())
                        .referenceType("INDENT").referenceId(indent.getId()).referenceNumber(indent.getIndentNo()).txnDate(LocalDateTime.now())
                        .build());

                remainingToIssue -= fromThisBatch;
            }

            line.setIssuedQty(line.getIssuedQty() + qtyToIssue);
        }

        boolean allFullyIssued = indent.getLines().stream()
                .allMatch(l -> l.getIssuedQty() >= (l.getApprovedQty() != null ? l.getApprovedQty() : l.getRequestedQty()));
        indent.setStatus(allFullyIssued ? Indent.STATUS_ISSUED : Indent.STATUS_PARTIALLY_ISSUED);

        return IndentResponse.toResponse(indentRepository.save(indent));
    }

    private Map<Long, Integer> toLineMap(IndentLineQtyListRequest request) {
        Map<Long, Integer> map = new HashMap<>();
        for (IndentLineQtyRequest line : request.getLines()) {
            map.put(line.getLineId(), line.getQty());
        }
        return map;
    }

    private void requireStatus(Indent indent, String required, String action) {
        if (!required.equals(indent.getStatus())) {
            throw new InvalidDocumentStateException(
                    "Indent can only be " + action + " from " + required + " status, current status: " + indent.getStatus());
        }
    }

    private Indent findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return indentRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Indent not found with id: " + id));
    }
}
