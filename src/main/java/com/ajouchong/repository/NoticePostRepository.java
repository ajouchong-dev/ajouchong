package com.ajouchong.repository;

import com.ajouchong.entity.NoticePost;
import lombok.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticePostRepository extends JpaRepository<NoticePost, Long> {
    @NonNull
    List<NoticePost> findAll(@NonNull Sort sort);
    
    @Query(value = "SELECT MAX(n_post_id) FROM notice_post", nativeQuery = true)
    Optional<Long> findMaxId();
    
    @Query(value = "SELECT pg_get_serial_sequence('notice_post', 'n_post_id')", nativeQuery = true)
    Optional<String> getSequenceName();
}
