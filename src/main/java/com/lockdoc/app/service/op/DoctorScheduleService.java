package com.lockdoc.app.service.op;

import com.lockdoc.app.config.SecurityUtils;
import com.lockdoc.app.dto.op.DoctorScheduleRequest;
import com.lockdoc.app.dto.op.DoctorScheduleResponse;
import com.lockdoc.app.entity.Doctor;
import com.lockdoc.app.entity.Facility;
import com.lockdoc.app.entity.doctor.DoctorFacilityMapping;
import com.lockdoc.app.entity.op.DoctorSchedule;
import com.lockdoc.app.exception.InvalidDocumentStateException;
import com.lockdoc.app.exception.ResourceNotFoundException;
import com.lockdoc.app.repository.DoctorRepository;
import com.lockdoc.app.repository.FacilityRepository;
import com.lockdoc.app.repository.doctor.DoctorFacilityMappingRepository;
import com.lockdoc.app.repository.op.DoctorScheduleRepository;
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
            doctor = doctorRepository.findByUserId(SecurityUtils.currentUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("No Doctor profile for the current user"));
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
