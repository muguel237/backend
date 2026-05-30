package com.colisender.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_utilisateur", updatable = false, nullable = false)
    private UUID idUtilisateur;

    private String nom;
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(name = "numero_principal", nullable = false)
    private String numeroPrincipal;

    @Column(name = "numero_secondaire")
    private String numeroSecondaire;

    @Column(name = "photo_profil")
    private String photoProfil;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_compte", nullable = false)
    private StatutCompteEnum statusCompte = StatutCompteEnum.EN_ATTENTE;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    // 💡 AJOUT : Ces deux colonnes manquantes pour faire fonctionner l'OTP du mot de passe oublié
    @Column(name = "password_reset_otp")
    private String passwordResetOtp;

    @Column(name = "otp_expiry_date")
    private LocalDateTime otpExpiryDate;

    // --- GETTERS ET SETTERS EXISTANTS ---
    public UUID getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(UUID idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getNumeroPrincipal() { return numeroPrincipal; }
    public void setNumeroPrincipal(String numeroPrincipal) { this.numeroPrincipal = numeroPrincipal; }

    public String getNumeroSecondaire() { return numeroSecondaire; }
    public void setNumeroSecondaire(String numeroSecondaire) { this.numeroSecondaire = numeroSecondaire; }

    public String getPhotoProfil() { return photoProfil; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    public StatutCompteEnum getStatusCompte() { return statusCompte; }
    public void setStatusCompte(StatutCompteEnum statusCompte) { this.statusCompte = statusCompte; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    // --- 💡 AJOUT : Getters et Setters pour l'OTP ---
    public String getPasswordResetOtp() { return passwordResetOtp; }
    public void setPasswordResetOtp(String passwordResetOtp) { this.passwordResetOtp = passwordResetOtp; }

    public LocalDateTime getOtpExpiryDate() { return otpExpiryDate; }
    public void setOtpExpiryDate(LocalDateTime otpExpiryDate) { this.otpExpiryDate = otpExpiryDate; }
}