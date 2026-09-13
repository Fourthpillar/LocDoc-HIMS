package com.lockdoc.common.repository;

import com.lockdoc.common.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    boolean existsByRegistrationNumber(String registrationNumber);

    Page<Doctor> findByVerificationStatus(String verificationStatus, Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.fullName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(d.registrationNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Doctor> search(@Param("search") String search, Pageable pageable);
}
