package com.ajouchong.repository;

import com.ajouchong.entity.Agora;
import lombok.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgoraRepository extends JpaRepository<Agora, Long> {
    @NonNull
    List<Agora> findAll(@NonNull Sort sort);
}
