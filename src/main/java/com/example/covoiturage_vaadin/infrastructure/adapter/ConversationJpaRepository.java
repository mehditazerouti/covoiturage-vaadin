package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.domain.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationJpaRepository extends JpaRepository<Conversation, Long> {

    /**
     * Recherche une conversation entre deux participants (dans n'importe quel ordre)
     */
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.participant1.id = :p1 AND c.participant2.id = :p2) OR " +
           "(c.participant1.id = :p2 AND c.participant2.id = :p1)")
    Optional<Conversation> findByParticipants(@Param("p1") Long participant1Id,
                                               @Param("p2") Long participant2Id);

    /**
     * Récupère toutes les conversations visibles d'un participant (excluant les soft-deleted)
     * Triées par date du dernier message (plus récent en premier)
     */
    @Query("SELECT c FROM Conversation c WHERE " +
           "((c.participant1.id = :userId AND c.hiddenByParticipant1 = false) OR " +
           "(c.participant2.id = :userId AND c.hiddenByParticipant2 = false)) " +
           "ORDER BY c.lastMessageAt DESC")
    List<Conversation> findVisibleByParticipantId(@Param("userId") Long participantId);

    /**
     * Récupère toutes les conversations d'un participant (y compris les masquées)
     */
    @Query("SELECT c FROM Conversation c WHERE " +
           "c.participant1.id = :userId OR c.participant2.id = :userId " +
           "ORDER BY c.lastMessageAt DESC")
    List<Conversation> findAllByParticipantId(@Param("userId") Long participantId);
}
