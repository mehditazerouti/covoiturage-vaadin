package com.example.covoiturage_vaadin.application.ports;

import com.example.covoiturage_vaadin.domain.model.Conversation;
import java.util.List;
import java.util.Optional;

/**
 * Port pour la couche Application - Gestion des conversations.
 *
 * Interface définissant les méthodes nécessaires pour manipuler les conversations
 * indépendamment de la technologie de persistance.
 */
public interface IConversationRepositoryPort {

    /**
     * Sauvegarde une conversation (création ou mise à jour)
     */
    Conversation save(Conversation conversation);

    /**
     * Recherche une conversation par son ID
     */
    Optional<Conversation> findById(Long id);

    /**
     * Recherche une conversation entre deux participants (dans n'importe quel ordre)
     */
    Optional<Conversation> findByParticipants(Long participant1Id, Long participant2Id);

    /**
     * Récupère toutes les conversations visibles d'un participant (excluant les soft-deleted)
     * Triées par date du dernier message (plus récent en premier)
     */
    List<Conversation> findVisibleByParticipantId(Long participantId);

    /**
     * Récupère toutes les conversations d'un participant (y compris les masquées)
     */
    List<Conversation> findAllByParticipantId(Long participantId);

    /**
     * Supprime une conversation par son ID
     */
    void deleteById(Long id);
}
