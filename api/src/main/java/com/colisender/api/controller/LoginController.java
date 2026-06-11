package com.colisender.api.controller;

import com.colisender.api.dto.LoginRequest;
import com.colisender.api.model.Utilisateur;
import com.colisender.api.repository.UtilisateurRepository;
import com.colisender.api.service.JwtService; // Assurez-vous que le chemin est correct
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class LoginController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService; // Injection nécessaire pour générer le token

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("--- DÉBUT TENTATIVE CONNEXION ---");
        
        if (loginRequest == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Requête invalide"));
        }

        // 1. Chercher l'utilisateur
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(loginRequest.getEmail().toLowerCase().trim());
        
        if (userOpt.isEmpty()) {
            System.out.println("ERREUR : Aucun utilisateur trouvé.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou mot de passe incorrect."));
        }

        Utilisateur utilisateur = userOpt.get();
        System.out.println("Utilisateur trouvé : " + utilisateur.getEmail());

        // 2. Vérifier le mot de passe
        String mdpSaisi = loginRequest.getMot_de_passe();
        String mdpHache = utilisateur.getMotDePasse();
        
        if (mdpSaisi == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Mot de passe requis."));
        }

        boolean matches = passwordEncoder.matches(mdpSaisi, mdpHache);
        
        if (!matches) {
            System.out.println("ERREUR : Mot de passe incorrect.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou mot de passe incorrect."));
        }

        // 3. Génération du token JWT
        String token = jwtService.generateToken(utilisateur.getEmail());
        
        // 4. Construction de la réponse JSON avec le token
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", utilisateur.getEmail());
        response.put("message", "Connexion réussie");

        System.out.println("SUCCESS : Connexion validée, token généré pour " + utilisateur.getEmail());
        
        return ResponseEntity.ok(response);
    }
}