package com.colisender.api.controller;

import com.colisender.api.model.Utilisateur;
import com.colisender.api.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender; // 💡 Alignement parfait
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

    @PostMapping("/request")
    public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Cet email n'existe pas dans notre système."));
        }

        Utilisateur u = userOpt.get();
        String otp = String.format("%06d", new Random().nextInt(1000000));
    
        u.setPasswordResetOtp(otp);
        u.setOtpExpiryDate(LocalDateTime.now().plusMinutes(5));
        utilisateurRepository.save(u);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(u.getEmail());
            message.setSubject("Code de réinitialisation - Colisender");
            message.setText("Bonjour,\n\nVoici votre code de validation pour modifier votre mot de passe : " 
                            + otp + "\nCe code est valable pendant 5 minutes.\n\nL'équipe Colisender.");
            mailSender.send(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur réseau lors de l'envoi de l'email."));
        }

        return ResponseEntity.ok(Map.of("message", "OTP envoyé avec succès."));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String codeEnvoye = request.get("code"); 

        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Demande invalide."));
        }

        Utilisateur u = userOpt.get();
        if (u.getPasswordResetOtp() == null || !u.getPasswordResetOtp().equals(codeEnvoye)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Code OTP incorrect."));
        }
        if (u.getOtpExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Le code OTP a expiré."));
        }
        return ResponseEntity.ok(Map.of("message", "Code OTP valide."));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String codeEnvoye = request.get("code");
        String nouveauMdp = request.get("nouveau_mot_de_passe"); 

        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Requête invalide."));
        }

        Utilisateur u = userOpt.get();
        if (u.getPasswordResetOtp() == null || !u.getPasswordResetOtp().equals(codeEnvoye) || u.getOtpExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Session expirée ou code invalide."));
        }
        
        u.setMotDePasse(nouveauMdp); 
        u.setPasswordResetOtp(null);
        u.setOtpExpiryDate(null);
        
        utilisateurRepository.save(u);

        return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour avec succès."));
    }
}