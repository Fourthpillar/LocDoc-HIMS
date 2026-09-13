package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.AreaRequest;
import com.lockdoc.outpatient.dto.AreaResponse;
import com.lockdoc.outpatient.dto.OrganizationRequest;
import com.lockdoc.outpatient.dto.OrganizationResponse;
import com.lockdoc.outpatient.dto.ProRequest;
import com.lockdoc.outpatient.dto.ProResponse;
import com.lockdoc.outpatient.dto.ReferralDoctorRequest;
import com.lockdoc.outpatient.dto.ReferralDoctorResponse;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.Area;
import com.lockdoc.outpatient.entity.Organization;
import com.lockdoc.outpatient.entity.Pro;
import com.lockdoc.outpatient.entity.ReferralDoctor;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.outpatient.repository.AreaRepository;
import com.lockdoc.outpatient.repository.OrganizationRepository;
import com.lockdoc.outpatient.repository.ProRepository;
import com.lockdoc.outpatient.repository.ReferralDoctorRepository;
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
