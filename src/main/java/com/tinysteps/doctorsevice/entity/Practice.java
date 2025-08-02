package com.tinysteps.doctorsevice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "doctor_practices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Practice {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "practice_name", nullable = false, length = 255)
    private String practiceName;

    @Column(name = "practice_type", length = 30)
    private String practiceType;

    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Column(length = 200)
    private String slug;

    @Column(name = "practice_position")
    private Integer practicePosition;

    @Column(name = "created_at")
    private Timestamp createdAt;
}
