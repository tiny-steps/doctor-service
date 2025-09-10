package com.tinysteps.doctorservice.controller;

import com.tinysteps.doctorservice.model.PhotoRequestDto;
import com.tinysteps.doctorservice.model.PhotoResponseDto;
import com.tinysteps.doctorservice.model.ResponseModel;
import com.tinysteps.doctorservice.service.PhotoService;
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
@RequestMapping("/api/v1/photos")
@RequiredArgsConstructor
@Tag(name = "Photo Management", description = "APIs for managing doctor photos")
@SecurityRequirement(name = "Bearer Authentication")
public class PhotoController {

    private final PhotoService photoService;

    @Operation(summary = "Create photo", description = "Creates a new photo for a doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Photo created successfully",
                    content = @Content(schema = @Schema(implementation = PhotoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PhotoResponseDto>> createPhoto(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Photo details", required = true) @Valid @RequestBody PhotoRequestDto requestDto) {
        PhotoResponseDto photo = photoService.create(doctorId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<PhotoResponseDto>builder()
                .status(HttpStatus.CREATED)
                .message("Photo created successfully")
                .data(photo)
                .build());
    }

    @Operation(summary = "Get photo by ID", description = "Retrieves a photo by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo found",
                    content = @Content(schema = @Schema(implementation = PhotoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Photo not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<PhotoResponseDto>> getPhotoById(
            @Parameter(description = "Photo ID", required = true) @PathVariable UUID id) {
        PhotoResponseDto photo = photoService.findById(id);
        return ResponseEntity.ok(ResponseModel.<PhotoResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Photo retrieved successfully")
                .data(photo)
                .build());
    }

    @Operation(summary = "Get all photos", description = "Retrieves a paginated list of all photos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photos retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<PhotoResponseDto>>> getAllPhotos(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PhotoResponseDto> photos = photoService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PhotoResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Photos retrieved successfully")
                .data(photos)
                .build());
    }

    @Operation(summary = "Update photo", description = "Updates an existing photo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo updated successfully"),
            @ApiResponse(responseCode = "404", description = "Photo not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@photoSecurity.isPhotoOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PhotoResponseDto>> updatePhoto(
            @Parameter(description = "Photo ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated photo details", required = true) @Valid @RequestBody PhotoRequestDto requestDto) {
        PhotoResponseDto photo = photoService.update(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<PhotoResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Photo updated successfully")
                .data(photo)
                .build());
    }

    @Operation(summary = "Partially update photo", description = "Partially updates an existing photo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo updated successfully"),
            @ApiResponse(responseCode = "404", description = "Photo not found")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("@photoSecurity.isPhotoOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PhotoResponseDto>> partialUpdatePhoto(
            @Parameter(description = "Photo ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Partial photo details", required = true) @Valid @RequestBody PhotoRequestDto requestDto) {
        PhotoResponseDto photo = photoService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(ResponseModel.<PhotoResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Photo updated successfully")
                .data(photo)
                .build());
    }

    @Operation(summary = "Delete photo", description = "Deletes a photo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Photo deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Photo not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@photoSecurity.isPhotoOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deletePhoto(
            @Parameter(description = "Photo ID", required = true) @PathVariable UUID id) {
        photoService.delete(id);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Photo deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Get photos by doctor", description = "Retrieves all photos for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ResponseModel<List<PhotoResponseDto>>> getPhotosByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<PhotoResponseDto> photos = photoService.findByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<PhotoResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor photos retrieved successfully")
                .data(photos)
                .build());
    }

    @Operation(summary = "Get photos by doctor (paginated)", description = "Retrieves paginated photos for a specific doctor")
    @GetMapping("/doctor/{doctorId}/paginated")
    public ResponseEntity<ResponseModel<Page<PhotoResponseDto>>> getPhotosByDoctorPaginated(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PhotoResponseDto> photos = photoService.findByDoctorId(doctorId, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PhotoResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Doctor photos retrieved successfully")
                .data(photos)
                .build());
    }

    @Operation(summary = "Get default photo by doctor", description = "Retrieves the default photo for a doctor")
    @GetMapping("/doctor/{doctorId}/default")
    public ResponseEntity<ResponseModel<PhotoResponseDto>> getDefaultPhotoByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        PhotoResponseDto photo = photoService.findDefaultPhotoByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<PhotoResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Default photo retrieved successfully")
                .data(photo)
                .build());
    }

    @Operation(summary = "Get non-default photos by doctor", description = "Retrieves non-default photos for a doctor")
    @GetMapping("/doctor/{doctorId}/non-default")
    public ResponseEntity<ResponseModel<List<PhotoResponseDto>>> getNonDefaultPhotosByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        List<PhotoResponseDto> photos = photoService.findNonDefaultPhotosByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<List<PhotoResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Non-default photos retrieved successfully")
                .data(photos)
                .build());
    }

    @Operation(summary = "Set as default photo", description = "Sets a photo as the default photo for a doctor")
    @PostMapping("/{id}/set-default")
    @PreAuthorize("@photoSecurity.isPhotoOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PhotoResponseDto>> setAsDefaultPhoto(
            @Parameter(description = "Photo ID", required = true) @PathVariable UUID id) {
        PhotoResponseDto photo = photoService.setAsDefaultPhoto(id);
        return ResponseEntity.ok(ResponseModel.<PhotoResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Photo set as default successfully")
                .data(photo)
                .build());
    }

    @Operation(summary = "Remove default status", description = "Removes default status from a photo")
    @PostMapping("/{id}/remove-default")
    @PreAuthorize("@photoSecurity.isPhotoOwner(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PhotoResponseDto>> removeDefaultStatus(
            @Parameter(description = "Photo ID", required = true) @PathVariable UUID id) {
        PhotoResponseDto photo = photoService.removeDefaultStatus(id);
        return ResponseEntity.ok(ResponseModel.<PhotoResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Default status removed successfully")
                .data(photo)
                .build());
    }

    @Operation(summary = "Get photo by URL", description = "Retrieves a photo by its URL")
    @GetMapping("/url")
    public ResponseEntity<ResponseModel<PhotoResponseDto>> getPhotoByUrl(
            @Parameter(description = "Photo URL", required = true) @RequestParam String photoUrl) {
        PhotoResponseDto photo = photoService.findByPhotoUrl(photoUrl);
        return ResponseEntity.ok(ResponseModel.<PhotoResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Photo retrieved successfully")
                .data(photo)
                .build());
    }

    @Operation(summary = "Search photos by URL pattern", description = "Searches photos by URL pattern")
    @GetMapping("/search/url-pattern")
    public ResponseEntity<ResponseModel<Page<PhotoResponseDto>>> searchPhotosByUrlPattern(
            @Parameter(description = "URL pattern to search") @RequestParam String urlPattern,
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PhotoResponseDto> photos = photoService.findByUrlPattern(urlPattern, pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PhotoResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Photos found")
                .data(photos)
                .build());
    }

    @Operation(summary = "Get default photos", description = "Retrieves all default photos")
    @GetMapping("/default")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<PhotoResponseDto>>> getDefaultPhotos(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PhotoResponseDto> photos = photoService.findDefaultPhotos(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PhotoResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Default photos retrieved successfully")
                .data(photos)
                .build());
    }

    @Operation(summary = "Get non-default photos", description = "Retrieves all non-default photos")
    @GetMapping("/non-default")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<PhotoResponseDto>>> getNonDefaultPhotos(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<PhotoResponseDto> photos = photoService.findNonDefaultPhotos(pageable);
        return ResponseEntity.ok(ResponseModel.<Page<PhotoResponseDto>>builder()
                .status(HttpStatus.OK)
                .message("Non-default photos retrieved successfully")
                .data(photos)
                .build());
    }

    @Operation(summary = "Check if photo exists", description = "Checks if a photo exists by ID")
    @GetMapping("/{id}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkPhotoExists(
            @Parameter(description = "Photo ID", required = true) @PathVariable UUID id) {
        boolean exists = photoService.existsById(id);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Photo existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor has photos", description = "Checks if a doctor has any photos")
    @GetMapping("/doctor/{doctorId}/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasPhotos(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        boolean exists = photoService.existsByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor photos existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if photo URL exists", description = "Checks if a photo URL exists")
    @GetMapping("/url/exists")
    public ResponseEntity<ResponseModel<Boolean>> checkPhotoUrlExists(
            @Parameter(description = "Photo URL", required = true) @RequestParam String photoUrl) {
        boolean exists = photoService.existsByPhotoUrl(photoUrl);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Photo URL existence checked")
                .data(exists)
                .build());
    }

    @Operation(summary = "Check if doctor has default photo", description = "Checks if a doctor has a default photo")
    @GetMapping("/doctor/{doctorId}/has-default")
    public ResponseEntity<ResponseModel<Boolean>> checkDoctorHasDefaultPhoto(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        boolean hasDefault = photoService.hasDefaultPhoto(doctorId);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Doctor default photo existence checked")
                .data(hasDefault)
                .build());
    }

    @Operation(summary = "Check if photo URL is unique", description = "Checks if a photo URL is unique")
    @GetMapping("/url/unique")
    public ResponseEntity<ResponseModel<Boolean>> checkPhotoUrlUnique(
            @Parameter(description = "Photo URL", required = true) @RequestParam String photoUrl) {
        boolean isUnique = photoService.isPhotoUrlUnique(photoUrl);
        return ResponseEntity.ok(ResponseModel.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Photo URL uniqueness checked")
                .data(isUnique)
                .build());
    }

    @Operation(summary = "Count photos by doctor", description = "Gets the count of photos for a doctor")
    @GetMapping("/doctor/{doctorId}/count")
    public ResponseEntity<ResponseModel<Long>> countPhotosByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        long count = photoService.countByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor photos count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Count default photos by doctor", description = "Gets the count of default photos for a doctor")
    @GetMapping("/doctor/{doctorId}/default/count")
    public ResponseEntity<ResponseModel<Long>> countDefaultPhotosByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        long count = photoService.countDefaultPhotosByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Doctor default photos count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Get total photos count", description = "Gets the total count of all photos")
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Long>> getTotalPhotosCount() {
        long count = photoService.countAll();
        return ResponseEntity.ok(ResponseModel.<Long>builder()
                .status(HttpStatus.OK)
                .message("Total photos count retrieved successfully")
                .data(count)
                .build());
    }

    @Operation(summary = "Get photo count statistics", description = "Gets photo count statistics")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Object[]>> getPhotoCountStatistics() {
        Object[] stats = photoService.getPhotoCountStatistics();
        return ResponseEntity.ok(ResponseModel.<Object[]>builder()
                .status(HttpStatus.OK)
                .message("Photo count statistics retrieved successfully")
                .data(stats)
                .build());
    }

    @Operation(summary = "Create batch photos", description = "Creates multiple photos for a doctor")
    @PostMapping("/doctor/{doctorId}/batch")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<PhotoResponseDto>>> createBatchPhotos(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "List of photo details", required = true) @Valid @RequestBody List<PhotoRequestDto> requestDtos) {
        List<PhotoResponseDto> photos = photoService.createBatch(doctorId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseModel.<List<PhotoResponseDto>>builder()
                .status(HttpStatus.CREATED)
                .message("Batch photos created successfully")
                .data(photos)
                .build());
    }

    @Operation(summary = "Delete photos by doctor", description = "Deletes all photos for a doctor")
    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deletePhotosByDoctor(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        photoService.deleteByDoctorId(doctorId);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Doctor photos deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Delete batch photos", description = "Deletes multiple photos by IDs")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteBatchPhotos(
            @Parameter(description = "List of photo IDs", required = true) @RequestBody List<UUID> ids) {
        photoService.deleteBatch(ids);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.NO_CONTENT)
                .message("Batch photos deleted successfully")
                .data(null)
                .build());
    }

    @Operation(summary = "Find doctors with multiple photos", description = "Finds doctors who have multiple photos")
    @GetMapping("/doctors/multiple-photos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<UUID>>> findDoctorsWithMultiplePhotos() {
        List<UUID> doctorIds = photoService.findDoctorsWithMultiplePhotos();
        return ResponseEntity.ok(ResponseModel.<List<UUID>>builder()
                .status(HttpStatus.OK)
                .message("Doctors with multiple photos found")
                .data(doctorIds)
                .build());
    }

    @Operation(summary = "Find doctors without photos", description = "Finds doctors who don't have any photos")
    @GetMapping("/doctors/without-photos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<UUID>>> findDoctorsWithoutPhotos() {
        List<UUID> doctorIds = photoService.findDoctorsWithoutPhotos();
        return ResponseEntity.ok(ResponseModel.<List<UUID>>builder()
                .status(HttpStatus.OK)
                .message("Doctors without photos found")
                .data(doctorIds)
                .build());
    }

    @Operation(summary = "Find doctors without default photos", description = "Finds doctors who don't have default photos")
    @GetMapping("/doctors/without-default-photos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<List<UUID>>> findDoctorsWithoutDefaultPhotos() {
        List<UUID> doctorIds = photoService.findDoctorsWithoutDefaultPhotos();
        return ResponseEntity.ok(ResponseModel.<List<UUID>>builder()
                .status(HttpStatus.OK)
                .message("Doctors without default photos found")
                .data(doctorIds)
                .build());
    }

    @Operation(summary = "Replace default photo", description = "Replaces the default photo for a doctor")
    @PostMapping("/doctor/{doctorId}/replace-default")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<PhotoResponseDto>> replaceDefaultPhoto(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "New photo details", required = true) @Valid @RequestBody PhotoRequestDto newPhotoRequest) {
        PhotoResponseDto photo = photoService.replaceDefaultPhoto(doctorId, newPhotoRequest);
        return ResponseEntity.ok(ResponseModel.<PhotoResponseDto>builder()
                .status(HttpStatus.OK)
                .message("Default photo replaced successfully")
                .data(photo)
                .build());
    }

    @Operation(summary = "Ensure default photo", description = "Ensures a doctor has a default photo")
    @PostMapping("/doctor/{doctorId}/ensure-default")
    @PreAuthorize("@doctorSecurity.isDoctorOwner(authentication, #doctorId) or hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> ensureDefaultPhoto(
            @Parameter(description = "Doctor ID", required = true) @PathVariable UUID doctorId) {
        photoService.ensureDefaultPhoto(doctorId);
        return ResponseEntity.ok(ResponseModel.<Void>builder()
                .status(HttpStatus.OK)
                .message("Default photo ensured successfully")
                .data(null)
                .build());
    }
}
