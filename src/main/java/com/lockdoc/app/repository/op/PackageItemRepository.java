package com.lockdoc.app.repository.op;

import com.lockdoc.app.entity.op.PackageItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageItemRepository extends JpaRepository<PackageItem, Long> {

    List<PackageItem> findByPackageEntityId(Long packageId);
}
