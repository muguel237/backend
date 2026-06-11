package com.colisender.api.service;

import com.colisender.api.model.Colis;
import com.colisender.api.repository.ColisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ColisService {

    @Autowired
    private ColisRepository colisRepository;
   
    public void deleteColis(Long id, Long utilisateurId) {
        Colis colis = colisRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Colis non trouvé"));

        if (!colis.getUtilisateur().getId().equals(utilisateurId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        if (!"EN_ATTENTE".equals(colis.getStatutColis())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de supprimer un colis en cours d'acheminement.");
        }

        colisRepository.delete(colis);
    }

    public Colis updateColis(Long id, Colis colisDetails, Long utilisateurId) {
        Colis colis = colisRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Colis non trouvé"));

        if (!colis.getUtilisateur().getId().equals(utilisateurId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        if (!"EN_ATTENTE".equals(colis.getStatutColis())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce colis ne peut plus être modifié.");
        }

        colis.setDescription(colisDetails.getDescription());
        colis.setPoids(colisDetails.getPoids());
        colis.setAdresseDepart(colisDetails.getAdresseDepart());
        colis.setAdresseArrivee(colisDetails.getAdresseArrivee());

        return colisRepository.save(colis);
    }
}