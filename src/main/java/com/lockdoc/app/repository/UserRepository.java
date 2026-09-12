package com.lockdoc.app.repository;

import com.lockdoc.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Master Spec §17.7 #33a - Manage Users, restricted to Receptionist/Pharmacist role codes at one facility. */
    List<User> findByFacilityIdAndRoles_RoleCodeInOrderByFullNameAsc(Long facilityId, List<String> roleCodes);

    Optional<User> findByIdAndFacilityId(Long id, Long facilityId);
}
