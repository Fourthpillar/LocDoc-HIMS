package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.CommissionBasisRequest;
import com.lockdoc.outpatient.dto.CommissionBasisResponse;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.CommissionBasis;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.outpatient.repository.CommissionBasisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Self-service (Master Spec §6, §16 step 8) - Hospital/Clinic Admin's own referrer commission terms. */
@Service
@RequiredArgsConstructor
@Transactional
public class CommissionBasisService {

    private final CommissionBasisRepository commissionBasisRepository;
    private final FacilityRepository facilityRepository;

    public List<CommissionBasisResponse> listMine() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return commissionBasisRepository.findByFacilityIdAndActiveTrue(facilityId).stream()
                .map(CommissionBasisResponse::toResponse).toList();
    }

    public CommissionBasisResponse create(CommissionBasisRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + facilityId));
        CommissionBasis entity = CommissionBasis.builder()
                .facility(facility)
                .partyType(request.getPartyType())
                .partyName(request.getPartyName())
                .basis(request.getBasis())
                .value(request.getValue())
                .appliesTo(request.getAppliesTo())
                .active(true)
                .build();
        return CommissionBasisResponse.toResponse(commissionBasisRepository.save(entity));
    }

    public CommissionBasisResponse update(Long id, CommissionBasisRequest request) {
        CommissionBasis entity = findEntity(id);
        entity.setPartyType(request.getPartyType());
        entity.setPartyName(request.getPartyName());
        entity.setBasis(request.getBasis());
        entity.setValue(request.getValue());
        entity.setAppliesTo(request.getAppliesTo());
        return CommissionBasisResponse.toResponse(commissionBasisRepository.save(entity));
    }

    public void deactivate(Long id) {
        CommissionBasis entity = findEntity(id);
        entity.setActive(false);
        commissionBasisRepository.save(entity);
    }

    private CommissionBasis findEntity(Long id) {
        Long facilityId = SecurityUtils.requireFacilityId();
        return commissionBasisRepository.findByIdAndFacilityId(id, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Commission basis not found with id: " + id));
    }
}
