package com.example.covoiturage_vaadin.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant un message dans une conversation.
 *
 * Validations JSR-303 appliquées sur les champs pour garantir
 * l'intégrité des données au niveau de la couche domaine.
 */
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La conversation est obligatoire")
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Conversation conversation;

    @NotNull(message = "L'expéditeur est obligatoire")
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student sender;

    @NotNull(message = "Le destinataire est obligatoire")
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student recipient;

    @NotBlank(message = "Le contenu du message est obligatoire")
    @Size(min = 1, max = 2000, message = "Le message doit contenir entre 1 et 2000 caractères")
    @Column(length = 2000, nullable = false)
    private String content;

    @NotNull(message = "La date d'envoi est obligatoire")
    private LocalDateTime sentAt;

    private boolean isRead = false;

    // Constructeur vide (obligatoire pour JPA)
    public Message() {
    }

    // Constructeur avec paramètres
    public Message(Conversation conversation, Student sender, Student recipient, String content) {
        this.conversation = conversation;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public Student getSender() {
        return sender;
    }

    public void setSender(Student sender) {
        this.sender = sender;
    }

    public Student getRecipient() {
        return recipient;
    }

    public void setRecipient(Student recipient) {
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

    // Méthodes métier

    /**
     * Marque le message comme lu
     */
    public void markAsRead() {
        this.isRead = true;
    }

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
