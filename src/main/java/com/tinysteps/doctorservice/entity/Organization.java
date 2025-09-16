package com.tinysteps.doctorservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Date;
import java.util.UUID;

@Entity
@Table(name = "doctor_organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id", nullable = false)
    @JsonManagedReference
    private Doctor doctor;

    @Column(name = "organization_name", nullable = false, length = 255)
    private String organizationName;

    @Column(length = 100)
    private String role;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(name = "tenure_start")
    private Date tenureStart;

    @Column(name = "tenure_end")
    private Date tenureEnd;

    @Column(columnDefinition = "TEXT")
    private String summary;
}
