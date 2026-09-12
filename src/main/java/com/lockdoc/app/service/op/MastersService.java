package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.op.AreaRequest;
import com.lockdoc.app.dto.op.AreaResponse;
import com.lockdoc.app.dto.op.OrganizationRequest;
import com.lockdoc.app.dto.op.OrganizationResponse;
import com.lockdoc.app.dto.op.ProRequest;
import com.lockdoc.app.dto.op.ProResponse;
import com.lockdoc.app.dto.op.ReferralDoctorRequest;
import com.lockdoc.app.dto.op.ReferralDoctorResponse;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.op.Area;
import com.lockdoc.app.entity.op.Organization;
import com.lockdoc.app.entity.op.Pro;
import com.lockdoc.app.entity.op.ReferralDoctor;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.op.AreaRepository;
import com.lockdoc.app.repository.op.OrganizationRepository;
import com.lockdoc.app.repository.op.ProRepository;
import com.lockdoc.app.repository.op.ReferralDoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Masters (Master Spec §7.2, §6, screen #34) - "missing entity, restored
 * here" three times over: ReferralDoctor, PRO, and Organization/TPA all
 * had data-model entries with no table and no screen ever built for
 * them, despite CommissionBasis (§16 step 8) referencing the first two
 * by party_type. One service, since the three are the same shape and
 * the same screen (#34's own "Masters" tab layout).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MastersService {

    private final ReferralDoctorRepository referralDoctorRepository;
    private final ProRepository proRepository;
    private final OrganizationRepository organizationRepository;
    private final AreaRepository areaRepository;
    private final FacilityRepository facilityRepository;

    // ---- Referral Doctors ----

    public List<ReferralDoctorResponse> listReferralDoctors() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return referralDoctorRepository.findByFacilityIdAndActiveTrueOrderByNameAsc(facilityId).stream()
                .map(ReferralDoctorResponse::toResponse).toList();
    }

    public ReferralDoctorResponse createReferralDoctor(ReferralDoctorRequest request) {
        Facility facility = facilityRepository.getReferenceById(SecurityUtils.requireFacilityId());
        ReferralDoctor entity = ReferralDoctor.builder()
                .facility(facility).name(request.getName()).contact(request.getContact())
                .registrationNo(request.getRegistrationNo()).active(true).build();
        return ReferralDoctorResponse.toResponse(referralDoctorRepository.save(entity));
    }

    public ReferralDoctorResponse updateReferralDoctor(Long id, ReferralDoctorRequest request) {
        ReferralDoctor entity = referralDoctorRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Referral doctor not found with id: " + id));
        entity.setName(request.getName());
        entity.setContact(request.getContact());
        entity.setRegistrationNo(request.getRegistrationNo());
        return ReferralDoctorResponse.toResponse(referralDoctorRepository.save(entity));
    }

    public void deactivateReferralDoctor(Long id) {
        ReferralDoctor entity = referralDoctorRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Referral doctor not found with id: " + id));
        entity.setActive(false);
        referralDoctorRepository.save(entity);
    }

    // ---- PRO ----

    public List<ProResponse> listPros() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return proRepository.findByFacilityIdAndActiveTrueOrderByNameAsc(facilityId).stream()
                .map(ProResponse::toResponse).toList();
    }

    public ProResponse createPro(ProRequest request) {
        Facility facility = facilityRepository.getReferenceById(SecurityUtils.requireFacilityId());
        Pro entity = Pro.builder().facility(facility).name(request.getName()).contact(request.getContact()).active(true).build();
        return ProResponse.toResponse(proRepository.save(entity));
    }

    public ProResponse updatePro(Long id, ProRequest request) {
        Pro entity = proRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("PRO not found with id: " + id));
        entity.setName(request.getName());
        entity.setContact(request.getContact());
        return ProResponse.toResponse(proRepository.save(entity));
    }

    public void deactivatePro(Long id) {
        Pro entity = proRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("PRO not found with id: " + id));
        entity.setActive(false);
        proRepository.save(entity);
    }

    // ---- Organization / TPA ----

    public List<OrganizationResponse> listOrganizations() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return organizationRepository.findByFacilityIdAndActiveTrueOrderByNameAsc(facilityId).stream()
                .map(OrganizationResponse::toResponse).toList();
    }

    public OrganizationResponse createOrganization(OrganizationRequest request) {
        Facility facility = facilityRepository.getReferenceById(SecurityUtils.requireFacilityId());
        Organization entity = Organization.builder()
                .facility(facility).name(request.getName()).orgType(request.getOrgType())
                .contractTerms(request.getContractTerms()).creditTerms(request.getCreditTerms())
                .authorisationRefFormat(request.getAuthorisationRefFormat()).active(true).build();
        return OrganizationResponse.toResponse(organizationRepository.save(entity));
    }

    public OrganizationResponse updateOrganization(Long id, OrganizationRequest request) {
        Organization entity = organizationRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
        entity.setName(request.getName());
        entity.setOrgType(request.getOrgType());
        entity.setContractTerms(request.getContractTerms());
        entity.setCreditTerms(request.getCreditTerms());
        entity.setAuthorisationRefFormat(request.getAuthorisationRefFormat());
        return OrganizationResponse.toResponse(organizationRepository.save(entity));
    }

    public void deactivateOrganization(Long id) {
        Organization entity = organizationRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
        entity.setActive(false);
        organizationRepository.save(entity);
    }

    // ---- Area (Country/State/City/Area, V47) ----

    public List<AreaResponse> listAreas() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return areaRepository.findByFacilityIdAndActiveTrueOrderByAreaNameAsc(facilityId).stream()
                .map(AreaResponse::toResponse).toList();
    }

    public AreaResponse createArea(AreaRequest request) {
        Facility facility = facilityRepository.getReferenceById(SecurityUtils.requireFacilityId());
        Area entity = Area.builder()
                .facility(facility).country(request.getCountry()).state(request.getState())
                .city(request.getCity()).areaName(request.getAreaName()).active(true).build();
        return AreaResponse.toResponse(areaRepository.save(entity));
    }

    public AreaResponse updateArea(Long id, AreaRequest request) {
        Area entity = areaRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Area not found with id: " + id));
        entity.setCountry(request.getCountry());
        entity.setState(request.getState());
        entity.setCity(request.getCity());
        entity.setAreaName(request.getAreaName());
        return AreaResponse.toResponse(areaRepository.save(entity));
    }

    public void deactivateArea(Long id) {
        Area entity = areaRepository.findByIdAndFacilityId(id, SecurityUtils.requireFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException("Area not found with id: " + id));
        entity.setActive(false);
        areaRepository.save(entity);
    }
}
