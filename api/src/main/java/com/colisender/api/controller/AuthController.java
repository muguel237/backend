package com.colisender.api.controller;

import com.colisender.api.model.*;
import com.colisender.api.repository.*;
import com.colisender.api.service.VerificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private VerificationIdentiteRepository verificationIdentiteRepository;

    @Autowired
    private OtpRepository otpRepository;           // NOUVEAU : repository de la table otp

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VerificationService verificationService;

    // BCryptPasswordEncoder — facteur de coût 12 (bon équilibre sécurité/performance)
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/colisender_uploads/";

    // =========================================================================
    // ENDPOINT 1 : ENVOI DE L'OTP PAR EMAIL (pour l'inscription)
    // POST /api/auth/send-otp
    // Corps JSON : { "email": "user@gmail.com" }
    // =========================================================================
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email manquant."));
        }

        String normalizedEmail = email.toLowerCase().trim();

        // Vérifier si l'utilisateur existe déjà avec ce compte ACTIF
        Optional<Utilisateur> existingUser = utilisateurRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent() && existingUser.get().getStatusCompte() == StatutCompteEnum.ACTIF) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Un compte actif existe déjà avec cet email."));
        }

        // Générer le code OTP en clair (6 chiffres)
        String otpEnClair = String.format("%06d", new Random().nextInt(1_000_000));

        // --- NOUVEAU : Traitement lié à la BD ---
        // Si l'utilisateur EN_ATTENTE existe déjà, utiliser son ID
        // Sinon créer un utilisateur temporaire EN_ATTENTE
        Utilisateur utilisateur;
        if (existingUser.isPresent()) {
            utilisateur = existingUser.get();
        } else {
            // Créer un utilisateur "placeholder" EN_ATTENTE pour lier l'OTP
            // Il sera complété lors du /register
            Utilisateur temp = new Utilisateur();
            temp.setEmail(normalizedEmail);
            temp.setNom("EN_ATTENTE");
            temp.setPrenom("EN_ATTENTE");
            temp.setMotDePasse(passwordEncoder.encode(UUID.randomUUID().toString())); // mdp provisoire haché
            temp.setNumeroPrincipal("00000000");
            temp.setStatusCompte(StatutCompteEnum.EN_ATTENTE);
            utilisateur = utilisateurRepository.save(temp);
        }

        // Invalider tous les OTP actifs précédents pour cet utilisateur
        otpRepository.expireAllActiveOtpsForUser(utilisateur.getIdUtilisateur());

        // Hacher l'OTP avant de le stocker (sécurité : si la BD est compromise)
        String otpHache = passwordEncoder.encode(otpEnClair);

        Otp otpEntity = new Otp();
        otpEntity.setIdUtilisateur(utilisateur.getIdUtilisateur());
        otpEntity.setCodeOtp(otpHache);                                    // stocké haché
        otpEntity.setDateExpiration(LocalDateTime.now().plusMinutes(10));   // valide 10 minutes
        otpEntity.setStatut(StatutOtpEnum.ACTIF);
        otpRepository.save(otpEntity);   // SAUVEGARDE EN BD

        // Envoyer l'OTP EN CLAIR par email (l'utilisateur reçoit le code lisible)
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(normalizedEmail);
            message.setSubject("Votre code de vérification ColiSender");
            message.setText(
                "Bonjour,\n\nVotre code OTP est : " + otpEnClair +
                "\n\nCe code est valable pendant 2 minutes.\n\nL'équipe Colisender."
            );
            mailSender.send(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de l'envoi de l'email : " + e.getMessage()));
        }

        System.out.println("DEBUG - OTP généré pour " + normalizedEmail + " : " + otpEnClair + " (stocké haché en BD)");
        return ResponseEntity.ok(Map.of("message", "OTP envoyé et sauvegardé en base de données."));
    }

    // =========================================================================
    // ENDPOINT 2 : VÉRIFICATION DE L'OTP
    // POST /api/auth/verify-otp
    // Corps JSON : { "email": "user@gmail.com", "otp": "123456" }
    // =========================================================================
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email").toLowerCase().trim();
        String otpSaisi = request.get("otp");

        // Récupérer l'utilisateur par email
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Email non trouvé."));
        }

        Utilisateur utilisateur = userOpt.get();

        // Récupérer les OTP actifs de cet utilisateur depuis la BD
        List<Otp> otpsActifs = otpRepository.findByIdUtilisateurAndStatut(
                utilisateur.getIdUtilisateur(), StatutOtpEnum.ACTIF
        );

        if (otpsActifs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Aucun OTP actif. Demandez un nouveau code."));
        }

        // Prendre le plus récent (dernier sauvegardé)
        Otp dernierOtp = otpsActifs.get(otpsActifs.size() - 1);

        // Vérifier l'expiration
        if (dernierOtp.getDateExpiration().isBefore(LocalDateTime.now())) {
            dernierOtp.setStatut(StatutOtpEnum.EXPIRE);
            otpRepository.save(dernierOtp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Code OTP expiré. Demandez un nouveau code."));
        }

        // Comparer le code saisi avec le hash stocké (BCrypt)
        boolean otpValide = passwordEncoder.matches(otpSaisi, dernierOtp.getCodeOtp());

        if (!otpValide) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Code OTP incorrect."));
        }

        // Marquer l'OTP comme utilisé (ne peut plus servir)
        dernierOtp.setStatut(StatutOtpEnum.UTILISE);
        otpRepository.save(dernierOtp);

        return ResponseEntity.ok(Map.of("message", "OTP valide. Vous pouvez finaliser l'inscription."));
    }

    // =========================================================================
    // ENDPOINT 3 : INSCRIPTION COMPLÈTE
    // POST /api/auth/register  (multipart/form-data)
    // Champs : prenom, nom, email, motDePasse, numeroPrincipal, numeroSecondaire,
    //          photoRectoCNI (fichier), photoVersoCNI (fichier), photoSelfie (fichier)
    // =========================================================================
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    @Transactional
    public ResponseEntity<?> register(@ModelAttribute InscriptionForm form) {
        String email = form.getEmail().toLowerCase().trim();

        // Vérifier que l'email existe déjà (inscrit en EN_ATTENTE lors du send-otp)
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Aucune demande d'inscription trouvée. Recommencez."));
        }

        // Vérifier qu'il n'y a pas déjà un compte ACTIF
        if (userOpt.get().getStatusCompte() == StatutCompteEnum.ACTIF) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Un compte actif existe déjà avec cet email."));
        }

        try {
            // --- VÉRIFICATION OCR ---
            String ocrResult = verificationService.extraireNom(form.getPhotoRectoCNI());
            String nomOcrNettoye = nettoyerTexte(ocrResult);
            String nomSaisiNettoye = nettoyerTexte(form.getNom());

            if (!nomOcrNettoye.contains(nomSaisiNettoye)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Le nom sur la CNI ne correspond pas au nom saisi."));
            }

            // --- MISE À JOUR DE L'UTILISATEUR (existait en EN_ATTENTE) ---
            Utilisateur u = userOpt.get();
            u.setNom(form.getNom());
            u.setPrenom(form.getPrenom());
            u.setNumeroPrincipal(form.getNumeroPrincipal());
            u.setNumeroSecondaire(form.getNumeroSecondaire());

            // HACHAGE DU MOT DE PASSE — jamais stocké en clair
            String motDePasseHache = passwordEncoder.encode(form.getMotDePasse());
            u.setMotDePasse(motDePasseHache);

            u.setStatusCompte(StatutCompteEnum.ACTIF); // Compte activé après vérification
            Utilisateur savedUser = utilisateurRepository.save(u);

            // --- SAUVEGARDE DES PHOTOS ---
            String pathRecto  = saveToDisk(form.getPhotoRectoCNI(), "recto_");
            String pathVerso  = saveToDisk(form.getPhotoVersoCNI(), "verso_");
            String pathSelfie = saveToDisk(form.getPhotoSelfie(),   "selfie_");

            // --- ENREGISTREMENT DE LA VÉRIFICATION D'IDENTITÉ ---
            // Vérifier si une entrée existe déjà (lors d'une nouvelle tentative)
            VerificationIdentite v = new VerificationIdentite();
            v.setIdUtilisateur(savedUser.getIdUtilisateur());
            v.setPhotoRectoCNI(pathRecto);
            v.setPhotoVersoCNI(pathVerso);
            v.setPhotoSelfie(pathSelfie);
            v.setStatutVerification(StatutVerifEnum.EN_ATTENTE);
            verificationIdentiteRepository.save(v);

            return ResponseEntity.ok(Map.of(
                    "message", "Inscription réussie ! Mot de passe haché et stocké de façon sécurisée.",
                    "userId", savedUser.getIdUtilisateur().toString()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur serveur : " + e.getMessage()));
        }
    }

    // =========================================================================
    // ENDPOINT 4 : CONNEXION
    // POST /api/auth/login
    // Corps JSON : { "email": "user@gmail.com", "motDePasse": "MonMotDePasse123" }
    // =========================================================================
// =========================================================================
//   @PostMapping("/login")
// public ResponseEntity<?> login(@RequestBody Map<String, Object> request) {
//     // 1. Log : Ce qu'on reçoit du client
//     System.out.println("DEBUG LOGIN - Corps reçu : " + request);

//     String email = request.get("email") != null ? request.get("email").toString() : null;
    
//     String motDePasse = null;
//     if (request.get("mot_de_passe") != null) motDePasse = request.get("mot_de_passe").toString();
//     else if (request.get("motDePasse") != null) motDePasse = request.get("motDePasse").toString();
//     else if (request.get("password") != null) motDePasse = request.get("password").toString();

//     // 2. Validation
//     if (email == null || motDePasse == null || motDePasse.isEmpty()) {
//         System.out.println("DEBUG LOGIN - ERREUR : Champs vides");
//         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                 .body(Map.of("error", "CHAMP_MANQUANT", "message", "Veuillez remplir tous les champs."));
//     }

//     // 3. Recherche
//     Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email.toLowerCase().trim());
    
//     if (userOpt.isEmpty()) {
//         System.out.println("DEBUG LOGIN - ERREUR : Email non trouvé [" + email + "]");
//         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                 .body(Map.of("error", "EMAIL_INCONNU", "message", "Aucun compte associé à cet email."));
//     }

//     Utilisateur u = userOpt.get();
//     System.out.println("DEBUG LOGIN - Utilisateur trouvé : " + u.getEmail() + " | Statut : " + u.getStatusCompte());

//     // 4. Statut
//     if (u.getStatusCompte() != StatutCompteEnum.ACTIF) {
//         System.out.println("DEBUG LOGIN - ERREUR : Statut non actif pour " + u.getEmail());
//         return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                 .body(Map.of("error", "COMPTE_NON_ACTIF", "message", "Le compte est en attente ou désactivé."));
//     }
    
//     // 5. Comparaison Mdp
//     boolean estValide = passwordEncoder.matches(motDePasse, u.getMotDePasse());
//     System.out.println("DEBUG LOGIN - Comparaison mot de passe :");
//     System.out.println("    Saisi : [" + motDePasse + "]");
//     System.out.println("    Hash en base : [" + u.getMotDePasse() + "]");
//     System.out.println("    Résultat matches() : " + estValide);

//     if (!estValide) {
//         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                 .body(Map.of("error", "MOT_DE_PASSE_INCORRECT", "message", "Le mot de passe saisi est incorrect."));
//     }

//     System.out.println("DEBUG LOGIN - Succès pour : " + u.getEmail());
//     return ResponseEntity.ok(Map.of("message", "Connexion réussie.", "userId", u.getIdUtilisateur()));
// }
    // =========================================================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // =========================================================================

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
