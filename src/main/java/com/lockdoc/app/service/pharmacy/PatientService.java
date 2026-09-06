package com.lockdoc.app.service.pharmacy;

import com.lockdoc.app.dto.PageResponse;
import com.lockdoc.app.dto.pharmacy.PatientRequest;
import com.lockdoc.app.dto.pharmacy.PatientResponse;
import com.lockdoc.app.entity.pharmacy.Patient;
import com.lockdoc.app.exception.ResourceNotFoundException;
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
    private final DocumentNumberService documentNumberService;

    public PageResponse<PatientResponse> list(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> result = StringUtils.hasText(search)
                ? patientRepository.search(search, pageable)
                : patientRepository.findByActiveTrue(pageable);
        return PageResponse.of(result, PatientResponse::toResponse);
    }

    public PatientResponse get(Long id) {
        return PatientResponse.toResponse(findEntity(id));
    }

    public PatientResponse create(PatientRequest request) {
        String mrn = documentNumberService.next("PATIENT", "PT");
        Patient patient = Patient.builder()
                .mrn(mrn)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
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
        return PatientResponse.toResponse(patientRepository.save(patient));
    }

    public void deactivate(Long id) {
        Patient patient = findEntity(id);
        patient.setActive(false);
        patientRepository.save(patient);
    }

    private Patient findEntity(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }
}
