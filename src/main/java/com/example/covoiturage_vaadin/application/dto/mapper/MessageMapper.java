package com.example.covoiturage_vaadin.application.dto.mapper;

import com.example.covoiturage_vaadin.application.dto.message.ConversationDTO;
import com.example.covoiturage_vaadin.application.dto.message.ContactDTO;
import com.example.covoiturage_vaadin.application.dto.message.MessageDTO;
import com.example.covoiturage_vaadin.application.dto.student.StudentListDTO;
import com.example.covoiturage_vaadin.domain.model.Conversation;
import com.example.covoiturage_vaadin.domain.model.Message;
import com.example.covoiturage_vaadin.domain.model.Student;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir Message/Conversation Entity → DTO.
 *
 * Ce mapper centralise toutes les conversions entre les entités Message/Conversation
 * et leurs représentations DTO.
 *
 * Dépendances :
 * - StudentMapper (pour convertir les participants)
 *
 * Utilisé par :
 * - MessageService (pour retourner des DTO)
 */
@Component
public class MessageMapper {

    private final StudentMapper studentMapper;

    public MessageMapper(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    /**
     * Convertit une entité Message en MessageDTO.
     *
     * @param message L'entité Message à convertir
     * @return MessageDTO ou null si message est null
     */
    public MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }

        return new MessageDTO(
            message.getId(),
            message.getConversation() != null ? message.getConversation().getId() : null,
            studentMapper.toListDTO(message.getSender()),
            studentMapper.toListDTO(message.getRecipient()),
            message.getContent(),
            message.getSentAt(),
            message.isRead()
        );
    }

    /**
     * Convertit une entité Conversation en ConversationDTO.
     * Nécessite des informations supplémentaires calculées par le service.
     *
     * @param conversation L'entité Conversation à convertir
     * @param currentUserId L'ID de l'utilisateur connecté (pour déterminer l'autre participant)
     * @param lastMessage Le dernier message de la conversation (peut être null)
     * @param unreadCount Le nombre de messages non lus
     * @return ConversationDTO ou null si conversation est null
     */
    public ConversationDTO toConversationDTO(Conversation conversation, Long currentUserId,
                                              Message lastMessage, int unreadCount) {
        if (conversation == null) {
            return null;
        }

        Student otherParticipant = conversation.getOtherParticipant(currentUserId);
        StudentListDTO otherParticipantDTO = studentMapper.toListDTO(otherParticipant);

        String lastMessagePreview = null;
        boolean isLastMessageFromMe = false;

        if (lastMessage != null) {
            lastMessagePreview = lastMessage.getContentPreview(50);
            isLastMessageFromMe = lastMessage.isSentBy(currentUserId);
        }

        return new ConversationDTO(
            conversation.getId(),
            otherParticipantDTO,
            lastMessagePreview,
            conversation.getLastMessageAt(),
            unreadCount,
            isLastMessageFromMe
        );
    }

    /**
     * Convertit un Student en ContactDTO.
     *
     * @param student L'étudiant à convertir
     * @param tripContext Le contexte du covoiturage partagé (optionnel)
     * @return ContactDTO ou null si student est null
     */
    public ContactDTO toContactDTO(Student student, String tripContext) {
        if (student == null) {
            return null;
        }

        return new ContactDTO(
            student.getId(),
            student.getName(),
            student.getEmail(),
            student.getAvatar(),
            tripContext
        );
    }

    /**
     * Convertit un Student en ContactDTO sans contexte.
     *
     * @param student L'étudiant à convertir
     * @return ContactDTO ou null si student est null
     */
    public ContactDTO toContactDTO(Student student) {
        return toContactDTO(student, null);
    }
}
