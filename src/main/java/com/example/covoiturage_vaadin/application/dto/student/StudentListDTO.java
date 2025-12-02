package com.example.covoiturage_vaadin.application.dto.student;

/**
 * DTO minimal pour afficher un étudiant dans une liste.
 *
 * Utilisé pour :
 * - Afficher le conducteur dans TripDTO
 * - Afficher l'étudiant dans BookingDTO
 * - Toute liste où seules les infos de base sont nécessaires
 *
 * Avantages :
 * - Réduit la taille des réponses (pas de champs inutiles)
 * - Évite les références circulaires (Booking → Trip → Student)
 * - Améliore les performances
 */
public class StudentListDTO {
    private Long id;
    private String name;
    private String email;

    // Constructeur vide
    public StudentListDTO() {}

    // Constructeur complet
    public StudentListDTO(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
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
}
