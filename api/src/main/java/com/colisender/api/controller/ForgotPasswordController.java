package com.colisender.api.controller;

import com.colisender.api.model.Utilisateur;
import com.colisender.api.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth/forgot-password")
@CrossOrigin(origins = "*")
public class ForgotPasswordController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private JavaMailSender mailSender;

    // Même encoder que dans AuthController — facteur 12
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    // =========================================================================
    // ENDPOINT 1 : DEMANDER UN OTP DE RÉINITIALISATION
    // POST /api/auth/forgot-password/request
    // Corps JSON : { "email": "user@gmail.com" }
    // =========================================================================
    @PostMapping("/request")
    public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Cet email n'existe pas dans notre système."));
        }

        Utilisateur u = userOpt.get();
        String otp = String.format("%06d", new Random().nextInt(1_000_000));

        // Stocker l'OTP HACHÉ dans la table utilisateur (colonnes déjà existantes)
        String otpHache = passwordEncoder.encode(otp);
        u.setPasswordResetOtp(otpHache);  // stocké haché, plus en clair
        u.setOtpExpiryDate(LocalDateTime.now().plusMinutes(5));
        utilisateurRepository.save(u);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(u.getEmail());
            message.setSubject("Code de réinitialisation - Colisender");
            message.setText(
                "Bonjour,\n\nVoici votre code pour modifier votre mot de passe : "
                + otp   // on envoie le code EN CLAIR par email
                + "\nCe code est valable pendant 5 minutes.\n\nL'équipe Colisender."
            );
            mailSender.send(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de l'envoi de l'email."));
        }

        return ResponseEntity.ok(Map.of("message", "OTP envoyé avec succès."));
    }

    // =========================================================================
    // ENDPOINT 2 : VÉRIFIER L'OTP DE RÉINITIALISATION
    // POST /api/auth/forgot-password/verify
    // Corps JSON : { "email": "user@gmail.com", "code": "123456" }
    // =========================================================================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email      = request.get("email");
        String codeEnvoye = request.get("code");

        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Demande invalide."));
        }

        Utilisateur u = userOpt.get();

        if (u.getPasswordResetOtp() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Aucune demande de réinitialisation en cours."));
        }

        // Vérifier l'expiration AVANT de vérifier le code
        if (u.getOtpExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Le code OTP a expiré. Faites une nouvelle demande."));
        }

        // Comparer le code saisi avec le hash stocké
        boolean codeValide = passwordEncoder.matches(codeEnvoye, u.getPasswordResetOtp());
        if (!codeValide) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Code OTP incorrect."));
        }

        return ResponseEntity.ok(Map.of("message", "Code OTP valide. Vous pouvez définir un nouveau mot de passe."));
    }

    // =========================================================================
    // ENDPOINT 3 : RÉINITIALISER LE MOT DE PASSE
    // POST /api/auth/forgot-password/reset
    // Corps JSON : { "email": "user@gmail.com", "code": "123456", "nouveau_mot_de_passe": "NouveauMdp1" }
    // =========================================================================
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email          = request.get("email");
        String codeEnvoye     = request.get("code");
        String nouveauMdp     = request.get("nouveau_mot_de_passe");

        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Requête invalide."));
        }

        Utilisateur u = userOpt.get();

        // Revérifier le code ET l'expiration
        if (u.getPasswordResetOtp() == null
                || u.getOtpExpiryDate().isBefore(LocalDateTime.now())
                || !passwordEncoder.matches(codeEnvoye, u.getPasswordResetOtp())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Session expirée ou code invalide."));
        }

        // HACHER le nouveau mot de passe avant de le stocker
        String nouveauMdpHache = passwordEncoder.encode(nouveauMdp);
        u.setMotDePasse(nouveauMdpHache);

        // Nettoyer l'OTP de réinitialisation
        u.setPasswordResetOtp(null);
        u.setOtpExpiryDate(null);

        utilisateurRepository.save(u);

        return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour avec succès."));
    }
}