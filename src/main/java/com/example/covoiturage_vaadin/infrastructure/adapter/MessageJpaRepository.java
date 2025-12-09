package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.domain.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageJpaRepository extends JpaRepository<Message, Long> {

    /**
     * Récupère tous les messages d'une conversation, triés par date d'envoi
     */
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    /**
     * Récupère le dernier message d'une conversation
     */
    Optional<Message> findFirstByConversationIdOrderBySentAtDesc(Long conversationId);

    /**
     * Compte les messages non lus d'une conversation pour un destinataire
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId " +
           "AND m.recipient.id = :recipientId AND m.isRead = false")
    int countUnreadByConversationIdAndRecipientId(@Param("conversationId") Long conversationId,
                                                   @Param("recipientId") Long recipientId);

    /**
     * Compte le nombre total de messages non lus pour un utilisateur
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipient.id = :recipientId AND m.isRead = false")
    int countUnreadByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * Marque tous les messages d'une conversation comme lus pour un destinataire
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.id = :conversationId " +
           "AND m.recipient.id = :recipientId AND m.isRead = false")
    void markAllAsReadByConversationIdAndRecipientId(@Param("conversationId") Long conversationId,
                                                      @Param("recipientId") Long recipientId);
}
