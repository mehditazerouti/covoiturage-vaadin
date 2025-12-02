package com.example.covoiturage_vaadin.application.dto.student;

import java.time.LocalDateTime;

/**
 * DTO pour afficher le profil complet d'un étudiant avec statistiques.
 *
 * Utilisé pour :
 * - Afficher le profil utilisateur dans ProfileDialog
 * - Statistiques : nombre de trajets proposés, nombre de réservations
 *
 * ⚠️ NE CONTIENT PAS le mot de passe (sécurité).
 */
public class ProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String studentCode;
    private String username;
    private String avatar;  // Icône Vaadin : USER, MALE, FEMALE
    private LocalDateTime createdAt;

    // Statistiques
    private long tripsCount;       // Nombre de trajets proposés (en tant que conducteur)
    private long bookingsCount;    // Nombre de réservations effectuées (en tant que passager)

    // Constructeur vide
    public ProfileDTO() {}

    // Constructeur complet
    public ProfileDTO(Long id, String name, String email, String studentCode,
                     String username, String avatar, LocalDateTime createdAt,
                     long tripsCount, long bookingsCount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.studentCode = studentCode;
        this.username = username;
        this.avatar = avatar;
        this.createdAt = createdAt;
        this.tripsCount = tripsCount;
        this.bookingsCount = bookingsCount;
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

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getTripsCount() {
        return tripsCount;
    }

    public void setTripsCount(long tripsCount) {
        this.tripsCount = tripsCount;
    }

    public long getBookingsCount() {
        return bookingsCount;
    }

    public void setBookingsCount(long bookingsCount) {
        this.bookingsCount = bookingsCount;
    }
}
