package com.example.covoiturage_vaadin.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant un étudiant/utilisateur.
 *
 * Validations JSR-303 appliquées sur les champs pour garantir
 * l'intégrité des données au niveau de la couche domaine.
 */
@Entity
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Le nom est obligatoire")
	@Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
	@Column(nullable = false)
	private String name;

	@NotBlank(message = "L'email est obligatoire")
	@Email(message = "L'email doit être valide")
	@Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
	@Column(unique = true, nullable = false)
	private String email;

	@NotBlank(message = "Le code étudiant est obligatoire")
	@Size(min = 5, max = 20, message = "Le code étudiant doit contenir entre 5 et 20 caractères")
	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "Le code étudiant ne doit contenir que des lettres et des chiffres")
	@Column(unique = true, nullable = false)
	private String studentCode;

	@NotBlank(message = "Le nom d'utilisateur est obligatoire")
	@Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
	@Column(unique = true, nullable = false)
	private String username;

	@NotBlank(message = "Le mot de passe est obligatoire")
	@Column(nullable = false)
	private String password;  // BCrypt hashed

	@Pattern(regexp = "^ROLE_(USER|ADMIN)$", message = "Le rôle doit être ROLE_USER ou ROLE_ADMIN")
	private String role = "ROLE_USER";  // ROLE_USER ou ROLE_ADMIN

	private boolean enabled = true;

	private boolean approved = false;  // Étudiant validé par un admin (true) ou en attente (false)

	private LocalDateTime createdAt;

	@Pattern(regexp = "^(USER|MALE|FEMALE)$", message = "L'avatar doit être USER, MALE ou FEMALE")
	private String avatar = "USER";  // Icône d'avatar : USER, MALE, FEMALE (VaadinIcon)
	// getters/setters
	
	// Constructeur vide (obligatoire pour JPA)
    public Student() {
    }
    
    // Constructeur avec paramètres (optionnel)
    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public Student(Long id, String name, String email) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
