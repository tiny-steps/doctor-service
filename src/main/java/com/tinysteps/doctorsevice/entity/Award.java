package com.tinysteps.doctorsevice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "doctor_awards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Award {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "awarded_year")
    private Integer awardedYear;

    @Column(length = 255)
    private String summary;
}
