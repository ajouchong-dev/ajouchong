package com.ajouchong.repository;

import com.ajouchong.entity.Member;
import com.ajouchong.entity.QnaLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QnaLikeRepository extends JpaRepository<QnaLike, Long> {
    Optional<QnaLike> findByMemberAndQnaPostId(Member member, Long qnaPostId);
    long countByQnaPostId(Long qnaPostId);
}
