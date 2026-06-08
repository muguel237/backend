package com.colisender.api.controller;

import com.colisender.api.model.*;
import com.colisender.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/colis")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ColisController {

    @Autowired private ColisRepository colisRepository;
    @Autowired private PhotoColisRepository photoColisRepository;
    @Autowired private UtilisateurRepository utilisateurRepository; // Injecté pour lier l'utilisateur

    private final String UPLOAD_DIR = System.getProperty("user.home") + "/colisender_uploads/colis/";

    @PostMapping
    public Colis createColis(
            @ModelAttribute Colis colis,
            @RequestParam("email") String email, // L'email identifie l'utilisateur
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos
    ) throws IOException {
        
        // 1. On cherche l'utilisateur par son email (la même logique que dans AuthController)
        Utilisateur user = utilisateurRepository.findByEmail(email.toLowerCase().trim())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // 2. On lie dynamiquement l'ID trouvé à l'objet colis
        colis.setIdUtilisateur(user.getIdUtilisateur());
        
        // 3. Sauvegarde : l'ID du colis est généré automatiquement par @GeneratedValue
        Colis savedColis = colisRepository.save(colis);

        // 4. Gestion des photos avec le bon ID colis
        if (photos != null) {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            for (MultipartFile photo : photos) {
                String fileName = "colis_" + savedColis.getIdColis() + "_" + UUID.randomUUID() + ".jpg";
                Files.copy(photo.getInputStream(), uploadPath.resolve(fileName));
                
                PhotoColis pc = new PhotoColis();
                pc.setUrlPhoto(fileName);
                pc.setColis(savedColis); // Le lien JPA se fait avec l'ID généré automatiquement
                photoColisRepository.save(pc);
            }
        }
        return savedColis;
    }
}