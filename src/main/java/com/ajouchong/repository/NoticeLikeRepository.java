package com.ajouchong.repository;

import com.ajouchong.entity.Member;
import com.ajouchong.entity.NoticeLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoticeLikeRepository extends JpaRepository<NoticeLike, Long> {
    Optional<NoticeLike> findByMemberAndNoticePostId(Member member, Long noticePostId);
    long countByNoticePostId(Long noticePostId);
}

