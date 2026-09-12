package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.op.RegistrationFeeConfigRequest;
import com.lockdoc.app.dto.op.RegistrationFeeConfigResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.RegistrationFeeConfig;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.op.RegistrationFeeConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Self-service registration fee config (Master Spec §7.2/§15.1) - one row
 * per facility, created on first save (upsert), edited any time after.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationFeeConfigService {

    private final RegistrationFeeConfigRepository configRepository;
    private final FacilityRepository facilityRepository;

    public RegistrationFeeConfigResponse getMine() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return configRepository.findByFacilityId(facilityId)
                .map(RegistrationFeeConfigResponse::toResponse)
                .orElse(null);
    }

    public RegistrationFeeConfigResponse upsert(RegistrationFeeConfigRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        RegistrationFeeConfig config = configRepository.findByFacilityId(facilityId)
                .orElseGet(() -> {
                    Facility facility = facilityRepository.findById(facilityId)
                            .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + facilityId));
                    return RegistrationFeeConfig.builder().facility(facility).build();
                });
        config.setFirstFee(request.getFirstFee());
        config.setReRegistrationFee(request.getReRegistrationFee());
        config.setValidityDays(request.getValidityDays());
        return RegistrationFeeConfigResponse.toResponse(configRepository.save(config));
    }
}
