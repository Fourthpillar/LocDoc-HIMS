package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.MedicineBatchResponse;
import com.lockdoc.app.dto.pharmacy.StockSummaryResponse;
import com.lockdoc.app.entity.pharmacy.Medicine;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.pharmacy.MedicineBatchRepository;
import com.lockdoc.app.repository.pharmacy.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final StoreResolutionService storeResolutionService;

    public PageResponse<StockSummaryResponse> getStockSummary(int page, int size, boolean lowStockOnly) {
        List<StockSummaryResponse> all = buildStockSummaries(lowStockOnly);

        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        List<StockSummaryResponse> content = all.subList(from, to);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) all.size() / size);

        return PageResponse.<StockSummaryResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(all.size())
                .totalPages(totalPages)
                .build();
    }

    public List<StockSummaryResponse> buildStockSummaries(boolean lowStockOnly) {
        Long facilityId = SecurityUtils.requireFacilityId();

        Map<Long, Integer> stockByMedicine = new HashMap<>();
        for (Object[] row : medicineBatchRepository.aggregateActiveStockByMedicine(facilityId)) {
            Long medicineId = (Long) row[0];
            Long qty = (Long) row[1];
            stockByMedicine.put(medicineId, qty == null ? 0 : qty.intValue());
        }

        List<StockSummaryResponse> summaries = medicineRepository.findByFacilityIdAndActiveTrue(facilityId).stream()
                .map(m -> {
                    int stock = stockByMedicine.getOrDefault(m.getId(), 0);
                    boolean lowStock = stock < m.getReorderLevel();
                    return StockSummaryResponse.builder()
                            .medicineId(m.getId())
                            .medicineCode(m.getCode())
                            .medicineName(m.getName())
                            .uom(m.getUom())
                            .reorderLevel(m.getReorderLevel())
                            .stockOnHand(stock)
                            .lowStock(lowStock)
                            .build();
                })
                .toList();

        return lowStockOnly
                ? summaries.stream().filter(StockSummaryResponse::getLowStock).toList()
                : summaries;
    }

    public List<MedicineBatchResponse> getBatches(Long medicineId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Medicine medicine = medicineRepository.findByIdAndFacilityId(medicineId, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + medicineId));

        Long storeId = storeResolutionService.resolveDefaultStore(facilityId).getId();
        return medicineBatchRepository
                .findByStoreIdAndMedicineIdAndQuantityOnHandGreaterThanOrderByExpiryDateAsc(storeId, medicine.getId(), 0)
                .stream()
                .map(MedicineBatchResponse::toResponse)
                .toList();
    }
}
