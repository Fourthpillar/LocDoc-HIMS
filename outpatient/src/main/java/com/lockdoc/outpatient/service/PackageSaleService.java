package com.lockdoc.outpatient.service;

import com.lockdoc.common.config.SecurityUtils;
import com.lockdoc.outpatient.dto.PackageConsumeRequest;
import com.lockdoc.outpatient.dto.PackageSaleRequest;
import com.lockdoc.outpatient.dto.PackageSaleResponse;
import com.lockdoc.outpatient.dto.PackageUtilizationResponse;
import com.lockdoc.common.entity.Facility;
import com.lockdoc.outpatient.entity.Bill;
import com.lockdoc.outpatient.entity.PackageItem;
import com.lockdoc.outpatient.entity.PackageSale;
import com.lockdoc.outpatient.entity.PackageUtilization;
import com.lockdoc.outpatient.entity.Patient;
import com.lockdoc.common.exception.InvalidDocumentStateException;
import com.lockdoc.common.exception.ResourceNotFoundException;
import com.lockdoc.common.repository.FacilityRepository;
import com.lockdoc.outpatient.repository.PackageItemRepository;
import com.lockdoc.outpatient.repository.PackageRepository;
import com.lockdoc.outpatient.repository.PackageSaleRepository;
import com.lockdoc.outpatient.repository.PackageUtilizationRepository;
import com.lockdoc.outpatient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Package sale & utilization (Master Spec §7.5, screen #11) - "a package
 * is sold once, its included items are drawn down against
 * PackageUtilization as the patient uses them across one or more
 * visits". Selling routes through {@link BillingService#createBill} like
 * every other OP encounter; consuming a utilization row does not - it
 * was already paid for at sale time (see V41's migration comment).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PackageSaleService {

    private final PackageSaleRepository packageSaleRepository;
    private final PackageUtilizationRepository packageUtilizationRepository;
    private final PackageRepository packageRepository;
    private final PackageItemRepository packageItemRepository;
    private final PatientRepository patientRepository;
    private final FacilityRepository facilityRepository;
    private final BillingService billingService;

    public PackageSaleResponse sell(PackageSaleRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        Long userId = SecurityUtils.currentUserId();

        Patient patient = patientRepository.findByIdAndFacilityId(request.getPatientId(), facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        com.lockdoc.outpatient.entity.Package pkg = packageRepository.findByIdAndFacilityId(request.getPackageId(), facilityId)
                .filter(com.lockdoc.outpatient.entity.Package::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + request.getPackageId()));
        List<PackageItem> packageItems = packageItemRepository.findByPackageEntityId(pkg.getId());
        if (packageItems.isEmpty()) {
            throw new InvalidDocumentStateException("This package has no included items configured - cannot sell it");
        }

        Facility facility = facilityRepository.getReferenceById(facilityId);
        PackageSale sale = PackageSale.builder()
                .facility(facility)
                .patient(patient)
                .packageEntity(pkg)
                .createdByUserId(userId)
                .build();
        sale = packageSaleRepository.save(sale);

        Bill bill = billingService.createBill(facility, patient, Bill.ENCOUNTER_PACKAGE, sale.getId(), pkg.getPrice(), userId);
        sale.setBillId(bill.getId());
        sale = packageSaleRepository.save(sale);

        for (PackageItem item : packageItems) {
            packageUtilizationRepository.save(PackageUtilization.builder()
                    .packageSale(sale)
                    .billableItem(item.getBillableItem())
                    .includedQty(item.getIncludedQty())
                    .usedQty(0)
                    .remainingQty(item.getIncludedQty())
                    .build());
        }

        return toResponse(sale, bill.getBillNo());
    }

    public PackageUtilizationResponse consume(Long utilizationId, PackageConsumeRequest request) {
        Long facilityId = SecurityUtils.requireFacilityId();
        PackageUtilization utilization = packageUtilizationRepository.findByIdAndPackageSaleFacilityId(utilizationId, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Package utilization not found with id: " + utilizationId));
        if (request.getQty() > utilization.getRemainingQty()) {
            throw new InvalidDocumentStateException(
                    "Only " + utilization.getRemainingQty() + " of " + utilization.getBillableItem().getName() + " remain on this package - cannot use " + request.getQty());
        }
        utilization.setUsedQty(utilization.getUsedQty() + request.getQty());
        utilization.setRemainingQty(utilization.getRemainingQty() - request.getQty());
        return PackageUtilizationResponse.toResponse(packageUtilizationRepository.save(utilization));
    }

    public List<PackageSaleResponse> listForPatient(Long patientId) {
        Long facilityId = SecurityUtils.requireFacilityId();
        patientRepository.findByIdAndFacilityId(patientId, facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        return packageSaleRepository.findByPatientIdOrderByCreatedDateDesc(patientId).stream()
                .map(s -> toResponse(s, billingService.getByEncounter(Bill.ENCOUNTER_PACKAGE, s.getId()) != null
                        ? billingService.getByEncounter(Bill.ENCOUNTER_PACKAGE, s.getId()).getBillNo()
                        : null))
                .toList();
    }

    private PackageSaleResponse toResponse(PackageSale sale, String billNo) {
        List<PackageUtilizationResponse> utilization = packageUtilizationRepository.findByPackageSaleId(sale.getId()).stream()
                .map(PackageUtilizationResponse::toResponse).toList();
        return PackageSaleResponse.toResponse(sale, billNo, utilization);
    }
}
