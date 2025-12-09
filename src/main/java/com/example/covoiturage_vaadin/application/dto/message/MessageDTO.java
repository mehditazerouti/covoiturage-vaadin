package com.example.covoiturage_vaadin.application.dto.message;

import com.example.covoiturage_vaadin.application.dto.student.StudentListDTO;
import java.time.LocalDateTime;

/**
 * DTO pour afficher un message avec ses détails.
 *
 * Utilisé pour :
 * - Afficher les messages dans une conversation
 * - Afficher le dernier message d'une conversation
 *
 * Particularités :
 * - Sender et recipient sont des StudentListDTO (version minimale)
 * - Contient les informations pour afficher les bulles de message
 */
public class MessageDTO {
    private Long id;
    private Long conversationId;
    private StudentListDTO sender;
    private StudentListDTO recipient;
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead;

    // Constructeur vide
    public MessageDTO() {}

    // Constructeur complet
    public MessageDTO(Long id, Long conversationId, StudentListDTO sender, StudentListDTO recipient,
                      String content, LocalDateTime sentAt, boolean isRead) {
        this.id = id;
        this.conversationId = conversationId;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public StudentListDTO getSender() {
        return sender;
    }

    public void setSender(StudentListDTO sender) {
        this.sender = sender;
    }

    public StudentListDTO getRecipient() {
        return recipient;
    }

    public void setRecipient(StudentListDTO recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // Méthodes utilitaires

    /**
     * Vérifie si le message a été envoyé par un utilisateur donné
     */
    public boolean isSentBy(Long userId) {
        return sender != null && sender.getId().equals(userId);
    }

    /**
     * Retourne un aperçu du contenu (pour les listes)
     */
    public String getContentPreview(int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength - 3) + "...";
    }
}
