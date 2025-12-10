package com.example.covoiturage_vaadin.application.services;

import com.example.covoiturage_vaadin.application.dto.mapper.MessageMapper;
import com.example.covoiturage_vaadin.application.dto.message.ContactDTO;
import com.example.covoiturage_vaadin.application.dto.message.ConversationDTO;
import com.example.covoiturage_vaadin.application.dto.message.MessageDTO;
import com.example.covoiturage_vaadin.application.ports.IBookingRepositoryPort;
import com.example.covoiturage_vaadin.application.ports.IConversationRepositoryPort;
import com.example.covoiturage_vaadin.application.ports.IMessageRepositoryPort;
import com.example.covoiturage_vaadin.application.ports.ITripRepositoryPort;
import com.example.covoiturage_vaadin.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de messagerie entre utilisateurs.
 *
 * Règles de sécurité :
 * - ADMIN : Peut contacter TOUS les utilisateurs (support, communication)
 * - UTILISATEURS : Peuvent s'envoyer des messages SI ET SEULEMENT SI
 *   ils ont partagé un covoiturage (l'un conducteur, l'autre passager).
 * - Les réservations annulées comptent dans l'historique.
 * - Les conversations avec l'admin sont bidirectionnelles (l'utilisateur peut répondre).
 */
@Service
@Transactional(readOnly = true)
public class MessageService {

    private final IMessageRepositoryPort messageRepository;
    private final IConversationRepositoryPort conversationRepository;
    private final IBookingRepositoryPort bookingRepository;
    private final ITripRepositoryPort tripRepository;
    private final StudentService studentService;
    private final SecurityContextService securityContext;
    private final MessageMapper messageMapper;

    public MessageService(IMessageRepositoryPort messageRepository,
                         IConversationRepositoryPort conversationRepository,
                         IBookingRepositoryPort bookingRepository,
                         ITripRepositoryPort tripRepository,
                         StudentService studentService,
                         SecurityContextService securityContext,
                         MessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.studentService = studentService;
        this.securityContext = securityContext;
        this.messageMapper = messageMapper;
    }

    // ==================== CONVERSATIONS ====================

