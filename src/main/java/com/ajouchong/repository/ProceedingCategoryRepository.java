package com.ajouchong.repository;

import com.ajouchong.entity.ProceedingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProceedingCategoryRepository extends JpaRepository<ProceedingCategory, Long> {
    List<ProceedingCategory> findByActiveTrueOrderByDisplayOrderAscIdAsc();
    List<ProceedingCategory> findAllByOrderByDisplayOrderAscIdAsc();
}

