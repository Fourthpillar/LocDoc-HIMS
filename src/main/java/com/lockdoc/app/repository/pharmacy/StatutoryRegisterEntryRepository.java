package com.lockdoc.app.repository.pharmacy;

import com.lockdoc.app.entity.pharmacy.StatutoryRegisterEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StatutoryRegisterEntryRepository extends JpaRepository<StatutoryRegisterEntry, Long> {

    List<StatutoryRegisterEntry> findByFacilityIdAndRegisterTypeAndSaleDateBetweenOrderBySaleDateAsc(
            Long facilityId, String registerType, LocalDate from, LocalDate to);
}
