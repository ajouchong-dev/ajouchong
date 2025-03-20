package com.ajouchong.repository;

import com.ajouchong.entity.RulePost;
import com.ajouchong.entity.enumClass.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RulePostRepository extends JpaRepository<RulePost, Long> {
    List<RulePost> findByTypeOrderByRpCreateTimeDesc(RuleType type);
}

