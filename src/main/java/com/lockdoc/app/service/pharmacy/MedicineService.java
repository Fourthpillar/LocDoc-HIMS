package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.MedicineRequest;
import com.lockdoc.app.dto.pharmacy.MedicineResponse;
import com.lockdoc.app.dto.pharmacy.MedicineStockResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.pharmacy.Medicine;
import com.lockdoc.app.exception.DuplicateResourceException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.pharmacy.MedicineBatchRepository;
import com.lockdoc.app.repository.pharmacy.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final FacilityRepository facilityRepository;

    public PageResponse<MedicineResponse> list(int page, int size, String search) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Medicine> result = StringUtils.hasText(search)
                ? medicineRepository.search(facilityId, search, pageable)
                : medicineRepository.findByFacilityIdAndActiveTrue(facilityId, pageable);
        return PageResponse.of(result, MedicineResponse::toResponse);
    }

    public MedicineResponse get(Long id) {
        return MedicineResponse.toResponse(findEntity(id));
    }

    public Medicine getEntity(Long id) {
        return findEntity(id);
    }

    public MedicineResponse create(MedicineRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        if (medicineRepository.existsByFacilityIdAndCode(facilityId, request.getCode())) {
            throw new DuplicateResourceException("Medicine already exists with code: " + request.getCode());
        }
        Facility facility = facilityRepository.getReferenceById(facilityId);
        Medicine medicine = Medicine.builder()
                .facility(facility)
                .code(request.getCode())
                .name(request.getName())
                .genericName(request.getGenericName())
                .manufacturer(request.getManufacturer())
                .category(request.getCategory())
                .uom(request.getUom())
                .hsnCode(request.getHsnCode())
                .taxPercent(request.getTaxPercent())
                .reorderLevel(request.getReorderLevel())
                .isScheduleDrug(request.getIsScheduleDrug() != null && request.getIsScheduleDrug())
                .drugSchedule(request.getDrugSchedule())
                .highAlert(request.getHighAlert() != null && request.getHighAlert())
                .active(true)
                .build();
        return MedicineResponse.toResponse(medicineRepository.save(medicine));
    }

    public MedicineResponse update(Long id, MedicineRequest request) {
        Medicine medicine = findEntity(id);
        medicine.setName(request.getName());
        medicine.setGenericName(request.getGenericName());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setCategory(request.getCategory());
        medicine.setUom(request.getUom());
        medicine.setHsnCode(request.getHsnCode());
        medicine.setTaxPercent(request.getTaxPercent());
        medicine.setReorderLevel(request.getReorderLevel());
        medicine.setIsScheduleDrug(request.getIsScheduleDrug() != null && request.getIsScheduleDrug());
        medicine.setDrugSchedule(request.getDrugSchedule());
        medicine.setHighAlert(request.getHighAlert() != null && request.getHighAlert());
        return MedicineResponse.toResponse(medicineRepository.save(medicine));
    }

    /** Generic substitution (Master Spec §11.5) - other active medicines sharing this one's generic/molecule name, with stock. */
    public List<MedicineStockResponse> genericAlternatives(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Medicine medicine = findEntity(id);
        if (!StringUtils.hasText(medicine.getGenericName())) {
            return List.of();
        }
        List<Medicine> alternatives = medicineRepository
                .findByFacilityIdAndGenericNameIgnoreCaseAndActiveTrueAndIdNot(facilityId, medicine.getGenericName(), id);
        Map<Long, Integer> stockByMedicine = aggregateStock(facilityId);
        return alternatives.stream()
                .map(m -> MedicineStockResponse.of(m, stockByMedicine.getOrDefault(m.getId(), 0)))
                .toList();
    }

    private Map<Long, Integer> aggregateStock(Long facilityId) {
        Map<Long, Integer> stockByMedicine = new HashMap<>();
        for (Object[] row : medicineBatchRepository.aggregateActiveStockByMedicine(facilityId)) {
            stockByMedicine.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return stockByMedicine;
    }

    public void deactivate(Long id) {
        Medicine medicine = findEntity(id);
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }

    private Medicine findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return medicineRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
    }
}
