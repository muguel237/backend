package com.colisender.api.controller;

import com.colisender.api.model.*;
import com.colisender.api.repository.*;
import com.colisender.api.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private VerificationIdentiteRepository verificationIdentiteRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VerificationService verificationService;

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/colisender_uploads/";
    private final Map<String, String> tempOtpStore = new HashMap<>();

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Email manquant."));
        }

        String normalizedEmail = email.toLowerCase().trim();
        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        tempOtpStore.put(normalizedEmail, otp);
        System.out.println("DEBUG - OTP généré pour " + normalizedEmail + " : " + otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(normalizedEmail);
        message.setSubject("Votre code de vérification ColiSender");
        message.setText("Votre code OTP est : " + otp);
        mailSender.send(message);

        return ResponseEntity.ok(Map.of("message", "OTP envoyé avec succès."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email").toLowerCase().trim();
        String otp = request.get("otp");
        String storedOtp = tempOtpStore.get(email);

        System.out.println("DEBUG - Vérification OTP pour " + email + " | Reçu: [" + otp + "] vs Stocké: [" + storedOtp + "]");

        if (storedOtp != null && storedOtp.equals(otp)) {
            return ResponseEntity.ok(Map.of("message", "OTP valide."));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Code OTP incorrect."));
        }
    }

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    @Transactional
    public ResponseEntity<?> register(@ModelAttribute InscriptionForm form) {
        String email = form.getEmail().toLowerCase().trim();
        
        // La vérification OTP est maintenant faite via /verify-otp avant d'appeler /register
        try {
            // 1. OCR & Vérification nom
            String ocrResult = verificationService.extraireNom(form.getPhotoRectoCNI());
            String nomOcrNettoye = nettoyerTexte(ocrResult);
            String nomSaisiNettoye = nettoyerTexte(form.getNom());

            if (!nomOcrNettoye.contains(nomSaisiNettoye)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Le nom sur la CNI ne correspond pas."));
            }

            // 2. Création utilisateur
            Utilisateur u = new Utilisateur();
            u.setNom(form.getNom());
            u.setPrenom(form.getPrenom());
            u.setEmail(email);
            u.setMotDePasse(form.getMotDePasse());
            u.setNumeroPrincipal(form.getNumeroPrincipal());
            u.setNumeroSecondaire(form.getNumeroSecondaire());
            u.setStatusCompte(StatutCompteEnum.EN_ATTENTE);
            Utilisateur savedUser = utilisateurRepository.save(u);

            // 3. Sauvegarde des fichiers
            String pathRecto = saveToDisk(form.getPhotoRectoCNI(), "recto_");
            String pathVerso = saveToDisk(form.getPhotoVersoCNI(), "verso_");
            String pathSelfie = saveToDisk(form.getPhotoSelfie(), "selfie_");

            VerificationIdentite v = new VerificationIdentite();
            v.setIdUtilisateur(savedUser.getIdUtilisateur());
            v.setPhotoRectoCNI(pathRecto);
            v.setPhotoVersoCNI(pathVerso);
            v.setPhotoSelfie(pathSelfie);
            v.setStatutVerification(StatutVerifEnum.EN_ATTENTE);
            verificationIdentiteRepository.save(v);

            // Nettoyage de l'OTP après inscription réussie
            tempOtpStore.remove(email);
            
            return ResponseEntity.ok(Map.of("message", "Inscription réussie !"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur serveur : " + e.getMessage()));
        }
    }

    private String nettoyerTexte(String texte) {
        return (texte == null) ? "" : texte.toUpperCase().replaceAll("[^A-Z]", "");
    }

    private String saveToDisk(MultipartFile file, String prefix) throws IOException {
        if (file == null || file.isEmpty()) return null;
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) directory.mkdirs();
        String fileName = prefix + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.write(filePath, file.getBytes());
        return filePath.toString();
    }
}
