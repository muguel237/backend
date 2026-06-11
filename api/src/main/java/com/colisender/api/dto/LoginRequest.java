package com.colisender.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {
    private String email;

    @JsonProperty("mot_de_passe") // C'est crucial : cela force le mapping du JSON vers cette variable
    private String mot_de_passe;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMot_de_passe() { return mot_de_passe; }
    public void setMot_de_passe(String mot_de_passe) { this.mot_de_passe = mot_de_passe; }
}
