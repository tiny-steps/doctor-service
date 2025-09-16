package com.tinysteps.doctorservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "doctor_session_pricing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pricing {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id", nullable = false)
    @JsonManagedReference
    private Doctor doctor;

    @Column(name = "session_type_id", nullable = false)
    private UUID sessionTypeId;

    @Column(name = "custom_price", precision = 10, scale = 2)
    private BigDecimal customPrice;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
