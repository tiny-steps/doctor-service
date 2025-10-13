package com.tinysteps.doctorservice.service;

import com.tinysteps.doctorservice.entity.SpecializationMaster;
import com.tinysteps.doctorservice.repository.SpecializationMasterRepository;
import com.tinysteps.doctorservice.service.impl.SpecializationMasterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD Test Cases for Specialization Master Management
 * 
 * User Stories:
 * 1. As an admin, I can create a new specialization (e.g., "Cardiology")
 * 2. As an admin, I can view all specializations
 * 3. As an admin, I can update a specialization
 * 4. As an admin, I can deactivate/activate a specialization
 * 5. System should prevent duplicate specialization names
 * 6. System should provide case-insensitive search
 */
@ExtendWith(MockitoExtension.class)
class SpecializationMasterServiceTest {

    @Mock
    private SpecializationMasterRepository specializationMasterRepository;

    @InjectMocks
    private SpecializationMasterServiceImpl specializationMasterService;

    private SpecializationMaster cardiology;
    private SpecializationMaster pediatrics;

    @BeforeEach
    void setUp() {
        cardiology = new SpecializationMaster();
        cardiology.setId(UUID.randomUUID());
        cardiology.setName("Cardiology");
        cardiology.setDescription("Heart and cardiovascular system");
        cardiology.setIsActive(true);

        pediatrics = new SpecializationMaster();
        pediatrics.setId(UUID.randomUUID());
        pediatrics.setName("Pediatrics");
        pediatrics.setDescription("Medical care for children");
        pediatrics.setIsActive(true);
    }

    // Test Case 1: Create a new specialization
    @Test
    void shouldCreateNewSpecialization() {
        // Given
        String name = "Neurology";
        String description = "Brain and nervous system";

        when(specializationMasterRepository.existsByNameIgnoreCase(name)).thenReturn(false);
        when(specializationMasterRepository.save(any(SpecializationMaster.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        SpecializationMaster result = specializationMasterService.create(name, description);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getIsActive()).isTrue();
        verify(specializationMasterRepository).save(any(SpecializationMaster.class));
    }

    // Test Case 2: Prevent duplicate specialization names
    @Test
    void shouldNotCreateDuplicateSpecialization() {
        // Given
        String name = "Cardiology";
        when(specializationMasterRepository.existsByNameIgnoreCase(name)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> specializationMasterService.create(name, "Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(specializationMasterRepository, never()).save(any());
    }

    // Test Case 3: Prevent duplicate specialization names (case-insensitive)
    @Test
    void shouldPreventDuplicateIgnoringCase() {
        // Given
        when(specializationMasterRepository.existsByNameIgnoreCase("CARDIOLOGY")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> specializationMasterService.create("CARDIOLOGY", "Description"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Test Case 4: Get all active specializations
    @Test
    void shouldGetAllActiveSpecializations() {
        // Given
        List<SpecializationMaster> activeList = Arrays.asList(cardiology, pediatrics);
        when(specializationMasterRepository.findByIsActiveTrue()).thenReturn(activeList);

        // When
        List<SpecializationMaster> result = specializationMasterService.getAllActive();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(cardiology, pediatrics);
    }

    // Test Case 5: Get or create - returns existing
    @Test
    void shouldReturnExistingSpecializationWhenGetOrCreate() {
        // Given
        when(specializationMasterRepository.findByNameIgnoreCase("Cardiology")).thenReturn(Optional.of(cardiology));

        // When
        SpecializationMaster result = specializationMasterService.getOrCreate("Cardiology");

        // Then
        assertThat(result).isEqualTo(cardiology);
        verify(specializationMasterRepository, never()).save(any());
    }

    // Test Case 6: Get or create - creates new
    @Test
    void shouldCreateNewSpecializationWhenGetOrCreate() {
        // Given
        String name = "Orthopedics";
        when(specializationMasterRepository.findByNameIgnoreCase(name)).thenReturn(Optional.empty());
        when(specializationMasterRepository.save(any(SpecializationMaster.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        SpecializationMaster result = specializationMasterService.getOrCreate(name);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        verify(specializationMasterRepository).save(any(SpecializationMaster.class));
    }

    // Test Case 7: Update specialization
    @Test
    void shouldUpdateSpecialization() {
        // Given
        UUID id = cardiology.getId();
        String newName = "Cardiology & Vascular";
        String newDesc = "Updated description";

        when(specializationMasterRepository.findById(id)).thenReturn(Optional.of(cardiology));
        when(specializationMasterRepository.findByNameIgnoreCase(newName)).thenReturn(Optional.empty());
        when(specializationMasterRepository.save(any(SpecializationMaster.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        SpecializationMaster result = specializationMasterService.update(id, newName, newDesc);

        // Then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getDescription()).isEqualTo(newDesc);
    }

    // Test Case 8: Deactivate specialization
    @Test
    void shouldDeactivateSpecialization() {
        // Given
        UUID id = cardiology.getId();
        when(specializationMasterRepository.findById(id)).thenReturn(Optional.of(cardiology));
        when(specializationMasterRepository.save(any(SpecializationMaster.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        specializationMasterService.deactivate(id);

        // Then
        assertThat(cardiology.getIsActive()).isFalse();
        verify(specializationMasterRepository).save(cardiology);
    }

    // Test Case 9: Activate specialization
    @Test
    void shouldActivateSpecialization() {
        // Given
        UUID id = cardiology.getId();
        cardiology.setIsActive(false);
        when(specializationMasterRepository.findById(id)).thenReturn(Optional.of(cardiology));
        when(specializationMasterRepository.save(any(SpecializationMaster.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        specializationMasterService.activate(id);

        // Then
        assertThat(cardiology.getIsActive()).isTrue();
        verify(specializationMasterRepository).save(cardiology);
    }

    // Test Case 10: Reject empty name
    @Test
    void shouldRejectEmptyName() {
        assertThatThrownBy(() -> specializationMasterService.create("", "Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");

        assertThatThrownBy(() -> specializationMasterService.create(null, "Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }

    // Test Case 11: Trim whitespace from names
    @Test
    void shouldTrimWhitespaceFromNames() {
        // Given
        String nameWithSpaces = "  Dermatology  ";
        when(specializationMasterRepository.existsByNameIgnoreCase("Dermatology")).thenReturn(false);
        when(specializationMasterRepository.save(any(SpecializationMaster.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        SpecializationMaster result = specializationMasterService.create(nameWithSpaces, "Description");

        // Then
        assertThat(result.getName()).isEqualTo("Dermatology");
    }
}



