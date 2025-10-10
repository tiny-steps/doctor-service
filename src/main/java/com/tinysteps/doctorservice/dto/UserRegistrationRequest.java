package com.tinysteps.doctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;
    private String imageData; // Base64 image data for avatar
}
