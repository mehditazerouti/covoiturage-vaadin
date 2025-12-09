package com.example.covoiturage_vaadin.infrastructure.adapter;

import com.example.covoiturage_vaadin.application.ports.IMessageRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MessageRepositoryAdapter implements IMessageRepositoryPort {

    private final MessageJpaRepository jpaRepository;

    public MessageRepositoryAdapter(MessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Message save(Message message) {
        return jpaRepository.save(message);
    }

    @Override
    public Optional<Message> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId) {
        return jpaRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }

    @Override
    public Optional<Message> findLastByConversationId(Long conversationId) {
        return jpaRepository.findFirstByConversationIdOrderBySentAtDesc(conversationId);
    }

    @Override
    public int countUnreadByConversationIdAndRecipientId(Long conversationId, Long recipientId) {
        return jpaRepository.countUnreadByConversationIdAndRecipientId(conversationId, recipientId);
    }

    @Override
    public int countUnreadByRecipientId(Long recipientId) {
        return jpaRepository.countUnreadByRecipientId(recipientId);
    }

    @Override
    public void markAllAsReadByConversationIdAndRecipientId(Long conversationId, Long recipientId) {
        jpaRepository.markAllAsReadByConversationIdAndRecipientId(conversationId, recipientId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
