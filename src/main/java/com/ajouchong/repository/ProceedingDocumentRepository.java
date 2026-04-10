package com.ajouchong.repository;

import com.ajouchong.entity.ProceedingDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProceedingDocumentRepository extends JpaRepository<ProceedingDocument, Long> {
    List<ProceedingDocument> findByCategoryIdAndActiveTrueOrderByDisplayOrderAscIdAsc(Long categoryId);
    List<ProceedingDocument> findByCategoryIdOrderByDisplayOrderAscIdAsc(Long categoryId);
    List<ProceedingDocument> findAllByOrderByDisplayOrderAscIdAsc();
}

