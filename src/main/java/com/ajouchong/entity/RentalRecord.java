package com.ajouchong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_item_id", nullable = false)
    private RentalItem rentalItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String managerName;

    @Column(nullable = false)
    private LocalDate rentalDate;

    @Column(nullable = false)
    private String borrowerName;

    @Column(nullable = false)
    private String borrowerDepartment;

    @Column(nullable = false)
    private String borrowerStudentId;

    @Column(nullable = false)
    private String borrowerPhone;

    @Column(nullable = false)
    private String borrowerSignature;

    private LocalDateTime returnedAt;

    private String managerConfirmation;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

