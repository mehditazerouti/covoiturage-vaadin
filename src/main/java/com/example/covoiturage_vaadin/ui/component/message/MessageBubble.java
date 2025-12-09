package com.example.covoiturage_vaadin.ui.component.message;

import com.example.covoiturage_vaadin.application.dto.message.MessageDTO;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.time.format.DateTimeFormatter;

/**
 * Composant pour afficher un message sous forme de bulle.
 * Style différent selon si le message est envoyé par moi ou reçu.
 */
public class MessageBubble extends HorizontalLayout {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public MessageBubble(MessageDTO message, boolean isFromMe) {
        setWidthFull();
        setPadding(false);
        setSpacing(false);

        // Conteneur de la bulle
        Div bubble = new Div();
        bubble.getStyle()
            .set("max-width", "75%")
            .set("padding", "10px 14px")
            .set("border-radius", "16px")
            .set("word-wrap", "break-word")
            .set("white-space", "pre-wrap");

        // Contenu du message
        Div contentDiv = new Div();
        contentDiv.setText(message.getContent());
        contentDiv.getStyle()
            .set("margin-bottom", "4px")
            .set("line-height", "1.4");

        // Heure d'envoi
        Span timeSpan = new Span(message.getSentAt().format(TIME_FORMATTER));
        timeSpan.getStyle()
            .set("font-size", "var(--lumo-font-size-xs)")
            .set("opacity", "0.7");

        bubble.add(contentDiv, timeSpan);

        if (isFromMe) {
            // Message envoyé par moi - aligné à droite, bleu
            setJustifyContentMode(JustifyContentMode.END);

            bubble.getStyle()
                .set("background-color", "var(--lumo-primary-color)")
                .set("color", "white")
                .set("border-bottom-right-radius", "4px");

            timeSpan.getStyle().set("color", "rgba(255, 255, 255, 0.7)");
        } else {
            // Message reçu - aligné à gauche, gris
            setJustifyContentMode(JustifyContentMode.START);

            bubble.getStyle()
                .set("background-color", "white")
                .set("color", "var(--lumo-body-text-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-bottom-left-radius", "4px");

            timeSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
        }

        // Indicateur de lecture (pour les messages envoyés)
        if (isFromMe && message.isRead()) {
            Span readIndicator = new Span(" ✓✓");
            readIndicator.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "rgba(255, 255, 255, 0.9)");
            timeSpan.add(readIndicator);
        }

        add(bubble);
    }
}
