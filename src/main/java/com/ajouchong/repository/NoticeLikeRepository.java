package com.ajouchong.repository;

import com.ajouchong.entity.Member;
import com.ajouchong.entity.NoticeLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeLikeRepository extends JpaRepository<NoticeLike, Long> {
    Optional<NoticeLike> findByMemberAndNoticePostId(Member member, Long noticePostId);
    long countByNoticePostId(Long noticePostId);
    List<NoticeLike> findByNoticePostId(Long noticePostId);
    
    @Modifying
    @Query("DELETE FROM NoticeLike nl WHERE nl.noticePostId = :postId")
    void deleteByNoticePostId(@Param("postId") Long postId);
}

