package com.ajouchong.repository;

import com.ajouchong.entity.AgoraLike;
import com.ajouchong.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgoraLikeRepository extends JpaRepository<AgoraLike, Long> {
    Optional<AgoraLike> findByMemberAndAgoraId(Member member, Long agoraId);
    long countByAgoraId(Long agoraId);
}
