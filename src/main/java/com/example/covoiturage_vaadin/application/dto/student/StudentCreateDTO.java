package com.example.covoiturage_vaadin.application.dto.student;

/**
 * DTO pour créer un nouvel étudiant.
 * ⚠️ CONTIENT le password (uniquement pour l'inscription).
 *
 * Utilisé pour :
 * - Formulaire d'inscription (RegisterView)
 * - Création manuelle par admin (AdminStudentCreationView)
 *
 * Note de sécurité :
 * - Le password sera hashé avec BCrypt avant persistance
 * - Ce DTO ne doit JAMAIS être retourné par une API/Service
 * - Utilisé uniquement en INPUT (création)
 */
public class StudentCreateDTO {
    private String name;
    private String email;
    private String studentCode;
    private String username;
    private String password;  // ⚠️ En clair, sera hashé avec BCrypt

    // Constructeur vide
    public StudentCreateDTO() {}

    // Constructeur complet
    public StudentCreateDTO(String name, String email, String studentCode,
                            String username, String password) {
        this.name = name;
        this.email = email;
        this.studentCode = studentCode;
        this.username = username;
        this.password = password;
    }

    // Getters et Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
