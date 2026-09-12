package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findByFacilityIdAndActiveTrueOrderByNameAsc(Long facilityId);
    Optional<Organization> findByIdAndFacilityId(Long id, Long facilityId);
}
