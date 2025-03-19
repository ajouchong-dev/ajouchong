package com.ajouchong.repository;

import com.ajouchong.entity.Member;
import com.ajouchong.entity.PartnershipLke;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnershipLikeRepository extends JpaRepository<PartnershipLke, Long> {
    Optional<PartnershipLke> findByMemberAndPsPostId(Member member, Long psPostId);
    long countByPsPostId(Long psPostId);
}
