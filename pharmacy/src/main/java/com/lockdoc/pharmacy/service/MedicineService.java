package com.lockdoc.pharmacy.service;

import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.pharmacy.dto.MedicineRequest;
import com.lockdoc.pharmacy.dto.MedicineResponse;
import com.lockdoc.pharmacy.entity.Medicine;
import com.lockdoc.common.exception.DuplicateResourceException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.pharmacy.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public PageResponse<MedicineResponse> list(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Medicine> result = StringUtils.hasText(search)
                ? medicineRepository.search(search, pageable)
                : medicineRepository.findByActiveTrue(pageable);
        return PageResponse.of(result, MedicineResponse::toResponse);
    }

    public MedicineResponse get(Long id) {
        return MedicineResponse.toResponse(findEntity(id));
    }

    public Medicine getEntity(Long id) {
        return findEntity(id);
    }

    public MedicineResponse create(MedicineRequest request) {
        if (medicineRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Medicine already exists with code: " + request.getCode());
        }
        Medicine medicine = Medicine.builder()
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
        return MedicineResponse.toResponse(medicineRepository.save(medicine));
    }

    public void deactivate(Long id) {
        Medicine medicine = findEntity(id);
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }

    private Medicine findEntity(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
    }
}
