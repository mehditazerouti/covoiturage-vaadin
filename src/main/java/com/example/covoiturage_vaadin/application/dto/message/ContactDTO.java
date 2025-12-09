package com.example.covoiturage_vaadin.application.dto.message;

/**
 * DTO pour afficher un contact éligible à la messagerie.
 *
 * Utilisé pour :
 * - Afficher la liste des utilisateurs contactables dans NewMessageDialog
 * - Sélectionner un destinataire pour un nouveau message
 *
 * Particularités :
 * - Contient le contexte du covoiturage partagé
 * - Utilisé uniquement pour les utilisateurs avec qui on a partagé un trajet
 */
public class ContactDTO {
    private Long id;
    private String name;
    private String email;
    private String avatar;
    private String tripContext; // Ex: "Conducteur sur Paris → Lyon" ou "Passager sur Marseille → Nice"

    // Constructeur vide
    public ContactDTO() {}

    // Constructeur complet
    public ContactDTO(Long id, String name, String email, String avatar, String tripContext) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.tripContext = tripContext;
    }

    // Constructeur sans contexte
    public ContactDTO(Long id, String name, String email, String avatar) {
        this(id, name, email, avatar, null);
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTripContext() {
        return tripContext;
    }

    public void setTripContext(String tripContext) {
        this.tripContext = tripContext;
    }

    // Méthodes utilitaires

    /**
     * Vérifie si un contexte de trajet est disponible
     */
    public boolean hasTripContext() {
        return tripContext != null && !tripContext.isEmpty();
    }
}
