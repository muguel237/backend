package com.colisender.api.controller;

import com.colisender.api.model.*;
import com.colisender.api.repository.*;
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
@CrossOrigin(origins = "*") 
public class AuthController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private VerificationIdentiteRepository verificationIdentiteRepository;

    @Autowired
    private JavaMailSender mailSender;
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/colisender_uploads/";

    // Stockage temporaire en mémoire des codes OTP en attente de validation (Email -> Code)
    private final Map<String, String> tempOtpStore = new HashMap<>();

    /**
     * ÉTAPE 1 : Génération d'un OTP et envoi réel par email
     */
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        // Vérification si l'email existe déjà en base de données
        if (utilisateurRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Cet email possède déjà un compte."));
        }

        // Génération d'un code OTP aléatoire à 6 chiffres
        Random random = new Random();
        String realOtp = String.format("%06d", random.nextInt(1000000));
        
        // Sauvegarde temporaire du code lié à cet email
        tempOtpStore.put(email, realOtp);

        try {
            // Configuration de l'email sortant
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("ton_adresse_gmail@gmail.com"); // À aligner avec ton application.properties
            message.setTo(email);
            message.setSubject("Colisender - Ton code de vérification");
            message.setText("Bonjour,\n\nVoici ton code de vérification à usage unique pour finaliser ton inscription sur Colisender : " 
                            + realOtp + "\n\nCe code est valable pendant 2 minutes.");

            // Envoi de l'email via le serveur SMTP configuré
            mailSender.send(message);
            System.out.println("[SMTP] Code OTP envoyé avec succès à : " + email);

            return ResponseEntity.ok(Map.of(
                "message", "Le code de vérification a été envoyé à votre adresse email.",
                "expiresInSeconds", 120
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Échec de l'envoi de l'email. Vérifiez vos identifiants SMTP ou votre connexion."));
        }
    }

    /**
     * ÉTAPE 2 : Validation du code saisi par l'utilisateur sur l'interface React
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        String validCode = tempOtpStore.get(email);
        if (validCode != null && validCode.equals(code)) {
            return ResponseEntity.ok(Map.of("message", "OTP valide"));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Code OTP incorrect ou expiré."));
    }

    /**
     * ÉTAPE 3 : Inscription complète, écriture des images et liaisons BDD (UUID)
     */
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    @Transactional // Annule tout en base de données si l'écriture d'un fichier échoue en cours de route
    public ResponseEntity<?> register(@ModelAttribute InscriptionForm form) {
        try {
            // Sécurité : double vérification de l'email avant insertion
            if (utilisateurRepository.existsByEmail(form.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Cet email est déjà utilisé."));
            }

        
            Utilisateur u = new Utilisateur();
            u.setNom(form.getNom());
            u.setPrenom(form.getPrenom());
            u.setEmail(form.getEmail());
            u.setMotDePasse(form.getMotDePasse()); 
u.setNumeroPrincipal(form.getNumeroPrincipal()); 
u.setNumeroSecondaire(form.getNumeroSecondaire());
            u.setStatusCompte(StatutCompteEnum.EN_ATTENTE);

            // La base de données PostgreSQL génère ici automatiquement le UUID id_utilisateur
            Utilisateur savedUser = utilisateurRepository.save(u);

            // 2. Écriture physique des fichiers images reçus sur le disque dur
            String pathRecto = saveToDisk(form.getPhotoRectoCNI(), "recto_");
            String pathVerso = saveToDisk(form.getPhotoVersoCNI(), "verso_");
            String pathSelfie = saveToDisk(form.getPhotoSelfie(), "selfie_");

            // 3. Création de la ligne correspondante dans verification_identite
            VerificationIdentite v = new VerificationIdentite();
            v.setIdUtilisateur(savedUser.getIdUtilisateur()); // Clé étrangère basée sur le UUID généré au-dessus !
            v.setPhotoRectoCNI(pathRecto);
            v.setPhotoVersoCNI(pathVerso);
            v.setPhotoSelfie(pathSelfie);
            v.setStatutVerification(StatutVerifEnum.EN_ATTENTE);

            verificationIdentiteRepository.save(v);

            // Inscription terminée, on supprime l'OTP de la mémoire volatile
            tempOtpStore.remove(form.getEmail());

            return ResponseEntity.ok(Map.of("message", "Inscription complète réussie ! Compte en attente de vérification."));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur technique lors du traitement ou du stockage des images."));
        }
    }

    /**
     * Méthode utilitaire privée : Copie un fichier MultipartFile vers le stockage local
     */
    private String saveToDisk(MultipartFile file, String prefix) throws IOException {
        if (file == null || file.isEmpty()) return null;

        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs(); // Crée le dossier "colisender_uploads" s'il n'existe pas encore
        }

        // Création d'un nom de fichier unique pour éviter les écrasements (prefix + UUID + nom original)
        String fileName = prefix + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.write(filePath, file.getBytes());

        return filePath.toString(); // Renvoie le chemin absolu du fichier à stocker dans PostgreSQL
    }
}