    /**
     * Récupère toutes les conversations visibles de l'utilisateur connecté.
     * @return Liste de ConversationDTO triée par date du dernier message
     */
    public List<ConversationDTO> getMyConversations() {
        Long currentUserId = getCurrentUserId();

        List<Conversation> conversations = conversationRepository.findVisibleByParticipantId(currentUserId);

        return conversations.stream()
                .map(conv -> {
                    Message lastMessage = messageRepository.findLastByConversationId(conv.getId()).orElse(null);
                    int unreadCount = messageRepository.countUnreadByConversationIdAndRecipientId(conv.getId(), currentUserId);
                    return messageMapper.toConversationDTO(conv, currentUserId, lastMessage, unreadCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * Récupère une conversation par son ID.
     * @param conversationId ID de la conversation
     * @return ConversationDTO ou null si non trouvée
     */
    public ConversationDTO getConversationById(Long conversationId) {
        Long currentUserId = getCurrentUserId();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation non trouvée"));

        // Vérifier que l'utilisateur est participant
        if (!conversation.hasParticipant(currentUserId)) {
            throw new IllegalStateException("Vous n'avez pas accès à cette conversation");
        }

        Message lastMessage = messageRepository.findLastByConversationId(conversationId).orElse(null);
        int unreadCount = messageRepository.countUnreadByConversationIdAndRecipientId(conversationId, currentUserId);

        return messageMapper.toConversationDTO(conversation, currentUserId, lastMessage, unreadCount);
    }

    /**
     * Masque une conversation pour l'utilisateur connecté (soft delete).
     * @param conversationId ID de la conversation à masquer
     */
    @Transactional
    public void hideConversation(Long conversationId) {
        Long currentUserId = getCurrentUserId();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation non trouvée"));

        // Vérifier que l'utilisateur est participant
        if (!conversation.hasParticipant(currentUserId)) {
            throw new IllegalStateException("Vous n'avez pas accès à cette conversation");
        }

        conversation.hideFor(currentUserId);
        conversationRepository.save(conversation);
    }

    // ==================== MESSAGES ====================

    /**
     * Récupère tous les messages d'une conversation.
     * Marque automatiquement les messages comme lus.
     * @param conversationId ID de la conversation
     * @return Liste de MessageDTO triée par date
     */
    @Transactional
    public List<MessageDTO> getConversationMessages(Long conversationId) {
        Long currentUserId = getCurrentUserId();

        // Vérifier l'accès à la conversation
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation non trouvée"));

        if (!conversation.hasParticipant(currentUserId)) {
            throw new IllegalStateException("Vous n'avez pas accès à cette conversation");
        }

        // Marquer tous les messages comme lus
        messageRepository.markAllAsReadByConversationIdAndRecipientId(conversationId, currentUserId);

        // Réafficher la conversation si elle était masquée (nouveau message reçu)
        conversation.unhideFor(currentUserId);
        conversationRepository.save(conversation);

        // Récupérer les messages
        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        return messages.stream()
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Envoie un message à un destinataire.
     * Crée la conversation si elle n'existe pas.
     * @param recipientId ID du destinataire
     * @param content Contenu du message
     * @return MessageDTO du message envoyé
     */
    @Transactional
    public MessageDTO sendMessage(Long recipientId, String content) {
        Long currentUserId = getCurrentUserId();

        // Validation du contenu
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Le message ne peut pas être vide");
        }
        if (content.length() > 2000) {
            throw new IllegalArgumentException("Le message ne peut pas dépasser 2000 caractères");
        }

        // Vérifier qu'on ne s'envoie pas un message à soi-même
        if (currentUserId.equals(recipientId)) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous envoyer un message à vous-même");
        }

        // Vérifier l'éligibilité à la messagerie
        if (!canMessageUser(recipientId)) {
            throw new IllegalStateException("Vous ne pouvez contacter que les personnes avec qui vous avez partagé un covoiturage");
        }

        // Récupérer les étudiants
        Student sender = studentService.getStudentByUsername(getCurrentUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
        Student recipient = studentService.getStudentEntityById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Destinataire non trouvé"));

        // Récupérer ou créer la conversation
        Conversation conversation = conversationRepository.findByParticipants(currentUserId, recipientId)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation(sender, recipient);
                    return conversationRepository.save(newConv);
                });

        // Réafficher la conversation pour les deux participants (si elle était masquée)
        conversation.unhideFor(currentUserId);
        conversation.unhideFor(recipientId);
        conversation.updateLastMessageAt();
        conversationRepository.save(conversation);

        // Créer et sauvegarder le message
        Message message = new Message(conversation, sender, recipient, content.trim());
        Message savedMessage = messageRepository.save(message);

        return messageMapper.toDTO(savedMessage);
    }

    // ==================== CONTACTS ====================

    /**
     * Récupère la liste des utilisateurs contactables.
     *
     * Règles :
     * - ADMIN : Peut contacter TOUS les étudiants (sauf lui-même)
     * - UTILISATEUR : Peut contacter les personnes avec qui il a partagé un covoiturage
     *
     * @return Liste de ContactDTO
     */
    public List<ContactDTO> getContactableUsers() {
        Long currentUserId = getCurrentUserId();

        // Si l'utilisateur est ADMIN → retourner tous les étudiants (sauf lui-même)
        if (securityContext.hasRole("ADMIN")) {
            return getAllStudentsAsContacts(currentUserId);
        }

        // Sinon, logique normale : seulement les covoitureurs
        return getCovoitureursAsContacts(currentUserId);
    }

    /**
     * Récupère tous les étudiants comme contacts (pour l'admin).
     * Exclut l'admin lui-même et les autres admins.
     */
    private List<ContactDTO> getAllStudentsAsContacts(Long currentUserId) {
        return studentService.getAllStudents().stream()
                .filter(student -> !student.getId().equals(currentUserId)) // Exclure soi-même
                .filter(student -> !"ROLE_ADMIN".equals(student.getRole())) // Exclure les admins
                .filter(student -> student.isApproved() && student.isEnabled()) // Seulement les actifs
                .map(student -> {
                    // Récupérer l'entité pour utiliser le mapper
                    return studentService.getStudentEntityById(student.getId())
                            .map(entity -> messageMapper.toContactDTO(entity, "Étudiant"))
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ContactDTO::getName))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les covoitureurs comme contacts (logique standard pour utilisateurs).
     */
    private List<ContactDTO> getCovoitureursAsContacts(Long currentUserId) {
        Set<Long> contactableUserIds = new HashSet<>();
        Map<Long, String> tripContexts = new HashMap<>();

        // 1. Trouver les conducteurs des trajets où l'utilisateur est passager
        List<Booking> myBookings = bookingRepository.findByStudentId(currentUserId);
        for (Booking booking : myBookings) {
            Long driverId = booking.getTrip().getDriver().getId();
            if (!driverId.equals(currentUserId)) {
                contactableUserIds.add(driverId);
                String context = "Conducteur sur " + booking.getTrip().getDepartureAddress() +
                                " → " + booking.getTrip().getDestinationAddress();
                tripContexts.putIfAbsent(driverId, context);
            }
        }

        // 2. Trouver les passagers des trajets où l'utilisateur est conducteur
        List<Trip> myTrips = tripRepository.findByDriverId(currentUserId);
        for (Trip trip : myTrips) {
            List<Booking> tripBookings = bookingRepository.findByTripId(trip.getId());
            for (Booking booking : tripBookings) {
                Long passengerId = booking.getStudent().getId();
                if (!passengerId.equals(currentUserId)) {
                    contactableUserIds.add(passengerId);
                    String context = "Passager sur " + trip.getDepartureAddress() +
                                    " → " + trip.getDestinationAddress();
                    tripContexts.putIfAbsent(passengerId, context);
                }
            }
        }

        // 3. Ajouter les admins avec qui on a une conversation existante (pour pouvoir répondre)
        List<Conversation> myConversations = conversationRepository.findVisibleByParticipantId(currentUserId);
        for (Conversation conv : myConversations) {
            Student otherParticipant = conv.getOtherParticipant(currentUserId);
            if (otherParticipant != null && "ROLE_ADMIN".equals(otherParticipant.getRole())) {
                contactableUserIds.add(otherParticipant.getId());
                tripContexts.putIfAbsent(otherParticipant.getId(), "Admin");
            }
        }

        // Convertir en ContactDTO
        return contactableUserIds.stream()
                .map(userId -> studentService.getStudentEntityById(userId)
                        .map(student -> messageMapper.toContactDTO(student, tripContexts.get(userId)))
                        .orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ContactDTO::getName))
                .collect(Collectors.toList());
    }

    /**
     * Vérifie si l'utilisateur connecté peut envoyer un message à un autre utilisateur.
     *
     * Règles :
     * - Un admin peut contacter TOUT LE MONDE
     * - Un utilisateur peut répondre à un admin (s'il existe une conversation)
     * - Un utilisateur peut contacter un autre utilisateur s'ils ont partagé un covoiturage
     *
     * @param targetUserId ID de l'utilisateur cible
     * @return true si la messagerie est autorisée
     */
    public boolean canMessageUser(Long targetUserId) {
        Long currentUserId = getCurrentUserId();

        // Ne peut pas se contacter soi-même
        if (currentUserId.equals(targetUserId)) {
            return false;
        }

        // 1. Si l'utilisateur courant est ADMIN → peut contacter tout le monde
        if (securityContext.hasRole("ADMIN")) {
            return true;
        }

        // 2. Si la cible est ADMIN et qu'une conversation existe → peut répondre
        Student targetUser = studentService.getStudentEntityById(targetUserId).orElse(null);
        if (targetUser != null && "ROLE_ADMIN".equals(targetUser.getRole())) {
            // Vérifier s'il existe déjà une conversation avec cet admin
            Optional<Conversation> existingConversation = conversationRepository.findByParticipants(currentUserId, targetUserId);
            if (existingConversation.isPresent()) {
                return true;
            }
        }

        // 3. Vérifier si targetUser est conducteur d'un trajet où currentUser a une réservation
        List<Booking> myBookings = bookingRepository.findByStudentId(currentUserId);
        boolean targetIsMyDriver = myBookings.stream()
                .anyMatch(b -> b.getTrip().getDriver().getId().equals(targetUserId));
        if (targetIsMyDriver) {
            return true;
        }

        // 4. Vérifier si currentUser est conducteur d'un trajet où targetUser a une réservation
        List<Trip> myTrips = tripRepository.findByDriverId(currentUserId);
        for (Trip trip : myTrips) {
            if (bookingRepository.existsByTripIdAndStudentId(trip.getId(), targetUserId)) {
                return true;
            }
        }

        return false;
    }

    // ==================== COMPTEURS ====================

    /**
     * Compte le nombre total de messages non lus pour l'utilisateur connecté.
     * @return Nombre de messages non lus
     */
    public int getTotalUnreadCount() {
        Long currentUserId = getCurrentUserId();
        return messageRepository.countUnreadByRecipientId(currentUserId);
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private Long getCurrentUserId() {
        String username = getCurrentUsername();
        return studentService.getStudentByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"))
                .getId();
    }

    private String getCurrentUsername() {
        return securityContext.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("Aucun utilisateur authentifié"));
    }
}
