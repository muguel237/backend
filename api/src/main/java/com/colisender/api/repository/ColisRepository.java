package com.colisender.api.repository;

import com.colisender.api.model.Colis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ColisRepository extends JpaRepository<Colis, UUID> {
    // Spring Data générera la requête SQL automatiquement
    List<Colis> findByIdUtilisateur(UUID idUtilisateur);
}