package com.lockdoc.outpatient.repository;

import com.lockdoc.outpatient.entity.PackageItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageItemRepository extends JpaRepository<PackageItem, Long> {

    List<PackageItem> findByPackageEntityId(Long packageId);
}
