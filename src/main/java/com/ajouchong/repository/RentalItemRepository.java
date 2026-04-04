package com.ajouchong.repository;

import com.ajouchong.entity.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalItemRepository extends JpaRepository<RentalItem, Long> {
    List<RentalItem> findAllByOrderByDisplayOrderAscIdAsc();
    List<RentalItem> findByActiveTrueOrderByDisplayOrderAscIdAsc();
}

