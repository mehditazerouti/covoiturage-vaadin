package com.example.covoiturage_vaadin.application.dto.student;

import java.time.LocalDateTime;

/**
 * DTO pour afficher les détails d'un étudiant.
 * ⚠️ NE CONTIENT PAS le mot de passe (sécurité).
 *
 * Utilisé pour :
 * - Afficher le profil d'un étudiant
 * - Liste des étudiants dans AdminStudentView
 * - Détails d'un étudiant en attente de validation
 */
public class StudentDTO {
    private Long id;
    private String name;
    private String email;
    private String studentCode;
    private String username;
    private String role;
    private boolean enabled;
    private boolean approved;
    private LocalDateTime createdAt;

    // Constructeur vide (requis pour certains frameworks)
    public StudentDTO() {}

    // Constructeur complet
    public StudentDTO(Long id, String name, String email, String studentCode,
                      String username, String role, boolean enabled,
                      boolean approved, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.studentCode = studentCode;
        this.username = username;
        this.role = role;
        this.enabled = enabled;
        this.approved = approved;
        this.createdAt = createdAt;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
