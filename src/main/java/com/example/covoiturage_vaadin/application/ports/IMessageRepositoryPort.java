package com.example.covoiturage_vaadin.application.ports;

import com.example.covoiturage_vaadin.domain.model.Message;
import java.util.List;
import java.util.Optional;

/**
 * Port pour la couche Application - Gestion des messages.
 *
 * Interface définissant les méthodes nécessaires pour manipuler les messages
 * indépendamment de la technologie de persistance.
 */
public interface IMessageRepositoryPort {

    /**
     * Sauvegarde un message (création ou mise à jour)
     */
    Message save(Message message);

    /**
     * Recherche un message par son ID
     */
    Optional<Message> findById(Long id);

    /**
     * Récupère tous les messages d'une conversation, triés par date
     */
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    /**
     * Récupère le dernier message d'une conversation
     */
    Optional<Message> findLastByConversationId(Long conversationId);

    /**
     * Compte les messages non lus d'une conversation pour un destinataire
     */
    int countUnreadByConversationIdAndRecipientId(Long conversationId, Long recipientId);

    /**
     * Compte le nombre total de messages non lus pour un utilisateur
     */
    int countUnreadByRecipientId(Long recipientId);

    /**
     * Marque tous les messages d'une conversation comme lus pour un destinataire
     */
    void markAllAsReadByConversationIdAndRecipientId(Long conversationId, Long recipientId);

    /**
     * Supprime un message par son ID
     */
    void deleteById(Long id);
}
