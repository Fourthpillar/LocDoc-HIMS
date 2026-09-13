package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.DoctorScheduleRequest;
import com.lockdoc.outpatient.dto.DoctorScheduleResponse;
import com.lockdoc.common.entity.Doctor;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.common.entity.DoctorFacilityMapping;
import com.lockdoc.outpatient.entity.DoctorSchedule;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.DoctorRepository;
import com.lockdoc.common.service.CurrentDoctorService;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.common.repository.DoctorFacilityMappingRepository;
import com.lockdoc.outpatient.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Minimal recurring-session backbone (Master Spec §8.2/§6) - not the
 * Availability Planner. Two callers: Hospital/Clinic Admin, creating for
 * any doctor already ACCEPTED at their facility, and the doctor
 * themselves, self-scheduling at any facility they're ACCEPTED-mapped to
 * (their JWT carries no facilityId, §4, and they may practise at several,
 * so - unlike the Admin path - {@link DoctorScheduleRequest#getFacilityId()}
 * is how a doctor's own request says which one). Appointment booking
 * (build order step 5) reads the result for capacity either way.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentDoctorService currentDoctorService;
    private final DoctorFacilityMappingRepository mappingRepository;
    private final FacilityRepository facilityRepository;

    public List<DoctorScheduleResponse> listForFacility() {
        Long facilityId = SecurityUtils.requireFacilityId();
        return scheduleRepository.findByFacilityIdAndActiveTrue(facilityId).stream()
                .map(DoctorScheduleResponse::toResponse)
                .toList();
    }

    public DoctorScheduleResponse create(DoctorScheduleRequest request) {
        boolean callerIsDoctor = SecurityUtils.currentPrincipal().isDoctor();

        Long facilityId;
        Doctor doctor;
        if (callerIsDoctor) {
            // Self-service: ignore request.doctorId entirely rather than trust it - a doctor
            // can only ever create their own schedule, full stop, no matter what's sent.
            doctor = currentDoctorService.require();
            if (request.getFacilityId() == null) {
                throw new InvalidDocumentStateException("facilityId is required when scheduling your own sessions");
            }
            facilityId = request.getFacilityId();
        } else {
            // Admin path, unchanged: always their own token-scoped facility, never the request body.
            facilityId = SecurityUtils.requireFacilityId();
            doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));
        }

        boolean mapped = !mappingRepository
                .findByDoctorIdAndFacilityIdAndStatusIn(doctor.getId(), facilityId, List.of(DoctorFacilityMapping.STATUS_ACCEPTED))
                .isEmpty();
        if (!mapped) {
            throw new InvalidDocumentStateException("Doctor is not currently mapped to this facility - cannot create a schedule");
        }

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found with id: " + facilityId));

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .facility(facility)
                .weekday(request.getWeekday())
                .sessionName(request.getSessionName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .capacity(request.getCapacity())
                .overbookAllowance(request.getOverbookAllowance() != null ? request.getOverbookAllowance() : 0)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .build();
        return DoctorScheduleResponse.toResponse(scheduleRepository.save(schedule));
    }
}
