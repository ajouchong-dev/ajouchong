package com.ajouchong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "proceeding_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProceedingDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ProceedingCategory category;

    @Column(nullable = false)
    private String title;

    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "meeting_date")
    private LocalDate meetingDate;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (displayOrder == null) {
            displayOrder = 1;
        }
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

