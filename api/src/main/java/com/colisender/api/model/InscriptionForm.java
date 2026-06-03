package com.colisender.api.model;

import org.springframework.web.multipart.MultipartFile;

public class InscriptionForm {
    private String prenom;
    private String nom;
    private String email;
    private String motDePasse;
    private String numeroPrincipal;   
    private String numeroSecondaire;  
    private MultipartFile photoRectoCNI;
    private MultipartFile photoVersoCNI;
    private String otp;
    private MultipartFile photoSelfie;

    // Getters et Setters
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    // 💡 Nouveaux Getters/Setters harmonisés
    public String getNumeroPrincipal() { return numeroPrincipal; }
    public void setNumeroPrincipal(String numeroPrincipal) { this.numeroPrincipal = numeroPrincipal; }

    public String getNumeroSecondaire() { return numeroSecondaire; }
    public void setNumeroSecondaire(String numeroSecondaire) { this.numeroSecondaire = numeroSecondaire; }

    public MultipartFile getPhotoRectoCNI() { return photoRectoCNI; }
    public void setPhotoRectoCNI(MultipartFile photoRectoCNI) { this.photoRectoCNI = photoRectoCNI; }

    public MultipartFile getPhotoVersoCNI() { return photoVersoCNI; }
    public void setPhotoVersoCNI(MultipartFile photoVersoCNI) { this.photoVersoCNI = photoVersoCNI; }

    public MultipartFile getPhotoSelfie() { return photoSelfie; }
    public void setPhotoSelfie(MultipartFile photoSelfie) { this.photoSelfie = photoSelfie; }
}