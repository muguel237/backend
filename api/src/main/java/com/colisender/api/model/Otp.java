package com.colisender.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp")
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_code", updatable = false, nullable = false)
    private UUID idCode;

    // Référence vers l'utilisateur concerné
    @Column(name = "id_utilisateur", nullable = false)
    private UUID idUtilisateur;

    // Le code OTP à 6 chiffres (on stocke le hash BCrypt)
    @Column(name = "code_otp", nullable = false)
    private String codeOtp;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    // Utilise l'enum qui existe déjà dans le SQL
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutOtpEnum statut = StatutOtpEnum.ACTIF;

    // --- Getters et Setters ---
    public UUID getIdCode() { return idCode; }
    public void setIdCode(UUID idCode) { this.idCode = idCode; }

    public UUID getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(UUID idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getCodeOtp() { return codeOtp; }
    public void setCodeOtp(String codeOtp) { this.codeOtp = codeOtp; }

    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    public StatutOtpEnum getStatut() { return statut; }
    public void setStatut(StatutOtpEnum statut) { this.statut = statut; }
}