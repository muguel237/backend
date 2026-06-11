package com.colisender.api.controller;

import com.colisender.api.dto.LoginRequest;
import com.colisender.api.model.Utilisateur;
import com.colisender.api.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("--- DÉBUT TENTATIVE CONNEXION ---");
        
        if (loginRequest == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Requête invalide"));

        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(loginRequest.getEmail());
        
        if (userOpt.isEmpty()) {
            System.out.println("ERREUR : Aucun utilisateur trouvé avec cet email.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou mot de passe incorrect."));
        }

        Utilisateur utilisateur = userOpt.get();
        System.out.println("Utilisateur trouvé : " + utilisateur.getEmail());

        String mdpSaisi = loginRequest.getMot_de_passe();
        String mdpHache = utilisateur.getMotDePasse();
        
        if (mdpSaisi == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Mot de passe requis."));

        boolean matches = passwordEncoder.matches(mdpSaisi, mdpHache);
        
        if (!matches) {
            System.out.println("ERREUR : Mot de passe incorrect.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou mot de passe incorrect."));
        }

        System.out.println("SUCCESS : Connexion validée.");

        return ResponseEntity.ok(Map.of(
            "message", "Connexion réussie",
            "userId", utilisateur.getIdUtilisateur().toString()
        ));
    }
}