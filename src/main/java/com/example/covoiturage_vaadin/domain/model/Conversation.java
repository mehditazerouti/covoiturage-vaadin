package com.example.covoiturage_vaadin.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student participant1;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student participant2;

    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;

    // Soft delete par participant
    private boolean hiddenByParticipant1 = false;
    private boolean hiddenByParticipant2 = false;

    // Constructeur vide (obligatoire pour JPA)
    public Conversation() {
    }

    // Constructeur avec paramètres
    public Conversation(Student participant1, Student participant2) {
        this.participant1 = participant1;
        this.participant2 = participant2;
        this.createdAt = LocalDateTime.now();
        this.lastMessageAt = LocalDateTime.now();
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getParticipant1() {
        return participant1;
    }

    public void setParticipant1(Student participant1) {
        this.participant1 = participant1;
    }

    public Student getParticipant2() {
        return participant2;
    }

    public void setParticipant2(Student participant2) {
        this.participant2 = participant2;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isHiddenByParticipant1() {
        return hiddenByParticipant1;
    }

    public void setHiddenByParticipant1(boolean hiddenByParticipant1) {
        this.hiddenByParticipant1 = hiddenByParticipant1;
    }

    public boolean isHiddenByParticipant2() {
        return hiddenByParticipant2;
    }

    public void setHiddenByParticipant2(boolean hiddenByParticipant2) {
        this.hiddenByParticipant2 = hiddenByParticipant2;
    }

    // Méthodes métier

    /**
     * Masque la conversation pour un participant (soft delete)
     */
    public void hideFor(Long participantId) {
        if (participant1 != null && participant1.getId().equals(participantId)) {
            this.hiddenByParticipant1 = true;
        } else if (participant2 != null && participant2.getId().equals(participantId)) {
            this.hiddenByParticipant2 = true;
        }
    }

    /**
     * Réaffiche la conversation pour un participant
     */
    public void unhideFor(Long participantId) {
        if (participant1 != null && participant1.getId().equals(participantId)) {
            this.hiddenByParticipant1 = false;
        } else if (participant2 != null && participant2.getId().equals(participantId)) {
            this.hiddenByParticipant2 = false;
        }
    }

    /**
     * Vérifie si la conversation est masquée pour un participant
     */
    public boolean isHiddenFor(Long participantId) {
        if (participant1 != null && participant1.getId().equals(participantId)) {
            return hiddenByParticipant1;
        } else if (participant2 != null && participant2.getId().equals(participantId)) {
            return hiddenByParticipant2;
        }
        return false;
    }

    /**
     * Retourne l'autre participant de la conversation
     */
    public Student getOtherParticipant(Long currentUserId) {
        if (participant1 != null && participant1.getId().equals(currentUserId)) {
            return participant2;
        }
        return participant1;
    }

    /**
     * Vérifie si un utilisateur est participant de cette conversation
     */
    public boolean hasParticipant(Long userId) {
        return (participant1 != null && participant1.getId().equals(userId)) ||
               (participant2 != null && participant2.getId().equals(userId));
    }

    /**
     * Met à jour la date du dernier message
     */
    public void updateLastMessageAt() {
        this.lastMessageAt = LocalDateTime.now();
    }
}
