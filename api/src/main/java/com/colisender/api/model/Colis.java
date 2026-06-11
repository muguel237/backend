package com.colisender.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "colis")
public class Colis {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_colis")
    private UUID idColis;

    // Colonne pour la base de données
    @Column(name = "id_utilisateur", nullable = false)
    private UUID idUtilisateur;

    // Relation JPA pour permettre colis.setUtilisateur(user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    private String description;
    private BigDecimal poids;
    private BigDecimal dimension;
    private String villeDepart;
    private String villeArrive;
    private String adresseRecuperation;
    private String adresseLivraison;
    
    @Column(name = "statut_colis")
    private String statutColis = "EN_ATTENTE";
    
    private BigDecimal prixTransport;
    private LocalDate dateCreation = LocalDate.now();
    private String telephoneDestinataire;

    public Colis() {}

    // Getters et Setters
    public UUID getIdColis() { return idColis; }
    public void setIdColis(UUID idColis) { this.idColis = idColis; }

    public UUID getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(UUID idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { 
        this.utilisateur = utilisateur; 
        if (utilisateur != null) {
            this.idUtilisateur = utilisateur.getIdUtilisateur();
        }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPoids() { return poids; }
    public void setPoids(BigDecimal poids) { this.poids = poids; }

    public BigDecimal getDimension() { return dimension; }
    public void setDimension(BigDecimal dimension) { this.dimension = dimension; }

    public String getVilleDepart() { return villeDepart; }
    public void setVilleDepart(String villeDepart) { this.villeDepart = villeDepart; }

    public String getVilleArrive() { return villeArrive; }
    public void setVilleArrive(String villeArrive) { this.villeArrive = villeArrive; }

    public String getAdresseRecuperation() { return adresseRecuperation; }
    public void setAdresseRecuperation(String adresseRecuperation) { this.adresseRecuperation = adresseRecuperation; }

    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }

    public String getStatutColis() { return statutColis; }
    public void setStatutColis(String statutColis) { this.statutColis = statutColis; }

    public BigDecimal getPrixTransport() { return prixTransport; }
    public void setPrixTransport(BigDecimal prixTransport) { this.prixTransport = prixTransport; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public String getTelephoneDestinataire() { return telephoneDestinataire; }
    public void setTelephoneDestinataire(String telephoneDestinataire) { this.telephoneDestinataire = telephoneDestinataire; }
}