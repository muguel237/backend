package com.colisender.api.repository;

import com.colisender.api.model.Otp;
import com.colisender.api.model.StatutOtpEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {

    List<Otp> findByIdUtilisateurAndStatut(UUID idUtilisateur, StatutOtpEnum statut);

    @Transactional   // ← ajouté
    @Modifying
    @Query("UPDATE Otp o SET o.statut = 'EXPIRE' WHERE o.idUtilisateur = :idUtilisateur AND o.statut = 'ACTIF'")
    void expireAllActiveOtpsForUser(@Param("idUtilisateur") UUID idUtilisateur);
}