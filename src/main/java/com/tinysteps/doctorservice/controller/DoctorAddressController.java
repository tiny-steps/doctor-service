package com.tinysteps.doctorservice.controller;

import com.tinysteps.doctorservice.model.DoctorAddressRequestDto;
import com.tinysteps.doctorservice.model.DoctorAddressResponseDto;
import com.tinysteps.doctorservice.model.ResponseModel;
import com.tinysteps.doctorservice.service.DoctorAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Address Management", description = "APIs for managing doctor-address relationships")
@SecurityRequirement(name = "Bearer Authentication")
public class DoctorAddressController {

    private final DoctorAddressService doctorAddressService;

    @Operation(summary = "Add address to doctor", description = "Associates an address with a doctor for a specific practice role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Address added to doctor successfully",
                    content = @Content(schema = @Schema(implementation = DoctorAddressResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or relationship already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{doctorId}/addresses")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<DoctorAddressResponseDto>> addDoctorAddress(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorAddressRequestDto requestDto) {
        DoctorAddressResponseDto response = doctorAddressService.addDoctorAddress(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.success("Address added to doctor successfully", response));
    }

    @Operation(summary = "Remove address from doctor", description = "Deactivates an address association from a doctor (sets status to INACTIVE)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Address deactivated from doctor successfully"),
            @ApiResponse(responseCode = "400", description = "Relationship does not exist"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{doctorId}/addresses/{addressId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<Void> removeDoctorAddress(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Address ID", required = true) @PathVariable UUID addressId,
            @Parameter(description = "Practice role", required = true) @RequestParam String practiceRole) {
        doctorAddressService.removeDoctorAddress(doctorId, addressId, practiceRole);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate address for doctor", description = "Activates an address association for a doctor (sets status to ACTIVE)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Address activated for doctor successfully"),
            @ApiResponse(responseCode = "400", description = "Relationship does not exist"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{doctorId}/addresses/{addressId}/activate")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<Void> activateDoctorAddress(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Address ID", required = true) @PathVariable UUID addressId,
            @Parameter(description = "Practice role", required = true) @RequestParam String practiceRole) {
        doctorAddressService.activateDoctorAddress(doctorId, addressId, practiceRole);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get doctor addresses", description = "Retrieves all active addresses associated with a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor addresses retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{doctorId}/addresses")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<List<DoctorAddressResponseDto>> getDoctorAddresses(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<DoctorAddressResponseDto> addresses = doctorAddressService.findActiveDoctorAddresses(doctorId);
        return ResponseEntity.ok(addresses);
    }

    @Operation(summary = "Get all doctor addresses", description = "Retrieves all addresses (active and inactive) associated with a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All doctor addresses retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{doctorId}/addresses/all")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<List<DoctorAddressResponseDto>> getAllDoctorAddresses(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<DoctorAddressResponseDto> addresses = doctorAddressService.findByDoctorId(doctorId);
        return ResponseEntity.ok(addresses);
    }

    @Operation(summary = "Get doctor addresses with pagination", description = "Retrieves addresses associated with a doctor with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor addresses retrieved successfully")
    })
    @GetMapping("/{doctorId}/addresses/paginated")
    public ResponseEntity<ResponseModel<Page<DoctorAddressResponseDto>>> getDoctorAddressesPaginated(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorAddressResponseDto> addresses = doctorAddressService.findByDoctorId(doctorId, pageable);
        return ResponseEntity.ok(ResponseModel.success("Doctor addresses retrieved successfully", addresses));
    }

    @Operation(summary = "Get doctors by address", description = "Retrieves all doctors associated with an address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors by address retrieved successfully")
    })
    @GetMapping("/addresses/{addressId}/doctors")
    public ResponseEntity<ResponseModel<List<DoctorAddressResponseDto>>> getDoctorsByAddress(
            @Parameter(description = "Address ID", required = true) @PathVariable UUID addressId) {
        List<DoctorAddressResponseDto> doctors = doctorAddressService.findByAddressId(addressId);
        return ResponseEntity.ok(ResponseModel.success("Doctors by address retrieved successfully", doctors));
    }

