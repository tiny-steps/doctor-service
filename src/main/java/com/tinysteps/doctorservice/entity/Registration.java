package com.tinysteps.doctorservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "doctor_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Registration {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id", nullable = false)
    @JsonManagedReference
    private Doctor doctor;

    @Column(name = "registration_council_name", length = 255)
    private String registrationCouncilName;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "registration_year")
    private Integer registrationYear;
}
