package com.example.covoiturage_vaadin.application.dto.message;

import com.example.covoiturage_vaadin.application.dto.student.StudentListDTO;
import java.time.LocalDateTime;

/**
 * DTO pour afficher une conversation dans la liste.
 *
 * Utilisé pour :
 * - Afficher la liste des conversations dans MessagingView
 * - Afficher un aperçu de la conversation
 *
 * Particularités :
 * - Contient l'autre participant (pas les deux)
 * - Contient un aperçu du dernier message
 * - Contient le nombre de messages non lus
 */
public class ConversationDTO {
    private Long id;
    private StudentListDTO otherParticipant;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
    private boolean isLastMessageFromMe;

    // Constructeur vide
    public ConversationDTO() {}

    // Constructeur complet
    public ConversationDTO(Long id, StudentListDTO otherParticipant, String lastMessagePreview,
                           LocalDateTime lastMessageAt, int unreadCount, boolean isLastMessageFromMe) {
        this.id = id;
        this.otherParticipant = otherParticipant;
        this.lastMessagePreview = lastMessagePreview;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
        this.isLastMessageFromMe = isLastMessageFromMe;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentListDTO getOtherParticipant() {
        return otherParticipant;
    }

    public void setOtherParticipant(StudentListDTO otherParticipant) {
        this.otherParticipant = otherParticipant;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isLastMessageFromMe() {
        return isLastMessageFromMe;
    }

    public void setLastMessageFromMe(boolean lastMessageFromMe) {
        isLastMessageFromMe = lastMessageFromMe;
    }

    // Méthodes utilitaires

    /**
     * Vérifie si la conversation a des messages non lus
     */
    public boolean hasUnreadMessages() {
        return unreadCount > 0;
    }
}
