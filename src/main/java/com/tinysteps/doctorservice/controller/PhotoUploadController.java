package com.tinysteps.doctorservice.controller;

import com.tinysteps.doctorservice.entity.BinaryPhoto;
import com.tinysteps.doctorservice.repository.BinaryPhotoRepository;
import com.tinysteps.doctorservice.model.ResponseModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/photo-uploads")
@RequiredArgsConstructor
@Tag(name = "Photo Uploads", description = "Simple binary photo upload and retrieval")
public class PhotoUploadController {

    private final BinaryPhotoRepository binaryPhotoRepository;

    @Operation(summary = "Upload a profile photo")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseModel<Map<String, String>>> upload(@RequestPart("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ResponseModel.<Map<String, String>>builder()
                        .status(HttpStatus.BAD_REQUEST)
                        .message("Empty file")
                        .data(null)
                        .build());
            }
            BinaryPhoto photo = new BinaryPhoto();
            photo.setData(file.getBytes());
            photo.setContentType(Optional.ofNullable(file.getContentType()).orElse("image/jpeg"));
            BinaryPhoto saved = binaryPhotoRepository.save(photo);
            String url = "/api/v1/photo-uploads/" + saved.getId();
            return ResponseEntity.created(URI.create(url)).body(ResponseModel.<Map<String, String>>builder()
                    .status(HttpStatus.CREATED)
                    .message("Uploaded")
                    .data(Map.of("url", url))
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseModel.<Map<String, String>>builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("Upload failed")
                            .data(null)
                            .build());
        }
    }

    @Operation(summary = "Fetch uploaded photo by id")
    @GetMapping(value = "/{id}")
    public ResponseEntity<byte[]> get(@PathVariable UUID id) {
        return binaryPhotoRepository.findById(id)
                .map(p -> ResponseEntity.ok()
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                        .contentType(MediaType.parseMediaType(p.getContentType()))
                        .body(p.getData()))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}

