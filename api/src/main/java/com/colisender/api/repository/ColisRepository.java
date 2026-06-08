package com.colisender.api.repository;

import com.colisender.api.model.Colis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ColisRepository extends JpaRepository<Colis, UUID> {
}