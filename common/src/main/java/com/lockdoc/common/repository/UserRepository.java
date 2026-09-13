package com.lockdoc.common.repository;

import com.lockdoc.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Master Spec §17.7 #33a - Manage Users, restricted to manageable (Receptionist) role codes at one facility. */
    List<User> findByFacilityIdAndRoles_RoleCodeInOrderByFullNameAsc(Long facilityId, List<String> roleCodes);

    Optional<User> findByIdAndFacilityId(Long id, Long facilityId);
}
