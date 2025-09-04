package com.tinysteps.doctorsevice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "doctor_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DoctorAddressId.class)
public class DoctorAddress {

    @Id
    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Id
    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Id
    @Column(name = "practice_role", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PracticeRole practiceRole = PracticeRole.CONSULTANT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Doctor doctor;

    public enum PracticeRole {
        CONSULTANT,
        VISITING_DOCTOR,
        HEAD_OF_DEPARTMENT,
        RESIDENT,
        SPECIALIST,
        EMERGENCY_DOCTOR
    }
}