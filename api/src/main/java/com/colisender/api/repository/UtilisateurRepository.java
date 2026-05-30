package com.colisender.api.repository;

import com.colisender.api.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {
    boolean existsByEmail(String email);
    Optional<Utilisateur> findByEmail(String email);
}