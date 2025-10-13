package com.tinysteps.doctorservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

/**
 * Junction table for many-to-many relationship between Doctor and
 * SpecializationMaster
 * One doctor can have multiple specializations
 * One specialization can belong to multiple doctors
 */
@Entity
@Table(name = "doctor_specializations", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "doctor_id", "specialization_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSpecialization {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id", nullable = false)
    @JsonManagedReference
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialization_id", referencedColumnName = "id", nullable = false)
    private SpecializationMaster specializationMaster;

    @Column(length = 100)
    private String subspecialization;

    // For backward compatibility during migration - will be removed after data
    // migration
    @Column(length = 100)
    @Deprecated
    private String speciality;
}
