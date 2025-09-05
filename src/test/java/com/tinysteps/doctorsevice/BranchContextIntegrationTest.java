package com.tinysteps.doctorsevice;

import com.tinysteps.doctorsevice.entity.Doctor;
import com.tinysteps.doctorsevice.model.DoctorRequestDto;
import com.tinysteps.doctorsevice.model.DoctorResponseDto;
import com.tinysteps.doctorsevice.repository.DoctorRepository;
import com.tinysteps.doctorsevice.service.DoctorService;
import com.tinysteps.doctorsevice.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BranchContextIntegrationTest {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorRepository doctorRepository;

    @MockBean
    private SecurityService securityService;

    private UUID branchId1;
    private UUID branchId2;
    private UUID userId1;
    private UUID userId2;
    private UUID userId3;

    @BeforeEach
    void setUp() {
        branchId1 = UUID.randomUUID();
        branchId2 = UUID.randomUUID();
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        userId3 = UUID.randomUUID();

        // Mock security service to return branch context
        when(securityService.getPrimaryBranchId()).thenReturn(branchId1);
        when(securityService.hasBranchAccess(branchId1)).thenReturn(true);
        when(securityService.hasBranchAccess(branchId2)).thenReturn(false);
    }

    @Test
    void testCreateDoctorWithBranchContext() {
        // Given
        DoctorRequestDto requestDto = createDoctorRequestDto(userId1, branchId1, false);

        // When
        DoctorResponseDto response = doctorService.create(requestDto);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.primaryBranchId()).isEqualTo(branchId1.toString());
        assertThat(response.isMultiBranch()).isFalse();
    }

    @Test
    void testCreateMultiBranchDoctor() {
        // Given
        DoctorRequestDto requestDto = createDoctorRequestDto(userId2, branchId1, true);

        // When
        DoctorResponseDto response = doctorService.create(requestDto);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.primaryBranchId()).isEqualTo(branchId1.toString());
        assertThat(response.isMultiBranch()).isTrue();
    }

    @Test
    void testFindDoctorsByBranch() {
        // Given
        createTestDoctors();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<DoctorResponseDto> result = doctorService.findByBranch(branchId1, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2); // Two doctors in branch1
        assertThat(result.getContent())
                .allMatch(doctor -> doctor.primaryBranchId().equals(branchId1.toString()));
    }

    @Test
    void testFindDoctorsByBranchAndStatus() {
        // Given
        createTestDoctors();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<DoctorResponseDto> result = doctorService.findByBranchAndStatus(branchId1, "ACTIVE", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(doctor -> doctor.primaryBranchId().equals(branchId1.toString()))
                .allMatch(doctor -> doctor.status().equals("ACTIVE"));
    }

    @Test
    void testFindDoctorsByBranchAndVerificationStatus() {
        // Given
        createTestDoctors();

        // When
        List<DoctorResponseDto> result = doctorService.findByBranchAndVerificationStatus(branchId1, true);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); // Only one verified doctor in branch1
        assertThat(result.get(0).primaryBranchId()).isEqualTo(branchId1.toString());
        assertThat(result.get(0).isVerified()).isTrue();
    }

    @Test
    void testFindMultiBranchDoctors() {
        // Given
        createTestDoctors();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<DoctorResponseDto> result = doctorService.findMultiBranchDoctors(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1); // Only one multi-branch doctor
        assertThat(result.getContent().get(0).isMultiBranch()).isTrue();
    }

    @Test
    void testFindDoctorsByCurrentUserBranch() {
        // Given
        createTestDoctors();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<DoctorResponseDto> result = doctorService.findDoctorsByCurrentUserBranch(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2); // Two doctors in user's primary branch
        assertThat(result.getContent())
                .allMatch(doctor -> doctor.primaryBranchId().equals(branchId1.toString()));
    }

    @Test
    void testCountByBranch() {
        // Given
        createTestDoctors();

        // When
        long count = doctorService.countByBranch(branchId1);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByBranchAndStatus() {
        // Given
        createTestDoctors();

        // When
        long count = doctorService.countByBranchAndStatus(branchId1, "ACTIVE");

        // Then
        assertThat(count).isEqualTo(2);
    }

    private void createTestDoctors() {
        // Doctor 1: Branch 1, Single branch, Verified, Active
        Doctor doctor1 = new Doctor();
        doctor1.setUserId(userId1);
        doctor1.setName("Dr. John Doe");
        doctor1.setSlug("dr-john-doe");
        doctor1.setGender("MALE");
        doctor1.setStatus("ACTIVE");
        doctor1.setIsVerified(true);
        doctor1.setPrimaryBranchId(branchId1);
        doctor1.setIsMultiBranch(false);
        doctorRepository.save(doctor1);

        // Doctor 2: Branch 1, Multi-branch, Not verified, Active
        Doctor doctor2 = new Doctor();
        doctor2.setUserId(userId2);
        doctor2.setName("Dr. Jane Smith");
        doctor2.setSlug("dr-jane-smith");
        doctor2.setGender("FEMALE");
        doctor2.setStatus("ACTIVE");
        doctor2.setIsVerified(false);
        doctor2.setPrimaryBranchId(branchId1);
        doctor2.setIsMultiBranch(true);
        doctorRepository.save(doctor2);

        // Doctor 3: Branch 2, Single branch, Verified, Active
        Doctor doctor3 = new Doctor();
        doctor3.setUserId(userId3);
        doctor3.setName("Dr. Bob Johnson");
        doctor3.setSlug("dr-bob-johnson");
        doctor3.setGender("MALE");
        doctor3.setStatus("ACTIVE");
        doctor3.setIsVerified(true);
        doctor3.setPrimaryBranchId(branchId2);
        doctor3.setIsMultiBranch(false);
        doctorRepository.save(doctor3);
    }

    private DoctorRequestDto createDoctorRequestDto(UUID userId, UUID branchId, boolean isMultiBranch) {
        return DoctorRequestDto.builder()
                .userId(userId.toString())
                .name("Test Doctor")
                .slug("test-doctor-" + UUID.randomUUID().toString().substring(0, 8))
                .gender("MALE")
                .status("ACTIVE")
                .isVerified(false)
                .primaryBranchId(branchId.toString())
                .isMultiBranch(isMultiBranch)
                .build();
    }
}