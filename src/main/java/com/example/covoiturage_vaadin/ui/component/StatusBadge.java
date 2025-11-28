package com.example.covoiturage_vaadin.ui.component;

import com.example.covoiturage_vaadin.domain.model.BookingStatus;
import com.vaadin.flow.component.html.Span;

/**
 * Composant réutilisable pour afficher un badge de statut de réservation.
 * Affiche le statut avec une couleur appropriée.
 */
public class StatusBadge extends Span {

    public StatusBadge(BookingStatus status) {
        setText(getStatusLabel(status));
        getElement().getThemeList().add(getStatusBadgeTheme(status));
    }

    private String getStatusLabel(BookingStatus status) {
        return switch (status) {
            case PENDING -> "En attente";
            case CONFIRMED -> "Confirmée";
            case CANCELLED -> "Annulée";
        };
    }

    private String getStatusBadgeTheme(BookingStatus status) {
        return switch (status) {
            case PENDING -> "badge contrast";
            case CONFIRMED -> "badge success";
            case CANCELLED -> "badge error";
        };
    }
}
