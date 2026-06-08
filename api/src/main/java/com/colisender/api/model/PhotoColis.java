package com.colisender.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "photo_colis")
public class PhotoColis {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_photo")
    private UUID idPhoto;

    @Column(name = "url_photo", nullable = false, length = 500)
    private String urlPhoto;

    @Column(name = "date_ajout", nullable = false)
    private LocalDateTime dateAjout = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_colis", nullable = false)
    private Colis colis;

    public PhotoColis() {}

    public UUID getIdPhoto() { return idPhoto; }
    public void setIdPhoto(UUID idPhoto) { this.idPhoto = idPhoto; }
    public String getUrlPhoto() { return urlPhoto; }
    public void setUrlPhoto(String urlPhoto) { this.urlPhoto = urlPhoto; }
    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }
    public Colis getColis() { return colis; }
    public void setColis(Colis colis) { this.colis = colis; }
}