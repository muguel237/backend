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

    @Column(name = "id_utilisateur", nullable = false)
    private UUID idUtilisateur;

    private String description;
    private BigDecimal poids;
    private BigDecimal dimension;
    private String villeDepart;
    private String villeArrive;
    private String adresseRecuperation;
    private String adresseLivraison;
    private String statut = "EN_ATTENTE";
    private BigDecimal prixTransport;
    private LocalDate dateCreation = LocalDate.now();
    private String telephoneDestinataire;

    public Colis() {}

    public UUID getIdColis() { return idColis; }
    public void setIdColis(UUID idColis) { this.idColis = idColis; }
    public UUID getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(UUID idUtilisateur) { this.idUtilisateur = idUtilisateur; }
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
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public BigDecimal getPrixTransport() { return prixTransport; }
    public void setPrixTransport(BigDecimal prixTransport) { this.prixTransport = prixTransport; }
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    public String getTelephoneDestinataire() { return telephoneDestinataire; }
    public void setTelephoneDestinataire(String telephoneDestinataire) { this.telephoneDestinataire = telephoneDestinataire; }
}