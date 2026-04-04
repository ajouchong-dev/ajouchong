package com.ajouchong.repository;

import com.ajouchong.entity.PromotionPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionPartnerRepository extends JpaRepository<PromotionPartner, Long> {
    List<PromotionPartner> findAllByOrderByDisplayOrderAscIdAsc();
    List<PromotionPartner> findByActiveTrueOrderByDisplayOrderAscIdAsc();
}