    @Operation(summary = "Get doctors by address with pagination", description = "Retrieves doctors associated with an address with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors by address retrieved successfully")
    })
    @GetMapping("/addresses/{addressId}/doctors/paginated")
    public ResponseEntity<ResponseModel<Page<DoctorAddressResponseDto>>> getDoctorsByAddressPaginated(
            @Parameter(description = "Address ID", required = true) @PathVariable UUID addressId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<DoctorAddressResponseDto> doctors = doctorAddressService.findByAddressId(addressId, pageable);
        return ResponseEntity.ok(ResponseModel.success("Doctors by address retrieved successfully", doctors));
    }

    @Operation(summary = "Get doctor-address relationships by practice role", description = "Retrieves all doctor-address relationships for a specific practice role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relationships by practice role retrieved successfully")
    })
    @GetMapping("/addresses/practice-role/{practiceRole}")
    public ResponseEntity<ResponseModel<List<DoctorAddressResponseDto>>> getByPracticeRole(
            @Parameter(description = "Practice role", required = true) @PathVariable String practiceRole) {
        List<DoctorAddressResponseDto> relationships = doctorAddressService.findByPracticeRole(practiceRole);
        return ResponseEntity.ok(ResponseModel.success("Relationships by practice role retrieved successfully", relationships));
    }

    @Operation(summary = "Get doctor addresses by practice role", description = "Retrieves addresses for a doctor filtered by practice role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor addresses by practice role retrieved successfully")
    })
    @GetMapping("/{doctorId}/addresses/practice-role/{practiceRole}")
    public ResponseEntity<ResponseModel<List<DoctorAddressResponseDto>>> getDoctorAddressesByPracticeRole(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Practice role", required = true) @PathVariable String practiceRole) {
        List<DoctorAddressResponseDto> addresses = doctorAddressService.findByDoctorIdAndPracticeRole(doctorId, practiceRole);
        return ResponseEntity.ok(ResponseModel.success("Doctor addresses by practice role retrieved successfully", addresses));
    }

    @Operation(summary = "Check if doctor-address relationship exists", description = "Checks if a specific doctor-address relationship exists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relationship existence checked successfully")
    })
    @GetMapping("/{doctorId}/addresses/{addressId}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorAddressExists(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Address ID", required = true) @PathVariable UUID addressId,
            @Parameter(description = "Practice role", required = true) @RequestParam String practiceRole) {
        boolean exists = doctorAddressService.existsDoctorAddress(doctorId, addressId, practiceRole);
        return ResponseEntity.ok(ResponseModel.success("Relationship existence checked successfully", exists));
    }

    @Operation(summary = "Remove all addresses from doctor", description = "Removes all address associations from a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "All addresses removed from doctor successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{doctorId}/addresses")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<Void> removeAllDoctorAddresses(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        doctorAddressService.removeAllDoctorAddresses(doctorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Count doctor addresses", description = "Gets the count of addresses associated with a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor address count retrieved successfully")
    })
    @GetMapping("/{doctorId}/addresses/count")
    public ResponseEntity<ResponseModel<Long>> countDoctorAddresses(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        long count = doctorAddressService.countByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.success("Doctor address count retrieved successfully", count));
    }

    @Operation(summary = "Count doctors by address", description = "Gets the count of doctors associated with an address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor count by address retrieved successfully")
    })
    @GetMapping("/addresses/{addressId}/doctors/count")
    public ResponseEntity<ResponseModel<Long>> countDoctorsByAddress(
            @Parameter(description = "Address ID", required = true) @PathVariable UUID addressId) {
        long count = doctorAddressService.countByAddressId(addressId);
        return ResponseEntity.ok(ResponseModel.success("Doctor count by address retrieved successfully", count));
    }

    @Operation(summary = "Add multiple addresses to doctor", description = "Associates multiple addresses with a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Addresses added to doctor successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or some relationships already exist"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{doctorId}/addresses/batch")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<DoctorAddressResponseDto>>> addMultipleDoctorAddresses(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody List<DoctorAddressRequestDto> requestDtos) {
        List<DoctorAddressResponseDto> responses = doctorAddressService.addMultipleDoctorAddresses(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.success("Addresses added to doctor successfully", responses));
    }
}
