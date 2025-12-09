package com.example.covoiturage_vaadin.ui.component.dialog;

import com.example.covoiturage_vaadin.application.dto.message.ConversationDTO;
import com.example.covoiturage_vaadin.application.dto.message.MessageDTO;
import com.example.covoiturage_vaadin.application.services.MessageService;
import com.example.covoiturage_vaadin.ui.component.message.MessageBubble;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

/**
 * Dialog pour afficher et envoyer des messages dans une conversation.
 */
public class ConversationDialog extends Dialog {

    private final MessageService messageService;
    private final Long conversationId;
    private final Runnable onSuccess;

    private VerticalLayout messagesContainer;
    private TextField messageField;
    private Long otherParticipantId;

    public ConversationDialog(MessageService messageService, Long conversationId, Runnable onSuccess) {
        this.messageService = messageService;
        this.conversationId = conversationId;
        this.onSuccess = onSuccess;

        setWidth("600px");
        setHeight("80vh");
        setDraggable(true);
        setResizable(true);

        // Charger la conversation
        ConversationDTO conversation = messageService.getConversationById(conversationId);
        this.otherParticipantId = conversation.getOtherParticipant().getId();

        // Header avec nom du participant
        createHeader(conversation);

        // Contenu principal
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);

        // Zone des messages (scrollable)
        messagesContainer = new VerticalLayout();
        messagesContainer.setPadding(true);
        messagesContainer.setSpacing(true);
        messagesContainer.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)");

        Scroller scroller = new Scroller(messagesContainer);
        scroller.setSizeFull();
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.getStyle().set("flex-grow", "1");

        // Zone de saisie
        HorizontalLayout inputArea = createInputArea();

        content.add(scroller, inputArea);
        add(content);

        // Charger les messages
        loadMessages();
    }

    private void createHeader(ConversationDTO conversation) {
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        
        // 1. IMPORTANT : Le header doit prendre toute la largeur
        header.setWidthFull(); 
        header.setPadding(false); // Optionnel : ajuste selon le conteneur parent

        // Avatar
        Icon avatar = VaadinIcon.USER.create();
        avatar.setSize("40px");
        avatar.getStyle()
            .set("background-color", "var(--lumo-primary-color-10pct)")
            .set("color", "var(--lumo-primary-color)")
            .set("border-radius", "50%")
            .set("padding", "8px");

        // Nom
        H3 name = new H3(conversation.getOtherParticipant().getName());
        name.getStyle().set("margin", "0");

        // Email (sous-titre)
        Span email = new Span(conversation.getOtherParticipant().getEmail());
        email.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout info = new VerticalLayout(name, email);
        info.setPadding(false);
        info.setSpacing(false);

        // Bouton de fermeture
        Button closeBtn = new Button(VaadinIcon.CLOSE.create(), e -> close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        // 2. On ajoute TOUS les éléments dans le header
        header.add(avatar, info, closeBtn);

        // 3. LA CLEF : On expand "info". 
        // Cela force "info" à prendre tout l'espace vide disponible, 
        // poussant ainsi "closeBtn" vers l'extrémité droite.
        header.expand(info);

        // Finalisation
        setHeaderTitle("");
        getHeader().add(header);
    }

    private HorizontalLayout createInputArea() {
        HorizontalLayout inputArea = new HorizontalLayout();
        inputArea.setWidthFull();
        inputArea.setPadding(true);
        inputArea.setSpacing(true);
        inputArea.setAlignItems(FlexComponent.Alignment.CENTER);
        inputArea.getStyle()
            .set("background-color", "white")
            .set("border-top", "1px solid var(--lumo-contrast-10pct)");

        // Options pour masquer la conversation
        Button optionsBtn = new Button(VaadinIcon.ELLIPSIS_DOTS_V.create());
        optionsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        optionsBtn.setTooltipText("Options");
        optionsBtn.addClickListener(e -> {
            // Dialog de confirmation pour masquer
            Dialog confirmDialog = new Dialog();
            confirmDialog.setHeaderTitle("Masquer la conversation ?");
            confirmDialog.add(new Span("Cette conversation sera masquée de votre liste. Vous pourrez la retrouver si vous recevez un nouveau message."));

            Button cancelBtn = new Button("Annuler", ev -> confirmDialog.close());
            Button hideBtn = new Button("Masquer", ev -> {
                try {
                    messageService.hideConversation(conversationId);
                    Notification.show("Conversation masquée", 3000, Notification.Position.MIDDLE);
                    if (onSuccess != null) onSuccess.run();
                    confirmDialog.close();
                    close();
                } catch (Exception ex) {
                    Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            hideBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            confirmDialog.getFooter().add(cancelBtn, hideBtn);
            confirmDialog.open();
        });

        // Champ de saisie
        messageField = new TextField();
        messageField.setPlaceholder("Écrivez votre message...");
        messageField.setWidthFull();
        messageField.setClearButtonVisible(true);
        messageField.addKeyPressListener(Key.ENTER, e -> sendMessage());

        // Bouton d'envoi
        Button sendBtn = new Button(VaadinIcon.PAPERPLANE.create());
        sendBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);
        sendBtn.setTooltipText("Envoyer");
        sendBtn.addClickListener(e -> sendMessage());

        inputArea.add(optionsBtn, messageField, sendBtn);
        inputArea.expand(messageField);

        return inputArea;
    }

    private void loadMessages() {
        messagesContainer.removeAll();

        List<MessageDTO> messages = messageService.getConversationMessages(conversationId);

        if (messages.isEmpty()) {
            Div emptyState = new Div();
            emptyState.setText("Aucun message. Commencez la conversation !");
            emptyState.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-align", "center")
                .set("padding", "var(--lumo-space-xl)");
            messagesContainer.add(emptyState);
            return;
        }

        for (MessageDTO message : messages) {
            boolean isFromMe = !message.getSender().getId().equals(otherParticipantId);
            MessageBubble bubble = new MessageBubble(message, isFromMe);
            messagesContainer.add(bubble);
        }

        // Scroll vers le bas
        messagesContainer.getElement().executeJs(
            "setTimeout(() => { this.scrollTop = this.scrollHeight; }, 100);"
        );
    }

    private void sendMessage() {
        String content = messageField.getValue();
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        try {
            messageService.sendMessage(otherParticipantId, content.trim());
            messageField.clear();
            loadMessages();
            if (onSuccess != null) onSuccess.run();
        } catch (Exception ex) {
            Notification.show("Erreur : " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
