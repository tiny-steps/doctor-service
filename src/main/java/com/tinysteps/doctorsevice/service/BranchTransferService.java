package com.tinysteps.doctorsevice.service;

import com.tinysteps.doctorsevice.entity.Doctor;
import com.tinysteps.doctorsevice.entity.DoctorAddress;
import com.tinysteps.doctorsevice.repository.DoctorRepository;
import com.tinysteps.doctorsevice.repository.DoctorAddressRepository;
import com.tinysteps.doctorsevice.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling doctor branch transfer operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BranchTransferService {

    private final DoctorRepository doctorRepository;
    private final DoctorAddressRepository doctorAddressRepository;
    private final SecurityService securityService;

    /**
     * Transfer doctor between branches by updating doctor-address relationships
     */
    @Transactional
    public boolean transferDoctorBetweenBranches(UUID doctorId, UUID sourceBranchId, UUID targetBranchId) {
        log.info("Transferring doctor {} from branch {} to branch {}", doctorId, sourceBranchId, targetBranchId);

        try {
            // Validate branch access
            securityService.validateBranchAccess(sourceBranchId);
            securityService.validateBranchAccess(targetBranchId);

            // Verify doctor exists
            Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
            if (doctorOpt.isEmpty()) {
                log.error("Doctor {} not found", doctorId);
                return false;
            }

            // Find doctor's current relationship with source branch
            List<DoctorAddress> sourceRelationships = doctorAddressRepository.findByDoctorIdAndAddressId(doctorId,
                    sourceBranchId);

            if (sourceRelationships.isEmpty()) {
                log.error("Doctor {} is not associated with source branch {}", doctorId, sourceBranchId);
                return false;
            }

            // Create new relationships with target branch (maintaining the same roles)
            for (DoctorAddress sourceRelationship : sourceRelationships) {
                // Check if relationship already exists
                boolean exists = doctorAddressRepository.existsByDoctorIdAndAddressIdAndPracticeRole(
                        doctorId, targetBranchId, sourceRelationship.getPracticeRole());

                if (!exists) {
                    DoctorAddress newRelationship = new DoctorAddress();
                    newRelationship.setDoctorId(doctorId);
                    newRelationship.setAddressId(targetBranchId);
                    newRelationship.setPracticeRole(sourceRelationship.getPracticeRole());
                    doctorAddressRepository.save(newRelationship);
                }
            }

            // Remove relationships with source branch
            doctorAddressRepository.deleteByDoctorIdAndAddressId(doctorId, sourceBranchId);

            // Update primary branch if it was the source branch
            Doctor doctor = doctorOpt.get();
            if (sourceBranchId.equals(doctor.getPrimaryBranchId())) {
                doctor.setPrimaryBranchId(targetBranchId);
                doctorRepository.save(doctor);
            }

            log.info("Successfully transferred doctor {} from branch {} to branch {}", doctorId, sourceBranchId,
                    targetBranchId);
            return true;

        } catch (Exception e) {
            log.error("Failed to transfer doctor {} between branches: {}", doctorId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Add doctor to additional branch without removing from current branches
     */
    @Transactional
    public boolean addDoctorToBranch(UUID doctorId, UUID branchId, DoctorAddress.PracticeRole role) {
        log.info("Adding doctor {} to branch {} with role {}", doctorId, branchId, role);

        try {
            // Validate branch access
            securityService.validateBranchAccess(branchId);

            // Verify doctor exists
            if (!doctorRepository.existsById(doctorId)) {
                log.error("Doctor {} not found", doctorId);
                return false;
            }

            // Check if relationship already exists
            boolean exists = doctorAddressRepository.existsByDoctorIdAndAddressIdAndPracticeRole(doctorId, branchId,
                    role);
            if (exists) {
                log.warn("Doctor {} already has role {} at branch {}", doctorId, role, branchId);
                return true; // Consider this a success
            }

            // Create new relationship
            DoctorAddress newRelationship = new DoctorAddress();
            newRelationship.setDoctorId(doctorId);
            newRelationship.setAddressId(branchId);
            newRelationship.setPracticeRole(role);
            doctorAddressRepository.save(newRelationship);

            log.info("Successfully added doctor {} to branch {} with role {}", doctorId, branchId, role);
            return true;

        } catch (Exception e) {
            log.error("Failed to add doctor {} to branch {}: {}", doctorId, branchId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Remove doctor from a specific branch
     */
    @Transactional
    public boolean removeDoctorFromBranch(UUID doctorId, UUID branchId) {
        log.info("Removing doctor {} from branch {}", doctorId, branchId);

        try {
            // Validate branch access
            securityService.validateBranchAccess(branchId);

            // Remove all relationships with the branch
            doctorAddressRepository.deleteByDoctorIdAndAddressId(doctorId, branchId);

            // Update primary branch if it was this branch
            Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();
                if (branchId.equals(doctor.getPrimaryBranchId())) {
                    // Find another branch to set as primary
                    List<UUID> remainingBranches = doctorAddressRepository.findAddressIdsByDoctorId(doctorId);
                    if (!remainingBranches.isEmpty()) {
                        doctor.setPrimaryBranchId(remainingBranches.get(0));
                    } else {
                        doctor.setPrimaryBranchId(null);
                    }
                    doctorRepository.save(doctor);
                }
            }

            log.info("Successfully removed doctor {} from branch {}", doctorId, branchId);
            return true;

        } catch (Exception e) {
            log.error("Failed to remove doctor {} from branch {}: {}", doctorId, branchId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get all doctors at a specific branch
     */
    public List<UUID> getDoctorsByBranch(UUID branchId) {
        try {
            // Validate branch access
            securityService.validateBranchAccess(branchId);

            return doctorAddressRepository.findDoctorIdsByAddressId(branchId);

        } catch (Exception e) {
            log.error("Failed to get doctors for branch {}: {}", branchId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get all branches where a doctor is assigned
     */
    public List<UUID> getBranchesByDoctor(UUID doctorId) {
        try {
            return doctorAddressRepository.findAddressIdsByDoctorId(doctorId);

        } catch (Exception e) {
            log.error("Failed to get branches for doctor {}: {}", doctorId, e.getMessage(), e);
            return List.of();
        }
    }
}