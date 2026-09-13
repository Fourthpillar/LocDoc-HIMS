package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.common.dto.PageResponse;
import com.lockdoc.outpatient.dto.MlcRegisterResponse;
import com.lockdoc.outpatient.dto.MlcRegisterRow;
import com.lockdoc.common.dto.platform.AuditLogResponse;
import com.lockdoc.outpatient.entity.OpVisit;
import com.lockdoc.common.entity.platform.AuditLogEntry;
import com.lockdoc.outpatient.repository.OpVisitRepository;
import com.lockdoc.common.service.platform.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Facility reports & audit log, incl. MLC register (Master Spec §17.7
 * #38, Hospital/Clinic Admin, own facility only). See V43's migration
 * comment for why this is exactly these two reports, not a broader
 * invented catalogue.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityReportsService {

    private final OpVisitRepository opVisitRepository;
    private final AuditLogService auditLogService;

    public MlcRegisterResponse mlcRegister(LocalDate from, LocalDate to) {
        Long facilityId = SecurityUtils.requireFacilityId();
        var visits = opVisitRepository.findByFacilityIdAndMlcFlagTrueAndArrivedTsBetweenOrderByArrivedTsAsc(
                facilityId, from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        var rows = visits.stream().map(this::toMlcRow).toList();
        return MlcRegisterResponse.builder().rows(rows).totalCount(rows.size()).build();
    }

    public PageResponse<AuditLogResponse> auditLog(int page, int size) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Page<AuditLogEntry> result = auditLogService.searchForFacility(facilityId, page, size);
        return PageResponse.of(result, AuditLogResponse::toResponse);
    }

    private MlcRegisterRow toMlcRow(OpVisit v) {
        return MlcRegisterRow.builder()
                .opNo(v.getOpNo())
                .patientName(v.getPatient().getFullName())
                .patientMrn(v.getPatient().getMrn())
                .doctorName(v.getDoctor().getFullName())
                .mlcPoliceStation(v.getMlcPoliceStation())
                .mlcNumber(v.getMlcNumber())
                .arrivedTs(v.getArrivedTs())
                .build();
    }
}
