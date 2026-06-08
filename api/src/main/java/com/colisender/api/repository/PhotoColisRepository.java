package com.colisender.api.repository;

import com.colisender.api.model.PhotoColis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PhotoColisRepository extends JpaRepository<PhotoColis, UUID> {
}