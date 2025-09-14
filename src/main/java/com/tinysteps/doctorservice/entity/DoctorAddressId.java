package com.tinysteps.doctorservice.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DoctorAddressId implements Serializable {

    private UUID doctorId;
    private UUID addressId;
    private PracticeRole practiceRole; // changed from DoctorAddress.PracticeRole
}
