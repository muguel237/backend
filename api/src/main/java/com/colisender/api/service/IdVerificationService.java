package com.colisender.api.service;

import com.colisender.api.model.*;
import com.colisender.api.repository.VerificationIdentiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IdVerificationService {

    private final VerificationService verificationService; // Ton service OCR
    private final VerificationIdentiteRepository repository;

    public IdVerificationService(VerificationService verificationService, VerificationIdentiteRepository repository) {
        this.verificationService = verificationService;
        this.repository = repository;
    }

    // Nouvelle logique simplifiée
    public boolean validerDossier(MultipartFile recto, String nomSaisi) throws Exception {
        // 1. Appel à l'OCR
        String ocrResult = verificationService.extraireNom(recto);
        
        // 2. Logique de comparaison textuelle
        return ocrResult.toUpperCase().contains(nomSaisi.toUpperCase());
    }
}