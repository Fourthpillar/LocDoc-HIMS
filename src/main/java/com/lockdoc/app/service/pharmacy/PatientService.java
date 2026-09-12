package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.PatientRequest;
import com.lockdoc.app.dto.pharmacy.PatientResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.Area;
import com.lockdoc.app.entity.pharmacy.Patient;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.op.AreaRepository;
import com.lockdoc.app.repository.pharmacy.PatientRepository;
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
public class PatientService {

    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;
    private final AreaRepository areaRepository;
    private final DocumentNumberService documentNumberService;

    public PageResponse<PatientResponse> list(int page, int size, String search) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> result = StringUtils.hasText(search)
                ? patientRepository.search(facilityId, search, pageable)
                : patientRepository.findByFacilityIdAndActiveTrue(facilityId, pageable);
        return PageResponse.of(result, PatientResponse::toResponse);
    }

    public PatientResponse get(Long id) {
        return PatientResponse.toResponse(findEntity(id));
    }

    public PatientResponse create(PatientRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Facility facility = facilityRepository.getReferenceById(facilityId);
        String mrn = documentNumberService.next(facilityId, "PATIENT", "PT");
        Patient patient = Patient.builder()
                .facility(facility)
                .mrn(mrn)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .allergies(request.getAllergies())
                .area(resolveArea(request.getAreaId(), facilityId))
                .active(true)
                .build();
        return PatientResponse.toResponse(patientRepository.save(patient));
    }

    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = findEntity(id);
        patient.setFullName(request.getFullName());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setAddress(request.getAddress());
        patient.setAllergies(request.getAllergies());
        patient.setArea(resolveArea(request.getAreaId(), SecurityUtils.requireFacilityId()));
        return PatientResponse.toResponse(patientRepository.save(patient));
    }

    private Area resolveArea(Long areaId, Long facilityId) {
        if (areaId == null) return null;
        return areaRepository.findByIdAndFacilityId(areaId, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found with id: " + areaId));
    }

    /** Looked up by OpPatientController for registration/booking - same entity, same facility scope. */
    public com.lockdoc.app.entity.pharmacy.Patient findEntityForOp(Long id) {
        return findEntity(id);
    }

    public void deactivate(Long id) {
        Patient patient = findEntity(id);
        patient.setActive(false);
        patientRepository.save(patient);
    }

    private Patient findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return patientRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }
}
