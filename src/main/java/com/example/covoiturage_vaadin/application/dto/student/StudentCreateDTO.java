package com.example.covoiturage_vaadin.application.dto.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
 *
 * Validations JSR-303 :
 * - @NotBlank : Champ obligatoire (non null et non vide)
 * - @Email : Format email valide
 * - @Size : Longueur min/max
 * - @Pattern : Format regex (code étudiant)
 */
public class StudentCreateDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
    private String email;

    @NotBlank(message = "Le code étudiant est obligatoire")
    @Size(min = 5, max = 20, message = "Le code étudiant doit contenir entre 5 et 20 caractères")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Le code étudiant ne doit contenir que des lettres et des chiffres")
    private String studentCode;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Le nom d'utilisateur ne doit contenir que des lettres, chiffres et underscores")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir au moins 6 caractères")
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
