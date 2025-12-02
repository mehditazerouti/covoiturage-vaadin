package com.example.covoiturage_vaadin.ui.component.badge;

import com.example.covoiturage_vaadin.domain.model.BookingStatus;
import com.vaadin.flow.component.html.Span;

public class StatusBadge extends Span {

    public StatusBadge(BookingStatus status) {
        setText(getStatusLabel(status));
        
        // Ajout du thème "badge" (couleur) ET "pill" (forme arrondie)
        String theme = getStatusBadgeTheme(status) + " pill";
        getElement().getThemeList().add(theme);
        
        // Petit ajustement de style pour aérer
        getStyle().set("padding", "0.3em 0.8em");
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
            case PENDING -> "badge contrast"; // Gris
            case CONFIRMED -> "badge success";  // Vert
            case CANCELLED -> "badge error";    // Rouge
        };
    }
}