package com.ajouchong.repository;

import com.ajouchong.entity.RentalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRecordRepository extends JpaRepository<RentalRecord, Long> {
    List<RentalRecord> findAllByOrderByRentalDateDescCreatedAtDesc();
}

