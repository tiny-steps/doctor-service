package com.tinysteps.doctorservice.repository;

import com.tinysteps.doctorservice.entity.BinaryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BinaryPhotoRepository extends JpaRepository<BinaryPhoto, UUID> {
}

