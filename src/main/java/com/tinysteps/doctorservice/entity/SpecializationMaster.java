package com.tinysteps.doctorservice.entity;

import com.tinysteps.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Master list of specializations - one record per unique specialization
 * Multiple doctors can reference the same specialization
 */
@Entity
@Table(name = "specializations", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SpecializationMaster extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "specializationMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoctorSpecialization> doctorSpecializations = new ArrayList<>();
}




