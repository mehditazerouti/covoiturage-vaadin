package com.example.covoiturage_vaadin.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

/**
 * Entité représentant un code étudiant autorisé (whitelist).
 * Les codes dans cette table sont autorisés à s'inscrire sur la plateforme.
 */
@Entity
public class AllowedStudentCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String studentCode;

    private boolean used = false;

    private LocalDateTime createdAt;

    private String createdBy; // Username de l'admin qui a créé le code

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "used_by_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Student usedBy; // Référence à l'étudiant qui a utilisé ce code (si used=true)

    // Constructeur vide (obligatoire pour JPA)
    public AllowedStudentCode() {
    }

    // Constructeur avec paramètres
    public AllowedStudentCode(String studentCode, String createdBy) {
        this.studentCode = studentCode;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Student getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(Student usedBy) {
        this.usedBy = usedBy;
    }

    // Méthode métier : marquer comme utilisé
    public void markAsUsed(Student student) {
        this.used = true;
        this.usedBy = student;
    }
}
