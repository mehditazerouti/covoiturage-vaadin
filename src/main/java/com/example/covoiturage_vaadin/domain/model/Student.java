package com.example.covoiturage_vaadin.domain.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
public class Student {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String email;

	@Column(unique = true, nullable = false)
	private String studentCode;

	@Column(unique = true, nullable = false)
	private String username;

	@Column(nullable = false)
	private String password;  // BCrypt hashed

	private String role = "ROLE_USER";  // ROLE_USER ou ROLE_ADMIN

	private boolean enabled = true;

	private boolean approved = false;  // Étudiant validé par un admin (true) ou en attente (false)

	private LocalDateTime createdAt;
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
}
