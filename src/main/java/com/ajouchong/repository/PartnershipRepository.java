package com.ajouchong.repository;

import com.ajouchong.entity.Partnership;
import lombok.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnershipRepository extends JpaRepository<Partnership, Long> {
    @NonNull
    List<Partnership> findAll(@NonNull Sort sort);
}
