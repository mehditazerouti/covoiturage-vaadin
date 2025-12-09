package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.application.ports.IConversationRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Conversation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ConversationRepositoryAdapter implements IConversationRepositoryPort {

    private final ConversationJpaRepository jpaRepository;

    public ConversationRepositoryAdapter(ConversationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Conversation save(Conversation conversation) {
        return jpaRepository.save(conversation);
    }

    @Override
    public Optional<Conversation> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Conversation> findByParticipants(Long participant1Id, Long participant2Id) {
        return jpaRepository.findByParticipants(participant1Id, participant2Id);
    }

    @Override
    public List<Conversation> findVisibleByParticipantId(Long participantId) {
        return jpaRepository.findVisibleByParticipantId(participantId);
    }

    @Override
    public List<Conversation> findAllByParticipantId(Long participantId) {
        return jpaRepository.findAllByParticipantId(participantId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
