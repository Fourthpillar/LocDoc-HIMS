package com.lockdoc.app.repository;

import com.lockdoc.app.entity.Right;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RightRepository extends JpaRepository<Right, Long> {

    Optional<Right> findByRightCode(String rightCode);
}
