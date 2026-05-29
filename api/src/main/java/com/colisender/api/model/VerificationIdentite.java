package com.colisender.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_identite")
public class VerificationIdentite {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_verification", updatable = false, nullable = false)
    private UUID idVerification;

    @Column(name = "id_utilisateur", nullable = false)
    private UUID idUtilisateur;

    @Column(name = "photo_recto_cni", nullable = false)
    private String photoRectoCNI;

    @Column(name = "photo_verso_cni", nullable = false)
    private String photoVersoCNI;

    @Column(name = "photo_selfie", nullable = false)
    private String photoSelfie;

    @Column(name = "score_correspondance")
    private Double scoreCorrespondance;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_verification", nullable = false)
    private StatutVerifEnum statutVerification = StatutVerifEnum.EN_ATTENTE;

    @Column(name = "date_soumission", nullable = false, updatable = false)
    private LocalDateTime dateSoumission = LocalDateTime.now();

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    // Getters et Setters manuels
    public UUID getIdVerification() { return idVerification; }
    public void setIdVerification(UUID idVerification) { this.idVerification = idVerification; }

    public UUID getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(UUID idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getPhotoRectoCNI() { return photoRectoCNI; }
    public void setPhotoRectoCNI(String photoRectoCNI) { this.photoRectoCNI = photoRectoCNI; }

    public String getPhotoVersoCNI() { return photoVersoCNI; }
    public void setPhotoVersoCNI(String photoVersoCNI) { this.photoVersoCNI = photoVersoCNI; }

    public String getPhotoSelfie() { return photoSelfie; }
    public void setPhotoSelfie(String photoSelfie) { this.photoSelfie = photoSelfie; }

    public Double getScoreCorrespondance() { return scoreCorrespondance; }
    public void setScoreCorrespondance(Double scoreCorrespondance) { this.scoreCorrespondance = scoreCorrespondance; }

    public StatutVerifEnum getStatutVerification() { return statutVerification; }
    public void setStatutVerification(StatutVerifEnum statutVerification) { this.statutVerification = statutVerification; }

    public LocalDateTime getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(LocalDateTime dateSoumission) { this.dateSoumission = dateSoumission; }

    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }
}