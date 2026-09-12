package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.StockCountLineSubmitRequest;
import com.lockdoc.app.dto.pharmacy.StockCountRequest;
import com.lockdoc.app.dto.pharmacy.StockCountResponse;
import com.lockdoc.app.dto.pharmacy.StockCountSubmitRequest;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.Store;
import com.lockdoc.app.entity.pharmacy.MedicineBatch;
import com.lockdoc.app.entity.pharmacy.StockCount;
import com.lockdoc.app.entity.pharmacy.StockCountLine;
import com.lockdoc.app.entity.pharmacy.StockLedgerEntry;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.StoreRepository;
import com.lockdoc.app.repository.pharmacy.MedicineBatchRepository;
import com.lockdoc.app.repository.pharmacy.StockCountRepository;
import com.lockdoc.app.repository.pharmacy.StockLedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Physical stock count (Master Spec §6/§11.4) - blind count sheet,
 * variance report, variance posts to stock only on Hospital/Clinic Admin
 * approval (§5 principle 4's approval discipline). "Blind" is enforced
 * at the response layer (StockCountResponse withholds system_qty/
 * variance until APPROVED), not just left to the frontend to hide.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockCountService {

    private static final String DOC_TYPE = "STOCK_COUNT";
    private static final String PREFIX = "SC";
    // stock_ledger_entries.txn_type is VARCHAR(20) - "STOCK_COUNT_ADJUSTMENT" (22 chars) doesn't fit.
    private static final String TXN_STOCK_COUNT_ADJUSTMENT = "STOCK_ADJUSTMENT";

    private final StockCountRepository stockCountRepository;
    private final StoreRepository storeRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final DocumentNumberService documentNumberService;

    public PageResponse<StockCountResponse> list(int page, int size) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Page<StockCount> result = stockCountRepository.findByFacilityId(facilityId, PageRequest.of(page, size));
        return PageResponse.of(result, StockCountResponse::toResponse);
    }

    public StockCountResponse get(Long id) {
        return StockCountResponse.toResponse(findEntity(id));
    }

    public StockCountResponse create(StockCountRequest request, Long userId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Store store = storeRepository.findById(request.getStoreId())
                .filter(s -> s.getFacility().getId().equals(facilityId))
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + request.getStoreId()));
        Facility facility = store.getFacility();

        StockCount count = StockCount.builder()
                .facility(facility).store(store).countNo(documentNumberService.next(facilityId, DOC_TYPE, PREFIX))
                .status(StockCount.STATUS_DRAFT).countedByUserId(userId).countDate(request.getCountDate())
                .build();

        for (MedicineBatch batch : medicineBatchRepository.findByStoreIdAndQuantityOnHandGreaterThanOrderByMedicine_NameAsc(store.getId(), 0)) {
            count.getLines().add(StockCountLine.builder()
                    .stockCount(count).medicine(batch.getMedicine()).medicineBatch(batch).systemQty(batch.getQuantityOnHand())
                    .build());
        }

        return StockCountResponse.toResponse(stockCountRepository.save(count));
    }

    public StockCountResponse submit(Long id, StockCountSubmitRequest request) {
        StockCount count = findEntity(id);
        if (!StockCount.STATUS_DRAFT.equals(count.getStatus())) {
            throw new InvalidDocumentStateException("Stock count can only be submitted from DRAFT status, current status: " + count.getStatus());
        }

        Map<Long, Integer> countedByLine = new HashMap<>();
        for (StockCountLineSubmitRequest line : request.getLines()) {
            countedByLine.put(line.getLineId(), line.getCountedQty());
        }
        for (StockCountLine line : count.getLines()) {
            if (countedByLine.containsKey(line.getId())) {
                line.setCountedQty(countedByLine.get(line.getId()));
            }
        }

        count.setStatus(StockCount.STATUS_SUBMITTED);
        return StockCountResponse.toResponse(stockCountRepository.save(count));
    }

    /** Posts every line's variance to actual stock - the one moment this stock count changes anything real. */
    public StockCountResponse approve(Long id, Long userId) {
        StockCount count = findEntity(id);
        if (!StockCount.STATUS_SUBMITTED.equals(count.getStatus())) {
            throw new InvalidDocumentStateException("Stock count can only be approved from SUBMITTED status, current status: " + count.getStatus());
        }

        for (StockCountLine line : count.getLines()) {
            if (line.getCountedQty() == null) continue;
            Integer variance = line.getVariance();
            if (variance == null || variance == 0) continue;

            MedicineBatch batch = line.getMedicineBatch();
            batch.setQuantityOnHand(line.getCountedQty());
            medicineBatchRepository.save(batch);

            stockLedgerEntryRepository.save(StockLedgerEntry.builder()
                    .facility(count.getFacility()).store(count.getStore()).medicine(line.getMedicine()).medicineBatch(batch)
                    .txnType(TXN_STOCK_COUNT_ADJUSTMENT)
                    .qtyIn(variance > 0 ? variance : 0)
                    .qtyOut(variance < 0 ? -variance : 0)
                    .balanceQty(batch.getQuantityOnHand())
                    .referenceType("STOCK_COUNT").referenceId(count.getId()).referenceNumber(count.getCountNo())
                    .txnDate(LocalDateTime.now())
                    .build());
        }

        count.setStatus(StockCount.STATUS_APPROVED);
        count.setApprovedByUserId(userId);
        count.setApprovedDate(LocalDateTime.now());
        return StockCountResponse.toResponse(stockCountRepository.save(count));
    }

    private StockCount findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return stockCountRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock count not found with id: " + id));
    }
}
