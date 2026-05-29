package com.colisender.api.repository;

import com.colisender.api.model.VerificationIdentite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface VerificationIdentiteRepository extends JpaRepository<VerificationIdentite, UUID> {
}