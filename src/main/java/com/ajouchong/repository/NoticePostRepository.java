package com.ajouchong.repository;

import com.ajouchong.entity.NoticePost;
import lombok.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticePostRepository extends JpaRepository<NoticePost, Long> {
    @NonNull
    List<NoticePost> findAll(@NonNull Sort sort);
}
