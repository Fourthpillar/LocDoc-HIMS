package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.SupplierRequest;
import com.lockdoc.app.dto.pharmacy.SupplierResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.pharmacy.Supplier;
import com.lockdoc.app.exception.DuplicateResourceException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.pharmacy.SupplierRepository;
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
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final FacilityRepository facilityRepository;

    public PageResponse<SupplierResponse> list(int page, int size, String search) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Supplier> result = StringUtils.hasText(search)
                ? supplierRepository.search(facilityId, search, pageable)
                : supplierRepository.findByFacilityIdAndActiveTrue(facilityId, pageable);
        return PageResponse.of(result, SupplierResponse::toResponse);
    }

    public SupplierResponse get(Long id) {
        return SupplierResponse.toResponse(findEntity(id));
    }

    public SupplierResponse create(SupplierRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        if (supplierRepository.existsByFacilityIdAndNameIgnoreCase(facilityId, request.getName())) {
            throw new DuplicateResourceException("Supplier already exists with name: " + request.getName());
        }
        Facility facility = facilityRepository.getReferenceById(facilityId);
        Supplier supplier = Supplier.builder()
                .facility(facility)
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .gstin(request.getGstin())
                .mobile2(request.getMobile2())
                .landline(request.getLandline())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .tinNo(request.getTinNo())
                .website(request.getWebsite())
                .supplierType(request.getSupplierType())
                .drugLicenseNo(request.getDrugLicenseNo())
                .active(true)
                .accepted(false)
                .build();
        return SupplierResponse.toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = findEntity(id);
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setGstin(request.getGstin());
        supplier.setMobile2(request.getMobile2());
        supplier.setLandline(request.getLandline());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setPincode(request.getPincode());
        supplier.setTinNo(request.getTinNo());
        supplier.setWebsite(request.getWebsite());
        supplier.setSupplierType(request.getSupplierType());
        supplier.setDrugLicenseNo(request.getDrugLicenseNo());
        return SupplierResponse.toResponse(supplierRepository.save(supplier));
    }

    public void deactivate(Long id) {
        Supplier supplier = findEntity(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    /** Master Spec §12/§31a - Hospital/Clinic Admin's facility-level acceptance, distinct from the PO-approval workflow that comes after it. */
    public SupplierResponse accept(Long id) {
        Supplier supplier = findEntity(id);
        supplier.setAccepted(true);
        return SupplierResponse.toResponse(supplierRepository.save(supplier));
    }

    private Supplier findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return supplierRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }
}
