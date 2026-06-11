package com.colisender.api.controller;

import com.colisender.api.model.*;
import com.colisender.api.repository.*;
import com.colisender.api.service.ColisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/colis")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ColisController {

    @Autowired private ColisRepository colisRepository;
    @Autowired private PhotoColisRepository photoColisRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private ColisService colisService;

    private final String UPLOAD_DIR = System.getProperty("user.home") + "/colisender_uploads/colis/";

    @GetMapping("/mes-colis")
    public List<Colis> getMesColis(Principal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        
        Utilisateur user = utilisateurRepository.findByEmail(principal.getName().toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        return colisRepository.findByIdUtilisateur(user.getIdUtilisateur());
    }

   
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteColis(@PathVariable Long id, Principal principal) {
        Long userId = getUtilisateurId(principal);
        colisService.deleteColis(id, userId);
        return ResponseEntity.noContent().build();
    }

   
    @PutMapping("/{id}")
    public ResponseEntity<Colis> modifierColis(@PathVariable Long id, @RequestBody Colis colisDetails, Principal principal) {
        Long userId = getUtilisateurId(principal);
        Colis colisMisAJour = colisService.updateColis(id, colisDetails, userId);
        return ResponseEntity.ok(colisMisAJour);
    }

    @PostMapping
    public Colis createColis(
            @ModelAttribute Colis colis,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            Principal principal 
    ) throws IOException {
        
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        
        Utilisateur user = utilisateurRepository.findByEmail(principal.getName().toLowerCase().trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        colis.setUtilisateur(user);
        colis.setIdUtilisateur(user.getIdUtilisateur());
        colis.setStatutColis("EN_ATTENTE");
        
        Colis savedColis = colisRepository.save(colis);

        if (photos != null && !photos.isEmpty()) {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            for (MultipartFile photo : photos) {
                String fileName = "colis_" + savedColis.getIdColis() + "_" + UUID.randomUUID() + ".jpg";
                Files.copy(photo.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                
                PhotoColis pc = new PhotoColis();
                pc.setUrlPhoto(fileName);
                pc.setColis(savedColis);
                photoColisRepository.save(pc);
            }
        }
        return savedColis;
    }
    private Long getUtilisateurId(Principal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return utilisateurRepository.findByEmail(principal.getName().toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .getIdUtilisateur();
    }
}